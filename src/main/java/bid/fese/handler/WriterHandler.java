package bid.fese.handler;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.SocketChannel;

/**
 * Created by feng_ on 2016/12/8.
 * main to write the response to server
 */
public class WriterHandler implements CompletionHandler<Integer, AsynchronousSocketChannel> {
    @Override
    public void completed(Integer result, AsynchronousSocketChannel attachment) {

    }

    @Override
    public void failed(Throwable exc, AsynchronousSocketChannel attachment) {

    }
}
