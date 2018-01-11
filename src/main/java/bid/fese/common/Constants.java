package bid.fese.common;

import java.time.ZoneId;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Created by feng_sh on 5/23/2017.
 * 存放全局变量
 */
public class Constants {

    // request handlers
    public static final String REQUEST_HANDLERS = "request.handlers";
    // request handler
//    public static final String REQUEST_HANDLER = "request.handler";
    // 最大上传文件大小
    public static final int MAX_UPLOAD_SIZE = 1024 * 1024 * 4;

    public static final Pattern NO_GZIP_STATIC_RESOURCE = Pattern.compile("(jpg)|(png)|(gif)|(svg)|(mp3)|(mp4)|(flv)");

    public static final int DEFAULT_UPLOAD_SIZE = 1024 * 8;
    // 默认响应的大小
    public static final int DEFAULT_RESPONSE_SIZE = 1024 * 8;
    // 中国时区
    public static final ZoneId ZONE_ID = ZoneId.of(ZoneId.SHORT_IDS.get("CTT"));

    // keep alive时长， 默认5min
    public static final long DEFAULT_KEEP_ALIVE_TIME = 5L;
    public static final TimeUnit DEFAULT_KEEP_ALIVE_TIME_UNIT = TimeUnit.MINUTES;

    // classpath
    public static final String CLASS_PATH = "CLASS_PATH";

    // 配置文件路径
    public static final String CONFIGURE_PATH = "config.path";
    public static final String SERVER_PORT = "server.port";
    public static final String CONTROLLER_PATH = "controller.path";
    public static final String DYNAMIC_REQUEST_HANDLER = "dynamic.request.handler";
    public static final String STATIC_RESOURCE_PATH = "static.resource.path";
    public static final String REQUEST_POSTFIX = "request.postfix";
    public static final String REQUEST_HANDLER = "request.handler";
    public static final String PAGE_INDEX = "index";
    public static final String PAGE_404 = "page.404";
    public static final String PAGE_500 = "page.500";

}
