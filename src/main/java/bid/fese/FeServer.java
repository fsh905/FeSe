package bid.fese;

import bid.fese.common.Constants;
import bid.fese.handler.ReadHandler;
import bid.fese.handler.RequestHandler;
import bid.fese.handler.RequestHandlers;
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
public class FeServer implements Runnable {

    private final Logger logger = LogManager.getLogger(FeServer.class);
    private final int port;
    private final RequestHandlers handlers;

    FeServer(int port, RequestHandlers handlers) {
        this.port = port;
        this.handlers = handlers;
    }

    @Override
    public void run() {
        AsynchronousServerSocketChannel serverSocketChannel = null;
        logger.info("Server start in " + port);
        try {
            serverSocketChannel = AsynchronousServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress("0.0.0.0", port));
        } catch (IOException e) {
            logger.error("socket server start error", e);
        }
        if (serverSocketChannel == null) {
            return;
        }
        //链接过来， 生成一个handler
        serverSocketChannel.accept(serverSocketChannel, new CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel>() {
            @Override
            public void completed(AsynchronousSocketChannel socketChannel, AsynchronousServerSocketChannel attachment) {
                // 继续接收其他请求
                attachment.accept(attachment, this);

                try {
                    logger.info("-------------------------------------------------------------");
                    logger.info("a new connection establish;" + socketChannel.getRemoteAddress());
                } catch (IOException e) {
                    logger.error("get connection info error;", e);
                    e.printStackTrace();
                }
                // 默认
                ByteBuffer byteBuffer = ByteBuffer.allocate(Constants.DEFAULT_UPLOAD_SIZE);
                // 建立连接后15s之内无数据传输则关闭连接
                socketChannel.read(byteBuffer,
                        Constants.DEFAULT_KEEP_ALIVE_TIME,
                        Constants.DEFAULT_KEEP_ALIVE_TIME_UNIT,
                        byteBuffer,
                        new ReadHandler(socketChannel, handlers));
            }

            @Override
            public void failed(Throwable exc, AsynchronousServerSocketChannel attachment) {
                logger.error("accept connection error;", exc);
            }
        });
    }
}
