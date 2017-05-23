package bid.fese;

import bid.fese.handler.ServerAcceptHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.logging.Level;

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
            serverSocketChannel.accept(serverSocketChannel,new ServerAcceptHandler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
