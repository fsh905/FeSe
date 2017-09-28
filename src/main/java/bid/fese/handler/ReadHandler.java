package bid.fese.handler;

import bid.fese.common.Constants;
import bid.fese.entity.SeHeader;
import bid.fese.entity.SeRequest;
import bid.fese.exception.UnsupportedRequestMethodException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.InterruptedByTimeoutException;

/**
 * Created by feng_ on 2016/12/8.
 * 接收请求数据， 并进行简单封装
 */
public class ReadHandler implements CompletionHandler<Integer, ByteBuffer> {
    private AsynchronousSocketChannel socketChannel;
    private static final Logger log = LogManager.getLogger(ReadHandler.class);
    private String socketAddress;
    private RequestHandlers requestHandlers;

    public ReadHandler(AsynchronousSocketChannel socketChannel, RequestHandlers requestHandlers) {
        this.socketChannel = socketChannel;
        this.requestHandlers = requestHandlers;
        try {
            this.socketAddress = socketChannel.getRemoteAddress().toString();
        } catch (IOException e) {
            log.error("get remote address error");
        }
    }

    @Override
    public void completed(Integer readBytesLen, ByteBuffer attachment) {

        if (readBytesLen <= 0) {
            if (readBytesLen == 0) {
                log.error("read byte len is 0, continue read");
                attachment.clear();
                keepAlive(attachment);
            } else {
                log.error("read byte len is " + readBytesLen + ", attempt to close socket");
                if (socketChannel.isOpen()) {
                    try {
                        socketChannel.close();
                        log.info("close socket" + socketAddress);
                    } catch (IOException e) {
                        log.error("close socket failed, address:" + socketAddress);
                    }
                }
                return;
            }
        }

        byte[] bytes = new byte[readBytesLen];
        attachment.flip();          //the length of position to limit
        attachment.get(bytes);
        attachment.clear();

        // 读取头部信息
        // 头部结尾
        int headEndLen = RequestHeaderParser.find(bytes, SeHeader.HEADER_END, 10);
        SeHeader header = null;
        try {
            header = RequestHeaderParser.parseHeader(bytes, headEndLen);
        } catch (UnsupportedRequestMethodException e) {
            log.error("parse header error", e);
        }
        if (header == null) {
            // 关闭连接
            if (socketChannel.isOpen()) {
                try {
                    socketChannel.close();
                    log.info("parse header error, close socket success");
                } catch (IOException e1) {
                    log.error("close socket error, address:" + socketAddress, e1);
                }
            }
            return;
        }

        // 当协议为http/1.0时， 检查connection是否为keep-alive
        // 当协议为http/1.1时， 默认为keep-alive直到收到connection: close或者超时自动关闭
        boolean isKeepAlive = true;
        if (header.getProtocol().equals(SeHeader.PROTOCOL_1_0)) {
            if (header.getHeaderParameter(SeHeader.CONNECTION) == null
                    || header.getHeaderParameter(SeHeader.CONNECTION).equals(SeHeader.CONNECTION_CLOSE)) {
                log.debug("keep-alive not set");
                isKeepAlive = false;
            }
        } else {
            if (header.getHeaderParameter(SeHeader.CONNECTION) != null
                    && header.getHeaderParameter(SeHeader.CONNECTION).equals(SeHeader.CONNECTION_CLOSE)) {
                log.debug("keep-alive not set");
                isKeepAlive = false;
            }
        }

        // 目前只支持get和post方法
        switch (header.getMethod()) {
            case GET: {
                //数据读取完毕, 进行下一阶段
                // request不需要inputStream
                requestHandlers.addRequest(new SeRequest(socketChannel, header, isKeepAlive));
                // 设置keep-alive
                if (isKeepAlive) {
                    keepAlive(attachment);
                }
            }
            break;
            case POST: {
                int contentLen = Integer.parseInt(header.getHeaderParameter(SeHeader.CONTENT_LENGTH));
                log.debug("post contentLen:" + contentLen);
                // 长度超过限制
                if (contentLen > Constants.MAX_UPLOAD_SIZE) {
                    log.error("the post data is too large, close this connection addredd:" + socketAddress);
                    try {
                        socketChannel.close();
                    } catch (IOException e) {
                        log.error("close large post conncetion failed, address:" + socketAddress);
                    }
                    return;
                }
                if (contentLen + headEndLen + 4 == bytes.length && contentLen + headEndLen + 4 <= Constants.DEFAULT_UPLOAD_SIZE) {

                    // 传输的数据较短
                    byte[] in = new byte[contentLen];
                    System.arraycopy(bytes, headEndLen + 4, in, 0, contentLen);

                    requestHandlers.addRequest(
                            new SeRequest(socketChannel, header, in, isKeepAlive));
                    // 设置keep-alive
                    if (isKeepAlive) {
                        keepAlive(attachment);
                    }
                } else {

                    // 当post的数据与头部长度和小于buffer长度时会一次性读取完
                    // 第一次只读取头部
                    // 开始第二次读取
                    ByteBuffer buffer = ByteBuffer.allocate(contentLen);
                    final boolean innerKeepAlive = isKeepAlive;
                    socketChannel.read(buffer, header, new CompletionHandler<Integer, SeHeader>() {
                        @Override
                        public void completed(Integer result, SeHeader seHeader) {
                            // 这里可能一次性读取不完， 待完善
                            log.debug("read large post data success, readLen:" + result);
                            requestHandlers.addRequest(
                                    new SeRequest(socketChannel, seHeader, buffer.array(), innerKeepAlive));
                            // 设置keep-alive
                            if (innerKeepAlive) {
                                keepAlive(attachment);
                            }
                        }

                        @Override
                        public void failed(Throwable exc, SeHeader attachment) {
                            log.error("read error!", new String(buffer.array()), exc);
                            // 关闭
                        }
                    });
                }
            }
            break;
            default: {
                log.error("this request method is not support!");
                attachment.clear();
                if (isKeepAlive) {
                    keepAlive(attachment);
                }
            }
        }
    }

    private void keepAlive(ByteBuffer attachment) {
        if (socketChannel.isOpen()) {
            socketChannel.read(attachment,
                    Constants.DEFAULT_KEEP_ALIVE_TIME,
                    Constants.DEFAULT_KEEP_ALIVE_TIME_UNIT,
                    attachment, this);
        }
    }

    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {
        if (exc instanceof InterruptedByTimeoutException) {
            log.info("timeout error! close socket:" + socketAddress);
            return;
        }
        // when read is fail
        log.error("read error! close socket:" + socketAddress, exc);
        if (socketChannel.isOpen()) {
            try {
                socketChannel.close();
                log.error("close socket success");
            } catch (IOException e) {
                log.error("close socket error" + socketAddress, e);
            }
        }
    }

    /**
     * 解析请求头
     */
    private static class RequestHeaderParser {

        private static SeHeader parseHeader(byte[] bytes, int end) throws UnsupportedRequestMethodException {
            SeHeader header = new SeHeader();
            parse(bytes, header, 0, end);
            return header;
        }

        private static void parse(byte[] bytes, SeHeader header, int headParseIndex, int headEndIndex) throws UnsupportedRequestMethodException {

            int index = requestLineParse(bytes, header, headParseIndex);
            headParseIndex += index;

            String key = null, value = null;
            while (headParseIndex < headEndIndex) {
                while (headEndIndex < bytes.length && bytes[headParseIndex] != ':') {
                    headParseIndex++;
                }
                if (headEndIndex >= bytes.length) {
                    throw new UnsupportedRequestMethodException("can't parse this header:" + new String(bytes));
                }
                key = new String(bytes, index, headParseIndex - index);
                index = headParseIndex += 2;
                while (headEndIndex < bytes.length && bytes[headParseIndex] != '\r') {
                    headParseIndex++;
                }
                if (headEndIndex > bytes.length) {
                    throw new UnsupportedRequestMethodException("can't parse this header:" + new String(bytes));
                }
                try {
                    value = new String(bytes, index, headParseIndex - index, "ISO-8859-1");
                } catch (UnsupportedEncodingException e) {
                    log.error("header parameter iso-8859-1 encode not support");
                }
                index = headParseIndex += 2;
                header.addHeaderParameter(key, value);

//                log.debug("parse header parameter: key:"+key+"-\tvalue:"+value);
            }
        }


        /**
         * parse request line
         * GET /index?name=fese&id=1 HTTP/1.1
         * method get
         * url /index
         * request para name:fese
         * protocol http1.1
         * 解析请求行
         */
        private static int requestLineParse(byte[] bytes, SeHeader header, int headParseIndex) throws UnsupportedRequestMethodException {

            //method
            // c, d, g, h, pa, po, pu, t
            switch (bytes[headParseIndex]) {
                case 67:
                    header.setMethod(SeRequest.METHOD.CONNECT);
                    headParseIndex += 8;
                    break;
                case 68:
                    header.setMethod(SeRequest.METHOD.DELETE);
                    headParseIndex += 7;
                    break;
                case 71:
                    header.setMethod(SeRequest.METHOD.GET);
                    headParseIndex += 4;
                    break;
                case 72:
                    header.setMethod(SeRequest.METHOD.HEAD);
                    headParseIndex += 5;
                    break;
                case 80:
                    switch (bytes[headParseIndex + 1]) {
                        case 65:
                            header.setMethod(SeRequest.METHOD.PATCH);
                            headParseIndex += 6;
                            break;
                        case 79:
                            header.setMethod(SeRequest.METHOD.POST);
                            headParseIndex += 5;
                            break;
                        case 85:
                            header.setMethod(SeRequest.METHOD.PUT);
                            headParseIndex += 4;
                            break;
                        default:
                            throw new UnsupportedRequestMethodException("unsupported request method, error request is :" + new String(bytes));
                    }
                    break;
                case 84:
                    header.setMethod(SeRequest.METHOD.TRACE);
                    headParseIndex += 6;
                    break;
                default:
                    throw new UnsupportedRequestMethodException("unsupported request method, error request is : " + new String(bytes));
            }

            log.debug("method:" + header.getMethod());

            int lastPosi = headParseIndex;
            int index = 0;
            if (headParseIndex > bytes.length) {
                throw new UnsupportedRequestMethodException("unsupported request method, error message is :" + new String(bytes));
            }
            //request
            while (headParseIndex < bytes.length && bytes[headParseIndex] != ' ') {
                if (bytes[headParseIndex] == '?') {
                    //hava reque para
                    index = parseRequestParam(bytes, header, headParseIndex);
                    break;
                }
                headParseIndex++;
            }
            if (headParseIndex > bytes.length) {
                throw new UnsupportedRequestMethodException("can't parse this header:" + new String(bytes));
            }
            // 解析url
            header.setUrl(new String(bytes, lastPosi, headParseIndex - lastPosi));

            log.debug("url:" + header.getUrl());

            lastPosi = (headParseIndex += index);

            while (headParseIndex < bytes.length && bytes[headParseIndex] != '\r') headParseIndex++;
            if (headParseIndex > bytes.length) {
                throw new UnsupportedRequestMethodException("can't parse this header:" + new String(bytes));
            }

            header.setProtocol(new String(bytes, lastPosi, headParseIndex - lastPosi));
            log.debug("protocol:" + header.getProtocol());
            return headParseIndex + 2;

        }

        /**
         * 解析请求参数
         */
        private static int parseRequestParam(byte[] bytes, SeHeader header, int headParseIndex) {
            //when have
            int index = headParseIndex, old;
            String key = null,
                    value = null;
            while (bytes[index] != ' ') {
                index++;
                old = index;
                while (bytes[index] != '&' && bytes[index] != ' ') {
                    if (bytes[index] == '=') {
                        key = new String(bytes, old, index - old);
                        old = index + 1;
                    }
                    index++;
                }
                try {
                    value = new String(bytes, old, index - old, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    log.error("parse url not support utf-8");
                }

                header.addRequestParameter(key, value);
                log.debug("parse url k:" + key + " \tv:" + value);
            }
            return index + 1;
        }


        public static int find(byte[] bytes, byte[] toFind, int start) {
            int index = bytes.length;
            // 找到指定字符的位置
            outer:
            for (int i = start; i < bytes.length; ++i) {

                for (int j = 0; j < toFind.length; ) {
                    if (bytes[i] == toFind[j]) {
                        ++i;
                        ++j;
                        if (j == toFind.length) {
                            index = i - toFind.length;
                            break outer;
                        }
                    } else {
                        i = i - j; // step back
                        break;
                    }
                }
            }
            return index;
        }
    }

}
