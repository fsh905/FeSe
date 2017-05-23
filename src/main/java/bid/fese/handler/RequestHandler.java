package bid.fese.handler;

import bid.fese.exception.UnsupportedRequestMethodException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * Created by feng_ on 2016/12/8.
 * 如何处理 当请求为post方法时
 */
public class RequestHandler implements CompletionHandler<Integer,ByteBuffer>{
    private AsynchronousSocketChannel socketChannel;
    private static final Logger log = LogManager.getLogger(RequestHandler.class);
    public RequestHandler(AsynchronousSocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    @Override
    public void completed(Integer result, ByteBuffer attachment) {

        attachment.flip();          //the length of position to limit
        byte[] bytes = new byte[attachment.remaining()];
        attachment.get(bytes);
        attachment.clear();

        log.info("show the recv request:"+new String(bytes));

        String method = "";

        switch (bytes[0]){
            case 71 : method = "GET"; break;
            case 80 : method = "POST"; break;
            default:
                UnsupportedRequestMethodException exception = new UnsupportedRequestMethodException("the unsupported header is " + new String(bytes));
                        exception.printStackTrace();
        }

        RequestHeaderHandler headerHandler = new RequestHeaderHandler(bytes,method);
        headerHandler.parse();

        log.info("=====================================================");

        // 等待客户端发送
        socketChannel.read(attachment, attachment, this);

    }

    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {
        // when read is fail
    }
}
