package bid.fese.handler;


import bid.fese.common.ApplicationContext;
import bid.fese.common.Constants;
import bid.fese.entity.SeRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * Created by feng_ on 2016/12/8.
 * when have the new client connect this server,it will be invoke
 * accept new connect
 */
public class ServerAcceptHandler implements CompletionHandler<AsynchronousSocketChannel,AsynchronousServerSocketChannel> {

    private static final Logger log = LogManager.getLogger(ServerAcceptHandler.class);

    /**
     *
     * @param socketChannel socketChannel, 新的连接
     * @param attachment socketServer， 服务器
     */
    @Override
    public void completed(AsynchronousSocketChannel socketChannel, AsynchronousServerSocketChannel attachment) {
        // 继续接收其他请求
        attachment.accept(attachment,this);
        try {
            log.info("a new connection establish;" + socketChannel.getRemoteAddress());
        } catch (IOException e) {
            log.error("get connection info error;", e);
            e.printStackTrace();
        }

        // in apache & tomcat the header size is 8196
         ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 8);
        // handler the message
        // first buf is read into it
        // second : when read is complete,it will be trans into the readhandler
        // readhandle need a socket channel to do other things

        socketChannel.read(byteBuffer,byteBuffer,new ReadHandler(socketChannel));
    }

    @Override
    public void failed(Throwable exc, AsynchronousServerSocketChannel attachment) {
        log.error("connect error;", exc);
        exc.printStackTrace();
    }
}
