package bid.fese.handler;

import bid.fese.common.Constants;
import bid.fese.entity.SeHeader;
import bid.fese.entity.SeRequest;
import bid.fese.exception.UnsupportedRequestMethodException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * Created by feng_ on 2016/12/8.
 * 如何处理 当请求为post方法时
 */
public class ReadHandler implements CompletionHandler<Integer,ByteBuffer>{
    private AsynchronousSocketChannel socketChannel;
    private static final Logger log = LogManager.getLogger(ReadHandler.class);

    public ReadHandler(AsynchronousSocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }
    @Override
    public void completed(Integer readBytesLen, ByteBuffer attachment) {
        log.debug("read len:" + readBytesLen + "\n");
        if (readBytesLen <= 0) {
            log.error("the request len is 0, attempt to close;");
            try {
                socketChannel.shutdownInput();
                socketChannel.shutdownOutput();
            } catch (IOException e) {
                log.error("close failed!", e);
            } finally {
                try {
                    socketChannel.close();
                } catch (IOException e) {
                    log.error("close socket channel failed!", e);
                }
            }
            return;
        }
        byte[] bytes = new byte[readBytesLen];
        attachment.flip();          //the length of position to limit
        attachment.get(bytes);
        attachment.clear();

        // 测试
        long t = System.currentTimeMillis();
        log.debug("start parse Header:" + t);

        // 头部长度
        int headerLenEnd = RequestHeaderParser.find(bytes, Constants.HEADER_END, 10);

/*
        // 头部过长
        if (headerLenEnd == bytes.length) {
            
        }
*/
        // 读取头部信息
        SeHeader header = null;
        try {
             header = RequestHeaderParser.parseHeader(bytes, headerLenEnd);
        } catch (UnsupportedRequestMethodException e) {
            log.error("parse header error", e);
        }

        log.debug("end parse Header; use time:" + (System.currentTimeMillis() - t));

        if (header == null) {
            try {
                socketChannel.shutdownInput();
            } catch (IOException e) {
                log.error("close input error", e);
            }
            return;
        }

        switch (header.getMethod()) {
            case GET: {

                log.debug("read completed\n the method is GET\n the len is:" + bytes.length);
                log.debug("-------------end--------------");
                //数据读取完毕, 进行下一阶段
                // request不需要inputStream
                RequestHandlers.addRequest(new SeRequest(socketChannel, header));
            } break;
            case POST: {
                int contentLen = Integer.parseInt(header.getHeaderParameter(Constants.CONTENT_LENGTH));
                if (contentLen <= Constants.DEFAULT_UPLOAD_SIZE - headerLenEnd + 4) {
                    // 长度合适
                    byte[] data = new byte[contentLen];
                    System.arraycopy(bytes, headerLenEnd + 4, data, 0, contentLen);
                    log.debug("post recv data:" + new String(data));
                    //数据读取完毕, 进行下一阶段
                    // post请求, request 实现inputStream需要byte
                    RequestHandlers.addRequest(new SeRequest(socketChannel, header, data));
                } else {
                    // 长度太长, 进行第二次读取
                    ByteBuffer buffer = ByteBuffer.allocate(contentLen);
                    buffer.put(bytes, headerLenEnd + 4, bytes.length - headerLenEnd - 4);
                    socketChannel.read(buffer, header, new CompletionHandler<Integer, SeHeader>() {
                        @Override
                        public void completed(Integer readLen, SeHeader seHeader) {
                            log.debug("buffer remain:" + buffer.position());
                            log.debug("read large request completed\n the len is:" + contentLen);
                            log.debug("-------------end--------------");
                            // todo 这里是否使用copy
                            RequestHandlers.addRequest(new SeRequest(socketChannel, seHeader, buffer.array()));
                        }

                        @Override
                        public void failed(Throwable exc, SeHeader attachment) {
                            log.error("read error!", new String(buffer.array()), exc);
                            // 关闭
                        }
                    });
                }
            } break;
            default: {
                log.error("this method is not support!");
                try {
                    socketChannel.shutdownInput();
                } catch (IOException e) {
                    log.error("close input error;", e);
                }
            }
        }
    }

    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {
        // when read is fail
        log.error("read error!", new String(attachment.array()), exc);
    }

    private static class RequestHeaderParser {

        public static SeHeader parseHeader(byte[] bytes, int end) throws UnsupportedRequestMethodException {
            SeHeader header = new SeHeader();
            parse(bytes, header, 0, end);
            return header;
        }

        private static void parse(byte[] bytes, SeHeader header, int headParseIndex, int headEndIndex) throws UnsupportedRequestMethodException {

            int index = requestLineParse(bytes, header, headParseIndex);
            headParseIndex += index;

            String key,value;
            while (headParseIndex < headEndIndex) {
                while (bytes[headParseIndex] != ':'){
                    headParseIndex ++;
                }
                key = new String(bytes,index,headParseIndex-index);
                index = headParseIndex += 2;
                while (bytes[headParseIndex] != '\r'){
                    headParseIndex ++;
                }
                value = new String(bytes,index,headParseIndex-index);
                index = headParseIndex += 2;
                header.addHeaderParameter(key,value);

                log.info("key:"+key+"-\tvalue:"+value);
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
                case 67 : header.setMethod(SeRequest.METHOD.CONNECT); headParseIndex += 8; break;
                case 68 : header.setMethod(SeRequest.METHOD.DELETE); headParseIndex += 7; break;
                case 71 : header.setMethod(SeRequest.METHOD.GET); headParseIndex += 4; break;
                case 72 : header.setMethod(SeRequest.METHOD.HEAD); headParseIndex += 5; break;
                case 80 :
                    switch (bytes[headParseIndex + 1]) {
                        case 65 : header.setMethod(SeRequest.METHOD.PATCH); headParseIndex += 6; break;
                        case 79 : header.setMethod(SeRequest.METHOD.POST); headParseIndex += 5; break;
                        case 85 : header.setMethod(SeRequest.METHOD.PUT); headParseIndex += 4; break;
                        default: throw new UnsupportedRequestMethodException("");
                    }
                    break;
                case 84 : header.setMethod(SeRequest.METHOD.TRACE); headParseIndex += 6; break;
                default: throw new UnsupportedRequestMethodException("");
            }

            log.debug("method:" + header.getMethod());
/*
        while (bytes[headParseIndex] != ' '){
            headParseIndex++;
        }
*/
//        headParseIndex += 1;
            int lastPosi = headParseIndex;
            int index = 0;
            //request
            while (bytes[headParseIndex] != ' ') {
                if (bytes[headParseIndex] == '?'){
                    //hava reque para
                    index = parseRequestParam(bytes, header, headParseIndex);
                    break;
                }
                headParseIndex ++;
            }
            // 解析url
            header.setUrl(new String(bytes,lastPosi,headParseIndex-lastPosi));

            log.debug("url:"+header.getUrl());

            lastPosi = (headParseIndex += index) ;

            while (bytes[headParseIndex] != '\r') headParseIndex ++;

            header.setProtocol(new String(bytes,lastPosi,headParseIndex-lastPosi));
            log.debug("protocol:"+header.getProtocol());
            return headParseIndex + 2;

        }

        /**
         * 解析请求参数
         */
        private static int parseRequestParam(byte[] bytes, SeHeader header, int headParseIndex){
            //when have
            int index = headParseIndex,old;
            String key=null,
                    value=null;
            while (bytes[index] != ' '){
                index ++;
                old = index;
                while (bytes[index] != '&' && bytes[index] != ' '){
                    if (bytes[index] =='='){
                        key = new String(bytes,old,index-old);
                        old = index + 1;
                    }
                    index ++;
                }
                value = new String(bytes,old,index-old);

                header.addRequestParameter(key,value);
                log.debug("k:"+key+" \tv:"+value);
            }
            return index + 1;
        }


        public static int find(byte[] bytes, byte[] toFind, int start) {
            int index = bytes.length;
            // 找到指定字符的位置
            outer: for (int i = start; i < bytes.length; ++i) {

                for (int j = 0; j < toFind.length;) {
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
