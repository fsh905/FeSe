package bid.fese.prepare;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

/**
 * Created by feng_sh on 17-5-26.
 */
public class RequestTest {
    static CountDownLatch latch = new CountDownLatch(3);

    public static void main(String[] args) throws IOException {
        server();
    }

    public static void server() throws IOException {
        new Thread() {
            @Override
            public void run() {

                try {
                    AsynchronousServerSocketChannel serverSocketChannel = AsynchronousServerSocketChannel.open();
                    serverSocketChannel.bind(new InetSocketAddress("0.0.0.0", 8888));

                    serverSocketChannel.accept(serverSocketChannel, new CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel>() {
                        @Override
                        public void completed(AsynchronousSocketChannel result, AsynchronousServerSocketChannel attachment) {
                            attachment.accept(attachment, this);
                            try {
                                result.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 8);
                            result.read(byteBuffer, result, new CompletionHandler<Integer, AsynchronousSocketChannel>() {
                                @Override
                                public void completed(Integer result, AsynchronousSocketChannel attachment) {
                                    if (result == -1) {
                                        try {
                                            System.out.println("close");
                                            attachment.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        latch.countDown();
                                        latch.countDown();
                                        latch.countDown();
                                        return;
                                    }
                                    latch.countDown();

                                    System.out.println("result len:" + result);
                                    byteBuffer.clear();
                                    attachment.read(byteBuffer, attachment, this);
                                    System.out.println("ready");
                                }

                                @Override
                                public void failed(Throwable exc, AsynchronousSocketChannel attachment) {
                                    System.out.println("failed");
                                }
                            });
                        }

                        @Override
                        public void failed(Throwable exc, AsynchronousServerSocketChannel attachment) {

                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();


    }

    @Test
    public void request() throws IOException {

    }

}
