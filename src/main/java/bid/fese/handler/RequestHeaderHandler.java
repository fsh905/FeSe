package bid.fese.handler;

import bid.fese.common.Constants;
import bid.fese.entity.SeHeader;
import bid.fese.entity.SeRequest;
import bid.fese.exception.UnsupportedRequestMethodException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by feng_sh on 17-5-23.
 * 解析头部
 */

public class RequestHeaderHandler {

    private final static Logger logger = LogManager.getLogger(RequestHeaderHandler.class);



    public static SeHeader parseHeader(byte[] bytes, int end) throws UnsupportedRequestMethodException {
        SeHeader header = new SeHeader();
        parse(bytes, header, 0, end);
        return header;
    }

    private static void parse(byte[] bytes, SeHeader header, int headParseIndex, int headEndIndex) throws UnsupportedRequestMethodException {

        int index = requestLineParse(bytes, header, headParseIndex);
        headParseIndex += index;

        String key,value;
        while (headParseIndex < headEndIndex) {
            while (bytes[headParseIndex] != ':'){
                headParseIndex ++;
            }
            key = new String(bytes,index,headParseIndex-index);
            index = headParseIndex += 2;
            while (bytes[headParseIndex] != '\r'){
                headParseIndex ++;
            }
            value = new String(bytes,index,headParseIndex-index);
            index = headParseIndex += 2;
            header.addHeaderParameter(key,value);

            logger.info("key:"+key+"-\tvalue:"+value);
        }
    }


    /**
     * parse request line
     * GET /index?name=fese&id=1 HTTP/1.1
     * method get
     * url /index
     * request para name:fese
     * protocol http1.1
     * 解析请求行
     */
    private static int requestLineParse(byte[] bytes, SeHeader header, int headParseIndex) throws UnsupportedRequestMethodException {

        //method
        // c, d, g, h, pa, po, pu, t
        switch (bytes[headParseIndex]) {
            case 67 : header.setMethod(SeRequest.METHOD.CONNECT); headParseIndex += 8; break;
            case 68 : header.setMethod(SeRequest.METHOD.DELETE); headParseIndex += 7; break;
            case 71 : header.setMethod(SeRequest.METHOD.GET); headParseIndex += 4; break;
            case 72 : header.setMethod(SeRequest.METHOD.HEAD); headParseIndex += 5; break;
            case 80 :
                switch (bytes[headParseIndex + 1]) {
                    case 65 : header.setMethod(SeRequest.METHOD.PATCH); headParseIndex += 6; break;
                    case 79 : header.setMethod(SeRequest.METHOD.POST); headParseIndex += 5; break;
                    case 85 : header.setMethod(SeRequest.METHOD.PUT); headParseIndex += 4; break;
                    default: throw new UnsupportedRequestMethodException("");
                }
                break;
            case 84 : header.setMethod(SeRequest.METHOD.TRACE); headParseIndex += 6; break;
            default: throw new UnsupportedRequestMethodException("");
        }

        logger.debug("method:" + header.getMethod());
/*
        while (bytes[headParseIndex] != ' '){
            headParseIndex++;
        }
*/
//        headParseIndex += 1;
        int lastPosi = headParseIndex;
        int index = 0;
        //request
        while (bytes[headParseIndex] != ' ') {
            if (bytes[headParseIndex] == '?'){
                //hava reque para
//                headParseIndex ++;
                index = parseRequestParam(bytes, header, headParseIndex);
//                headParseIndex--;
                break;
            }
            headParseIndex ++;
        }
        // 解析url
        header.setUrl(new String(bytes,lastPosi,headParseIndex-lastPosi));

        logger.debug("url:"+header.getUrl());

        lastPosi = (headParseIndex += index) ;

        while (bytes[headParseIndex] != '\r') headParseIndex ++;

        header.setProtocol(new String(bytes,lastPosi,headParseIndex-lastPosi));
        logger.debug("protocol:"+header.getProtocol());
        return headParseIndex + 2;

    }

    /**
     * 解析请求参数
     */
    private static int parseRequestParam(byte[] bytes, SeHeader header, int headParseIndex){
        //when have
        int index = headParseIndex,old;
        String key=null,
                value=null;
        while (bytes[index] != ' '){
            index ++;
            old = index;
            while (bytes[index] != '&' && bytes[index] != ' '){
                if (bytes[index] =='='){
                    key = new String(bytes,old,index-old);
                    old = index + 1;
                }
                index ++;
            }
            value = new String(bytes,old,index-old);

            header.addRequestParameter(key,value);
            logger.debug("k:"+key+" \tv:"+value);
        }
        return index + 1;
    }


    public static int find(byte[] bytes, byte[] toFind, int start) {
        int index = bytes.length;
        // 找到指定字符的位置
        outer: for (int i = start; i < bytes.length; ++i) {

            for (int j = 0; j < toFind.length;) {
                if (bytes[i] == toFind[j]) {
                    ++i;
                    ++j;
                    if (j == toFind.length) {
                        index = i - toFind.length;
                        break outer;
                    }
                } else {
                    i = i - j; // step back
                    break;
                }
            }
        }
        return index;
    }

    public static void main(String[] args) throws UnsupportedRequestMethodException {
        String hd = "GET /yin-jingyu/archive/2011/08/01/2123548.html?name=feng&name=shao HTTP/1.1\r\n" +
                "Host: www.cnblogs.com\r\n" +
                "Connection: keep-alive\r\n" +
                "Pragma: no-cache\r\n" +
                "Cache-Control: no-cache\r\n" +
                "Upgrade-Insecure-Requests: 1\r\n" +
                "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36\r\n" +
                "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8\r\n" +
                "DNT: 1\r\n" +
                "Referer: https://www.baidu.com/link?url=yzCqz4EqdbSnTD7mN5Ww8dLM2Y5XKcYOeXmkZeksacoHMDHLiPhX1liXgmysAnwLT-l0NUvZ1KsaOr4YPCM6GRqMPFcpkGiVu-qF2ecDr2W&wd=&eqid=c7a2d39300002434000000035924f29a\r\n" +
                "Accept-Encoding: gzip, deflate, sdch\r\n" +
                "Accept-Language: zh-CN,zh;q=0.8,en;q=0.6\r\n" +
                "Cookie: sc_is_visitor_unique=rx11108148.1491558417.84D29D5BB5814FB96C7B7949DADBA8CC.2.2.2.2.2.2.2.2.2; UM_distinctid=15b4badb4bed1-0b2a670c5d82a2-8373f6a-144000-15b4badb4bfa5; CNZZDATA5808629=cnzz_eid%3D709391905-1491623328-null%26ntime%3D1491623328; CNZZDATA2686777=cnzz_eid%3D657918885-1491794825-null%26ntime%3D1491825109; pgv_pvi=1856399360; .CNBlogsCookie=4417CB748ED68D68E65A2365A69267A6736DEE8AE4ABA4D5DD1B4ACFC4C018B6985853548C7B51A79A258B7A099B56BF61DBC8F39BF2D4B2655B30DDBD28AEA1680A3D9995EDE44561677D8DB9BA5D360AF8EC47; _ga=GA1.2.1267869330.1490500865; _gid=GA1.2.1792861177.1495593628\r\n" +
                "\r\n";
        byte[] bytes = hd.getBytes();
        long t = System.currentTimeMillis();
        parseHeader(bytes, find(bytes, Constants.HEADER_END, 0));
        System.out.println(System.currentTimeMillis() - t);
    }

}
