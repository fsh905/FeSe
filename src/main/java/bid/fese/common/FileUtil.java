package bid.fese.common;

import java.io.*;
import java.util.zip.GZIPOutputStream;

/**
 * Created by feng_sh on 6/2/2017.
 * 文件操作
 */
public class FileUtil {

    /**
     * 将文件转化为byte数组
     * @param file 文件
     * @param zip 是否压缩
     * @return byte
     * @throws IOException 发生错误
     */
    public static byte[] file2ByteArray(File file, boolean zip) throws IOException {
        InputStream is = null;
        GZIPOutputStream gzip = null;
        byte[] buffer = new byte[8912];
        ByteArrayOutputStream baos = new ByteArrayOutputStream(8912);
        try {
            if (zip) {
                gzip = new GZIPOutputStream(baos);
            }

            is = new BufferedInputStream(new FileInputStream(file));
            int read = 0;
            while ((read = is.read(buffer)) != -1) {
                if (zip) {
                    gzip.write(buffer, 0, read);
                } else {
                    baos.write(buffer, 0, read);
                }
            }
        } catch (IOException e) {
            throw e;
        } finally {
            if (is != null) {
                is.close();
            }
            if (gzip != null) {
                gzip.close();
            }
        }
        return baos.toByteArray();

    }

}
