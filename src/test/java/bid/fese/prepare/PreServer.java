package bid.fese.prepare;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketOption;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

/**
 * Created by feng_ on 2016/11/24.
 */
public class PreServer {

    public static void main(String[] args) {
        PreServer server = new PreServer();
        server.startServer(8080);
    }

    public void startServer(int port) {
        if (port == 0)
            return;
        //创建新的服务器
        AsynchronousServerSocketChannel serverSocketChannel = null;
        try {
            serverSocketChannel = AsynchronousServerSocketChannel.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //监听
        try {
            serverSocketChannel.bind(new InetSocketAddress(port));
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(new ServerHandler(serverSocketChannel)).start();
        System.out.println("server start!");
    }

    private class ServerHandler implements Runnable {
        AsynchronousServerSocketChannel serverSocketChannel;
        CountDownLatch latch;

        public ServerHandler(AsynchronousServerSocketChannel serverSocketChannel) {
            this.serverSocketChannel = serverSocketChannel;
        }

        @Override
        public void run() {
            //防止因空等待退出   相当于加了个while
            latch = new CountDownLatch(1);
            serverSocketChannel.accept(this, new AccpetServerHandler());
            try {
                latch.await();
            } catch (InterruptedException e) {
                System.out.println("server exit ; ");
                e.printStackTrace();
            }
            System.out.println("server stop");
        }
    }

    /**
     * 专门用来处理新客户端连接
     */
    private class AccpetServerHandler implements CompletionHandler<AsynchronousSocketChannel, ServerHandler> {

        @Override
        public void completed(AsynchronousSocketChannel channel, ServerHandler attachment) {
            System.out.println("new client!");
            //等待其他客户端请求 先进先出?
            attachment.serverSocketChannel.accept(attachment, this);
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);// dst   attachment(附件)   handler
            channel.read(byteBuffer, byteBuffer, new ReadCompletionHandler(channel));

        }

        @Override
        public void failed(Throwable exc, ServerHandler attachment) {
            //断开连接
            exc.printStackTrace();
            attachment.latch.countDown();
        }
    }

    private class ReadCompletionHandler implements CompletionHandler<Integer, ByteBuffer> {
        //用于读取半包 和 返回消息
        AsynchronousSocketChannel channel;

        public ReadCompletionHandler(AsynchronousSocketChannel channel) {
            this.channel = channel;
        }

        @Override
        public void completed(Integer result, ByteBuffer attachment) {
            //继续监听
            channel.read(attachment, attachment, this);

            attachment.flip();
            //清除空闲
            byte[] bytes = new byte[attachment.remaining()];
            attachment.get(bytes);
            //clear buffer
            attachment.clear();
            String req = new String(bytes);
            System.out.println("recv data : " + req);
            Stream.of(req.split("\n")).forEach(s -> System.out.println(s));
            System.out.println("---end---");
            SocketOption<Boolean> socketOption = StandardSocketOptions.SO_KEEPALIVE;
            try {
                channel.setOption(socketOption, true);
            } catch (IOException e) {
                e.printStackTrace();
            }

            new WriteCompletionHandler(channel).doWrite("<h1>hello world</h1>");

        }

        @Override
        public void failed(Throwable exc, ByteBuffer attachment) {
            exc.printStackTrace();
        }
    }

    private class ReuqestHandler {
        private String body;
        private AsynchronousSocketChannel channel;

        public ReuqestHandler(String body, AsynchronousSocketChannel channel) {
            this.body = body;
            this.channel = channel;
        }

        public void invoke() {

        }

    }

    private class WriteCompletionHandler {
        private AsynchronousSocketChannel channel;

        public WriteCompletionHandler(AsynchronousSocketChannel channel) {
            this.channel = channel;
        }

        public void doWrite(String msg) {
            String response = "HTTP/1.1 200 OK\n" +
                    "Server: FeSe/1.1\n" +
                    "Cache-Control: no-cache, no-store\n" +
                    "Content-Type: text/html;charset=UTF-8\n" +
                    "Content-Length: " + msg.getBytes().length + "\n\n"; //must has two \n
            response += msg;
            System.out.println(response);
            byte[] bytes = response.getBytes();
            ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
            byteBuffer.put(bytes);
            byteBuffer.flip();
            channel.write(byteBuffer, byteBuffer, new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    if (attachment.hasRemaining())
//                        防止一次性不能传送完毕
                        channel.write(attachment, attachment, this);
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    exc.printStackTrace();
                }
            });
            System.out.println(channel.isOpen());
        }

    }

}
