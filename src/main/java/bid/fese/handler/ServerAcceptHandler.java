package bid.fese.handler;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.logging.Level;

/**
 * Created by feng_ on 2016/12/8.
 * when have the new client connect this server,it will be invoke
 * accept new connect
 */
public class ServerAcceptHandler implements CompletionHandler<AsynchronousSocketChannel,AsynchronousServerSocketChannel> {

    private static final Logger log = LogManager.getLogger(ServerAcceptHandler.class);

    /**
     *
     * @param result socketChannel, 新的连接
     * @param attachment socketServer， 服务器
     */
    @Override
    public void completed(AsynchronousSocketChannel result, AsynchronousServerSocketChannel attachment) {
        // carry on accept new client
        attachment.accept(attachment,this);
        try {
            log.info("a new connection establish;" + result.getRemoteAddress());
        } catch (IOException e) {
            log.error("get connection info error;", e);
            e.printStackTrace();
        }

        //in apache & tomcat the header size is 8196
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 8);
        // handler the message
        // first buf is read into it
        // second : when read is complete,it will be trans into the readhandler
        // readhandle need a socket channel to do other things
        // 这里将result进行包装， 便于后面读取
        // 后面的更改将会转向buffer分支， 对io包下面的进行实现
        result.read(byteBuffer,byteBuffer,new ReadHandler(result));

    }

    @Override
    public void failed(Throwable exc, AsynchronousServerSocketChannel attachment) {
        log.error("connect error;", exc);
        exc.printStackTrace();
    }
}
