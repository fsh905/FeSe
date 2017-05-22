package bid.fese.handler;

import bid.fese.exception.UnsupportedRequestMethodException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * Created by feng_ on 2016/12/8.
 * @TODO when recv a message this will be callback
 */
public class ReadHandler implements CompletionHandler<Integer,ByteBuffer>{
    private AsynchronousSocketChannel socketChannel;
    private static final Logger log = LogManager.getLogger(ReadHandler.class);
    public ReadHandler(AsynchronousSocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    @Override
    public void completed(Integer result, ByteBuffer attachment) {

        attachment.flip();          //the length of position to limit
        byte[] bytes = new byte[attachment.remaining()];
        attachment.get(bytes);
        attachment.clear();

        log.info("show the recv request:"+new String(bytes));

        String method = bytes[0] == 71 ? "GET" : "POST"; //when the header has been destroy , there will

        switch (bytes[0]){
            case 71 : method = "GET"; break;
            case 80 : method = "POST"; break;
            default:
                UnsupportedRequestMethodException exception = new UnsupportedRequestMethodException("the unsupported header is " + new String(bytes));
                        exception.printStackTrace();
        }

        HeaderHandler headerHandler = new HeaderHandler(bytes,method);
        headerHandler.parse();

        // read is complete
//        System.out.println(result);
//        System.out.println(attachment.position());
//        attachment.flip();
//        System.out.println(new String(attachment.array()));
//        attachment.clear();

        //if size > 2048
        //in the most case ,the header is small 8196,

        log.info("=====================================================");
    }

    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {
        // when read is fail
    }
}
