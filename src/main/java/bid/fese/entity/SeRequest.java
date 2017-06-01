package bid.fese.entity;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Set;

/**
 * Created by feng_sh on 5/23/2017.
 * 请求
 * 不支持同名多参数请求：例如： ?name=feng&name=ming 不支持
 */
public class SeRequest {

    public enum METHOD {
        CONNECT,
        DELETE,
        GET,
        HEAD,
        PATCH, POST, PUT,
        TRACE
    }

//    private static final Logger log = LogManager.getLogger(SeRequest.class);

    private SeHeader header;
    private SeCookies cookies;
    private byte[] in;
    private InStream inputStream;
    private AsynchronousSocketChannel socketChannel;
    private boolean isKeepAlive;
    private String remoteAddress;

    public SeRequest(AsynchronousSocketChannel socketChannel, SeHeader header, byte[] in, boolean isKeepAlive) {
        this.socketChannel = socketChannel;
        this.in = in;
        this.header = header;
        this.cookies = header.getCookies();
        this.isKeepAlive = isKeepAlive;
        try {
            this.remoteAddress = socketChannel.getRemoteAddress().toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        parsePostData();
    }

    public SeRequest(AsynchronousSocketChannel socketChannel, SeHeader header, boolean isKeepAlive) {
        this.socketChannel = socketChannel;
        this.in = null;
        this.header = header;
        this.cookies = header.getCookies();
        this.isKeepAlive = isKeepAlive;
        try {
            this.remoteAddress = socketChannel.getRemoteAddress().toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public boolean isKeepAlive() {
        return isKeepAlive;
    }

    public String getUrl() {
        return header.getUrl();
    }

    public String getParameter(String name) {
        return header.getRequestParameter(name);
    }

    public Set<String> getParameterNames() {
        return header.getRequestParameters().keySet();
    }

    public METHOD getMethod() {
        return header.getMethod();
    }

    private void parsePostData() {
        if (header.getHeaderParameter(SeHeader.CONTENT_TYPE) != null
                && header.getHeaderParameter(SeHeader.CONTENT_TYPE).equals(SeHeader.X_WWW_FORM_URLENCODED)) {
            // Content-Type: application/x-www-form-urlencoded
            // 可以解析
            if (in.length > 2) {
                int index = 0;
                int old = 0;
                String key = null;
                String value = null;
                while (index < in.length) {
                    old = index;
                    while (index < in.length && in[index] != '&') {
                        if (in[index] == '=') {
                            key = new String(in, old, index - old);
                            old = index + 1;
                        }
                        index++;
                    }
                    value = new String(in, old, index - old);
                    header.addRequestParameter(key, value);
                    index++;
                }
            }
        }
    }

    /**
     * 可以先获取header再调用
     *
     * @param name 参数名
     * @return 值 或者 null
     */
    public String getHeaderParameter(String name) {
        return header.getHeaderParameter(name);
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
     *
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

    private class InStream extends InputStream {

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
