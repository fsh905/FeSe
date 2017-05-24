package bid.fese.handler;

import bid.fese.entity.SeCookies;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by feng_ on 2016/12/8.
 * parse
 */
@Deprecated
public class OldRequestHeaderHandler {

    private static final Logger logger = LogManager.getLogger(OldRequestHeaderHandler.class);
    //request method
    private String method;
    //protocol
    private String protocol;
    //request url
    private String url;
    //cookies
    private SeCookies cookies;
    //header parameter
    private Map<String,String> headerParameter;
    //request parameter
    private Map<String,String> requestParameter;
    //now
    private byte[] unparseHeaders;
    //just complete get
    int position;

    public OldRequestHeaderHandler(byte[] header, String method) {

        this.method = method;
        this.unparseHeaders = header;
        this.headerParameter = new HashMap<>();
        position = 0;
        logger.info("method:"+method);
    }

    public void parse(){
        //only support get
        //parse request line
        requestLineParse();

        int index = position;
        String key,value;
        while (position < unparseHeaders.length-2) {
            while (unparseHeaders[position] != ':'){
                position ++;
            }
            key = new String(unparseHeaders,index,position-index);
            index = position += 2;
            while (unparseHeaders[position] != '\r'){
                position ++;
            }
            value = new String(unparseHeaders,index,position-index);
            index = position += 2;
            headerParameter.put(key,value);
            logger.info("key:"+key+" value:"+value);
        }
    }

    /**
     * parse request line
     * GET /index?name=fese&id=1 HTTP/1.1
     * method get
     * url /index
     * request para name:fese
     * protocol http1.1
     */
    public void requestLineParse(){

        //method
        while (unparseHeaders[position] != ' ') position++;
        position += 1;
        int lastPosi = position;
        int index = 0;
        //request
        while (unparseHeaders[position] != ' ') {

            if (unparseHeaders[position] == '?'){
                //hava reque para
                position ++;
                index = parseRequestParam();
                break;
            }
            position ++;
        }
        // 解析url
        url = new String(unparseHeaders,lastPosi,position-lastPosi-1);
        logger.info("url:"+url);
        lastPosi = (position += index) ;
            while (unparseHeaders[position] != '\r') position ++;
        protocol = new String(unparseHeaders,lastPosi,position-lastPosi);
        logger.info("protocol:"+protocol);
        position += 2;
    }

    public int parseRequestParam(){
        //when have
        requestParameter = new HashMap<>();
        int index = position,old;
        String key=null,
                value=null;
        while (unparseHeaders[index] != ' '){

            index ++;
            old = index;
            while (unparseHeaders[index] != '&' && unparseHeaders[index] != ' '){
                if (unparseHeaders[index] =='='){
                    key = new String(unparseHeaders,old,index-old);
                    old = index + 1;
                }
                index ++;
            }
            value = new String(unparseHeaders,old,index-old);

            requestParameter.put(key,value);
            logger.info("k:"+key+" v:"+value);
        }
        return index + 1;
    }

    public String getMethod() {
        return method;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getUrl() {
        return url;
    }

    public SeCookies getCookies() {
        return cookies;
    }

    public Map<String, String> getHeaderParameter() {
        return headerParameter;
    }

    public Map<String, String> getRequestParameter() {
        return requestParameter;
    }

}
