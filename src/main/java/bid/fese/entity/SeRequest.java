package bid.fese.entity;

import bid.fese.common.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * Created by feng_sh on 5/23/2017.
 * 请求
 */
public class SeRequest {

    private static final Logger log = LogManager.getLogger(SeRequest.class);

    private SeHeader header;
    private SeCookies cookies;
    private int headerLen = -1;
    private byte[] in;
    private InputStream inputStream;
    private AsynchronousSocketChannel socketChannel;

    public SeRequest(AsynchronousSocketChannel socketChannel, byte[] in) {
        this.socketChannel = socketChannel;
        this.in = in;
    }

/*    private synchronized void getIn() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(Constants.DEFAULT_UPLOAD_SIZE);
        status = RequestStatus.READING;
        socketChannel.read(byteBuffer, 0, new CompletionHandler<Integer, Integer>() {
            @Override
            public void completed(Integer result, Integer attachment) {
                byteBuffer.flip();
                if (in == null) {
                    in = byteBuffer.array();
                    // get method or post length < 1024 * 8
                    if (in.length < Constants.DEFAULT_UPLOAD_SIZE) {
                        log.debug("connection read finished; len is:" + in.length);
                        SeRequest.this.status = RequestStatus.FINISHED;
//                        SeRequest.this.notifyAll();
                        return;
                    }
                } else {
                    // 长度非常长
                    // todo 这里最好增加一个最大长度限制
                    if (attachment > in.length) {
                        byte[] newIn = new byte[in.length * 2];
                        System.arraycopy(in, 0, newIn, 0, in.length);
                        byteBuffer.get(newIn, in.length, attachment - in.length);
                        in = newIn;
                    }
                }
                if (result != -1) {
                    SeRequest.this.status = RequestStatus.READING;
                    byteBuffer.clear();
                    socketChannel.read(byteBuffer, attachment + result, this);
                } else {
                    log.debug("connection read finished; len is:" + in.length);
                    SeRequest.this.status = RequestStatus.FINISHED;
//                    SeRequest.this.notifyAll();
                }
            }

            @Override
            public void failed(Throwable exc, Integer attachment) {
                log.error("read error,", socketChannel, exc);
            }
        });
    }*/

    public InputStream getInputStream() {
        if (inputStream == null) {
            if (headerLen == -1) {
                // getHeader()
            }
            inputStream = new InStream(headerLen);
        }
        return inputStream;
    }

    /**
     * 通过socketChannel的hashCode来判断是否为同一个连接
     * @return hashCode
     */
    @Override
    public int hashCode() {
        return socketChannel.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj.hashCode() == hashCode();
    }

    private class InStream extends InputStream{

        private byte[] in;
        private int nextChars;
        private int nChars;

        InStream(int start) {
            this.in = SeRequest.this.in;
            nextChars = start;
            nChars = in.length;
        }


        @Override
        public int read() throws IOException {
            if (nextChars < nChars) {
                return in[nextChars++];
            }
            return -1;
        }

        @Override
        public synchronized void reset() throws IOException {
            nextChars = 0;
        }
    }

}
