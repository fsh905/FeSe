package bid.fese.handler;

import bid.fese.common.Constants;
import bid.fese.entity.SeResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;

/**
 * Created by feng_sh on 6/2/2017.
 * 输出response
 */
public class WriteHandler {

    private static final Logger logger = LogManager.getLogger(WriteHandler.class);

    private AsynchronousSocketChannel socketChannel;
    private SeResponse response;
    private String remoteAddress;

    public WriteHandler(AsynchronousSocketChannel socketChannel, SeResponse response, String remoteAddress) {
        this.socketChannel = socketChannel;
        this.response = response;
        this.remoteAddress = remoteAddress;
    }

    /**
     * 发送响应
     * @param headerBytes 头部字段
     * @param bodyBytes body字段
     */
    public void sendResponse(byte[] headerBytes, byte[] bodyBytes, int len) {
        logger.debug("len is:" + len);
        // 判断长度
        if (len < Constants.DEFAULT_UPLOAD_SIZE) {
            logger.debug("use direct");
            ByteBuffer byteBuffer = ByteBuffer.allocate(headerBytes.length + len);
            byteBuffer.put(headerBytes, 0, headerBytes.length);
            byteBuffer.put(bodyBytes, 0, len);
            byteBuffer.rewind();
            logger.debug("copy done");
            socketChannel.write(byteBuffer, socketChannel, new WriteBodyHandler());
        } else {
            logger.debug("use split");
            socketChannel.write(ByteBuffer.wrap(headerBytes), bodyBytes, new WriteHeaderHandler());
        }
    }


    private class WriteHeaderHandler implements CompletionHandler<Integer, byte[]> {
        @Override
        public void completed(Integer result, byte[] attachment) {
            logger.info("response header success: -" + remoteAddress);
            socketChannel.write(ByteBuffer.wrap(attachment), socketChannel, new WriteBodyHandler());
        }

        @Override
        public void failed(Throwable exc, byte[] attachment) {
            if (exc instanceof ClosedChannelException) {
                logger.error("socket already closed, send header error -" + remoteAddress);
                return;
            }
            logger.error("response header error -" + remoteAddress, attachment, exc);
        }
    }

    private class WriteBodyHandler implements CompletionHandler<Integer, AsynchronousSocketChannel> {
        @Override
        public void completed(Integer result, AsynchronousSocketChannel socketChannel) {
            logger.info("response success: -" + remoteAddress);
            // 如果设置keepAlive
            if (socketChannel.isOpen() && !response.isKeepAlive()) {
                logger.debug("close socket" + remoteAddress);
                try {
                    socketChannel.close();
                } catch (IOException e) {
                    logger.error("close response error -" + remoteAddress);
                }
            } else {
                logger.debug("already set keep-alive, this socket not need close");
            }
        }

        @Override
        public void failed(Throwable exc, AsynchronousSocketChannel attachment) {
            if (exc instanceof ClosedChannelException) {
                logger.error("socket already closed -" + remoteAddress);
                return;
            }
            logger.error("response error -" + remoteAddress, attachment, exc);
        }
    }

}
