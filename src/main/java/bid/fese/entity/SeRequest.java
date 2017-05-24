package bid.fese.entity;

import bid.fese.common.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * Created by feng_sh on 5/23/2017.
 * 请求
 */
public class SeRequest {

    public enum METHOD {
        CONNECT,
        DELETE,
        GET,
        HEAD,
        PATCH, POST, PUT,
        TRACE}

    private static final Logger log = LogManager.getLogger(SeRequest.class);

    private SeHeader header;
    private SeCookies cookies;
    private byte[] in;
    private InputStream inputStream;
    private AsynchronousSocketChannel socketChannel;

    public SeRequest(AsynchronousSocketChannel socketChannel, SeHeader header, byte[] in) {
        this.socketChannel = socketChannel;
        this.in = in;
        this.header = header;
        this.cookies = header.getCookies();
    }
    public SeRequest(AsynchronousSocketChannel socketChannel, SeHeader header) {
        this.socketChannel = socketChannel;
        this.in = null;
        this.header = header;
        this.cookies = header.getCookies();
    }

    AsynchronousSocketChannel getSocketChannel() {
        return socketChannel;
    }

    public SeHeader getHeader() {
        return header;
    }

    public void setHeader(SeHeader header) {
        this.header = header;
    }

    public SeCookies getCookies() {
        return cookies;
    }

    public void setCookies(SeCookies cookies) {
        this.cookies = cookies;
    }

    public InputStream getInputStream() {
        if (in == null) {
            return null;
        }
        if (inputStream == null) {
            inputStream = new InStream();
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

        InStream() {
            this.in = SeRequest.this.in;
            nextChars = 0;
        }


        @Override
        public int read() throws IOException {
            if (nextChars < in.length) {
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
