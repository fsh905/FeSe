package bid.fese.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Set;

/**
 * Created by feng_sh on 5/23/2017.
 * 请求
 * 不支持同名多参数请求：例如： ?name=feng&name=ming 不支持
 */
public class SeRequest {

    private static final Logger log = LoggerFactory.getLogger(SeRequest.class);
    private SeHeader header;
    private SeCookies cookies;
    private InStream inputStream;
    private AsynchronousSocketChannel socketChannel;
    private boolean isKeepAlive;
    private String remoteAddress;
    public SeRequest(AsynchronousSocketChannel socketChannel, SeHeader header, byte[] in, boolean isKeepAlive) {
        this.socketChannel = socketChannel;
        this.header = header;
        this.cookies = header.getCookies();
        this.isKeepAlive = isKeepAlive;
        try {
            this.remoteAddress = socketChannel.getRemoteAddress().toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 如果不符合Content-Type: application/x-www-form-urlencoded
        // 说明需要进行复杂的解析,暂不实现
        if (!parsePostData(in)) {
            inputStream = new InStream(in);
        } else {
            inputStream = null;
        }
    }

    public SeRequest(AsynchronousSocketChannel socketChannel, SeHeader header, boolean isKeepAlive) {
        this.socketChannel = socketChannel;
        this.inputStream = null;
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

    private boolean parsePostData(byte[] in) {
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
                    try {
                        value = new String(in, old, index - old, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        log.error("parse post param error, use utf-8");
                    }
                    log.debug("post param k:" + key + "- v:" + value);
                    header.addRequestParameter(key, value);
                    index++;
                }
            }
            return true;
        }
        return false;
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

    public enum METHOD {
        CONNECT,
        DELETE,
        GET,
        HEAD,
        PATCH, POST, PUT,
        TRACE
    }

    private class InStream extends InputStream {

        private byte[] in;
        private int nextChars;

        InStream(byte[] in) {
            this.in = in;
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
