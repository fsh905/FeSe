package bid.fese.common;

import java.io.File;

/**
 * Created by feng_sh on 17-5-25.
 * 文件处理
 */
public class FileUtil {

    /**
     * 进行gzip压缩
     * @param fileName name
     * @return bytes
     */
    public static byte[] GZIPFile(String fileName) {
        return GZIPFile(new File(fileName));
    }

    /**
     * 进行gzip压缩
     * @param file file
     * @return bytes
     */
    public static byte[] GZIPFile(File file) {
        return null;
    }

}
