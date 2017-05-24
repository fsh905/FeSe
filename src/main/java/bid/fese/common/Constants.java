package bid.fese.common;

/**
 * Created by feng_sh on 5/23/2017.
 * 存放全局变量
 */
public class Constants {

    // request handlers
    public static final String REQUEST_HANDLERS = "REQUEST_HANDLERS";
    // request handler
    public static final String REQUEST_HANDLER = "REQUEST_HANDLER";
    // 最大上传文件大小
    public static final int MAX_UPLOAD_SIZE = 1024 * 1024 * 4;

    public static final int DEFAULT_UPLOAD_SIZE = 1024 * 8;

    public static final byte[] HEADER_END = {13, 10, 13, 10};
    // 长度
    public static final String CONTENT_LENGTH = "Content-Length";
}
