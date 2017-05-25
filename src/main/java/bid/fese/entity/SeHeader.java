package bid.fese.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by feng_ on 2016/12/8.
 * request header and response header
 */
public class SeHeader {

    public static final String OK_200 = "HTTP/1.1 200 OK";
    public static final String NEWLINE = "\r\n";
    public static final String NOT_FOUND_404 = "HTTP/1.1 404 Not Found";
    public static final String SERVER_ERROR_500 = "HTTP/1.1 500 Internal Server Error";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String CONNECTION = "Connection";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String KEEP_ALIVE = "keep-alive";
    public static final String CONTENT_ENCODING = "Content-Encoding";
    public static final String ACCEPT_ENCODING = "Accept-Encoding";
    public static final String LAST_MODIFIED = "Last-Modified";
    public static final String GZIP = "gzip";
    public static final String SERVER = "Server";
    public static final String COOKIES = "Cookie";
    public static final String SET_COOKIE = "Set-Cookie";



    private SeRequest.METHOD method;
    private String status;
    private String url;
    private String protocol;
    private Map<String, String> requestParameters;
    private Map<String, String> headerParameters;
    private SeCookies cookies;

    public SeHeader() {
        this.requestParameters = new HashMap<>();
        this.headerParameters = new HashMap<>();
        this.status = OK_200;

    }

    public void clear() {
        requestParameters.clear();
        headerParameters.clear();
        // cookies不变
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setContentLength(int length) {
        headerParameters.put(CONTENT_LENGTH, String.valueOf(length));
    }

    public void setContentEncoding(String contentEncoding) {
        headerParameters.put(CONTENT_ENCODING, contentEncoding);
    }

    public void setContentType(String contentType) {
        headerParameters.put(CONTENT_TYPE, contentType);
    }

    public SeRequest.METHOD getMethod() {
        return method;
    }

    public void setMethod(SeRequest.METHOD method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void addRequestParameter(String key, String value) {
        requestParameters.put(key, value);
    }

    public String getRequestParameter(String key) {
        return requestParameters.get(key);
    }

    public Map<String, String> getRequestParameters() {
        return requestParameters;
    }

    public void setRequestParameters(Map<String, String> requestParameters) {
        this.requestParameters = requestParameters;
    }

    public void addHeaderParameter(String key, String value) {
        headerParameters.put(key, value);
    }

    public String getHeaderParameter(String key) {
        return headerParameters.get(key);
    }

    public Map<String, String> getHeaderParameters() {
        return headerParameters;
    }

    public void setHeaderParameters(Map<String, String> headerParameters) {
        this.headerParameters = headerParameters;
    }

    public SeCookies getCookies() {
        if (cookies == null) {
            cookies = new SeCookies(headerParameters.get(COOKIES));
        }
        return cookies;
    }

    public void setCookies(SeCookies cookies) {
        this.cookies = cookies;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(status).append(NEWLINE);
        Set<String> keys = headerParameters.keySet();
        for (String key : keys) {
            sb.append(key).append(": ").append(headerParameters.get(key)).append(NEWLINE);
        }
        sb.append(NEWLINE);
        return sb.toString();
    }
}
