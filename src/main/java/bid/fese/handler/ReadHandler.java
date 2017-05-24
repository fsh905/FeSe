package bid.fese.handler;

import bid.fese.common.ApplicationContext;
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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;

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
//        log.debug("read len:" + readBytesLen + "\nread data:" + new String(attachment.array()));
        
        byte[] bytes = new byte[attachment.position()];
        attachment.flip();          //the length of position to limit
        attachment.get(bytes);
        attachment.clear();

        // 测试
        long t = System.currentTimeMillis();
        log.debug("start parse Header:" + t);

        // 头部长度
        int headerLenEnd = RequestHeaderHandler.find(bytes, Constants.HEADER_END, 10);
        // 读取头部信息
        SeHeader header = null;
        try {
             header = RequestHeaderHandler.parseHeader(bytes, headerLenEnd);
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
                log.debug("read completed\n the method is GET\n the len is:" + bytes.length + " the data is:\n" + new String(bytes));
                log.debug("-------------end--------------");
                //数据读取完毕, 进行下一阶段
                // header不需要inputStream
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
                    // post请求, header需要inputStream
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
                            log.debug("the data is:" + new String(buffer.array()));
                            log.debug("-------------end--------------");
                            // todo 这里是否使用copy
                            RequestHandlers.addRequest(new SeRequest(socketChannel, seHeader, buffer.array()));
                        }

                        @Override
                        public void failed(Throwable exc, SeHeader attachment) {
                            log.error("read error!", new String(buffer.array()), exc);
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

        /*if (readBytesLen == Constants.DEFAULT_UPLOAD_SIZE) {
            // 还有数据未读取
            Map<String, Object> readInfo = new HashMap<>();
            readInfo.put("bytes", bytes);
            readInfo.put("nowLen", Constants.DEFAULT_UPLOAD_SIZE);
            readInfo.put("header", header);

            socketChannel.read(attachment, readInfo, new CompletionHandler<Integer, Map<String, Object>>() {
                @Override
                public void completed(Integer readBytesLen, Map<String, Object> readInfo) {

                    byte[] data = (byte[]) readInfo.get("bytes");
                    int nowIndex = (int) readInfo.get("nowLen");

                    log.debug("remain size is:" + readBytesLen);
                    attachment.flip();
                    // 这样的效率远不如直接把头部解析出来
                    if (readBytesLen < Constants.DEFAULT_UPLOAD_SIZE) {
                        byte[] newBytes = new byte[nowIndex + readBytesLen];
                        System.arraycopy(data, 0, newBytes, 0, nowIndex);
                        attachment.get(newBytes, nowIndex, attachment.remaining());
                        // 处理完毕
                        log.debug("read large request completed, the len is:" + newBytes.length + " the data is:\n\n" + new String(newBytes));
                        log.debug("-------------end--------------");
                        RequestHandlers.addRequest(new SeRequest(socketChannel, header, data));
                        attachment.clear();
                    } else {
                        // 还有未读取得
                        if (nowIndex + readBytesLen > data.length) {
                            byte[] newBytes = new byte[nowIndex * 2];
                            System.arraycopy(data, 0, newBytes, 0, nowIndex);
                            data = newBytes;
                        }
                        attachment.get(data, nowIndex, attachment.remaining());
                        nowIndex += attachment.remaining();
                        attachment.clear();

                        readInfo.put("bytes", data);
                        readInfo.put("nowLen", nowIndex);
                        socketChannel.read(attachment, readInfo, this);
                    }
                }

                @Override
                public void failed(Throwable exc, Map<String, Object> readInfo) {
                    log.error("read input error, attempt to close connection; ", attachment, exc);
                    try {
                        socketChannel.close();
                    } catch (IOException e) {
                        log.error("close connection failed;");
                        e.printStackTrace();
                    }
                }
            });
        } else {


        }*/

    }

    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {
        // when read is fail
        log.error("read error!", new String(attachment.array()), exc);
    }
}
