package bid.fese.handler;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * Created by feng_ on 2016/12/8.
 * main to write the response to server
 */
public class WriterHandler implements CompletionHandler<AsynchronousSocketChannel,ByteBuffer>{
    @Override
    public void completed(AsynchronousSocketChannel result, ByteBuffer attachment) {

    }

    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {

    }
}
