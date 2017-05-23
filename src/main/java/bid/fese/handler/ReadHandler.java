package bid.fese.handler;

import bid.fese.common.ApplicationContext;
import bid.fese.common.Constants;
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
        log.debug("read len:" + readBytesLen + "\nread data:" + new String(attachment.array()));
        attachment.flip();          //the length of position to limit
        if (readBytesLen == Constants.DEFAULT_UPLOAD_SIZE) {
            // 还有数据未读取
            byte[] bytes = new byte[readBytesLen * 2];
            attachment.get(bytes, 0, attachment.remaining());
            attachment.clear();

            Map<String, Object> readInfo = new HashMap<>();
            readInfo.put("bytes", bytes);
            readInfo.put("nowLen", Constants.DEFAULT_UPLOAD_SIZE);

            socketChannel.read(attachment, readInfo, new CompletionHandler<Integer, Map<String, Object>>() {
                @Override
                public void completed(Integer readBytesLen, Map<String, Object> readInfo) {

                    byte[] data = (byte[]) readInfo.get("bytes");
                    int nowIndex = (int) readInfo.get("nowLen");

                    log.debug("remain size is:" + readBytesLen);
                    attachment.flip();
                    if (readBytesLen < Constants.DEFAULT_UPLOAD_SIZE) {
                        byte[] newBytes = new byte[nowIndex + readBytesLen];
                        System.arraycopy(data, 0, newBytes, 0, nowIndex);
                        attachment.get(newBytes, nowIndex, attachment.remaining());
                        // 处理完毕
                        log.debug("read large request completed, the len is:" + newBytes.length + " the data is:\n\n" + new String(newBytes));
                        log.debug("-------------end--------------");
                        RequestHandlers.addRequest(new SeRequest(socketChannel, data));
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
            byte[] bytes = new byte[attachment.remaining()];
            attachment.get(bytes);
            attachment.clear();
            log.debug("read completed, the len is:" + bytes.length + " the data is:\n\n" + new String(bytes));
            log.debug("-------------end--------------");

            //数据读取完毕, 进行下一阶段
            RequestHandlers.addRequest(new SeRequest(socketChannel, bytes));
        }

    }

    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {
        // when read is fail
        log.error("read error!", new String(attachment.array()), exc);
    }
}
