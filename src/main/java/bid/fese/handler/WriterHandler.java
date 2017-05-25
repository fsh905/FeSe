package bid.fese.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.SocketChannel;

/**
 * Created by feng_ on 2016/12/8.
 * main to write the response to server
 */
public class WriterHandler implements CompletionHandler<Integer, AsynchronousSocketChannel> {

    private final static Logger logger = LogManager.getLogger(WriterHandler.class);

    @Override
    public void completed(Integer result, AsynchronousSocketChannel socketChannel) {
        logger.debug("write success!");
        logger.debug("attempt to close");
        try {
            socketChannel.shutdownOutput();
//            socketChannel.close();
            logger.debug("close success");
        } catch (IOException e) {
            logger.error("close socketChannel", e);
        }
    }

    @Override
    public void failed(Throwable exc, AsynchronousSocketChannel attachment) {
        logger.error("write error", attachment, exc);
    }
}
