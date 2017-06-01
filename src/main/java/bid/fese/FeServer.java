package bid.fese;

import bid.fese.common.Constants;
import bid.fese.handler.ReadHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * Created by feng_ on 2016/12/8.
 * boot server class
 */
public class FeServer implements Runnable{

    private final Logger logger = LogManager.getLogger(FeServer.class);
    private int port;

    FeServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            AsynchronousServerSocketChannel serverSocketChannel = AsynchronousServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress("0.0.0.0",port));
            logger.info("Server start in " + port);
            //链接过来， 生成一个handler
            serverSocketChannel.accept(serverSocketChannel, new CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel>() {
                @Override
                public void completed(AsynchronousSocketChannel socketChannel, AsynchronousServerSocketChannel attachment) {
                    // 继续接收其他请求
                    serverSocketChannel.accept(attachment,this);

                    try {
                        logger.info("-------------------------------------------------------------");
                        logger.info("a new connection establish;" + socketChannel.getRemoteAddress());
                    } catch (IOException e) {
                        logger.error("get connection info error;", e);
                        e.printStackTrace();
                    }
                    // 默认
                    ByteBuffer byteBuffer = ByteBuffer.allocate(Constants.DEFAULT_UPLOAD_SIZE);
                    socketChannel.read(byteBuffer,byteBuffer,new ReadHandler(socketChannel));
                }

                @Override
                public void failed(Throwable exc, AsynchronousServerSocketChannel attachment) {
                    logger.error("accept error;", exc);
                }
            });
        } catch (IOException e) {
            logger.error("connect error;", e);

        }
    }
}
