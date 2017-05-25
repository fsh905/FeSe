package bid.fese.entity;

import bid.fese.common.Constants;
import bid.fese.handler.WriterHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

/**
 * Created by feng_sh on 5/24/2017.
 * response
 */
public class SeResponse {

    private static final Logger logger = LogManager.getLogger(SeResponse.class);

    private SeCookies cookies;
    private AsynchronousSocketChannel socketChannel;
    private SeHeader header;
    private OutStream outStream;
    private byte[] contents;

    public SeResponse(SeRequest request) {
        this.socketChannel = request.getSocketChannel();
        SeHeader header = request.getHeader();
        this.cookies = header.getCookies();
        // 清除header中的请求参数等
        header.clear();
        this.header = header;
        this.header.setStatus(SeHeader.OK_200);
        this.header.addHeaderParameter(SeHeader.SERVER, "FeSe");
    }



    public void writeFile(String filePath) {
        writeFile(new File(filePath));
    }

    public void writeFile(File file) {
        logger.debug("write file:" + file.getAbsolutePath());
        if (!file.exists()) {
            logger.error("not found:" + file.getAbsolutePath());
            _404_notFound();
        } else {
            // 文件存在
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                long len = file.length();
                contents = new byte[(int) len];
                fis.read(contents);
            } catch (FileNotFoundException e) {
                logger.error("file not found; path:" + file.getAbsolutePath(), e);
                _404_notFound();
            } catch (IOException e) {
                logger.error("read error; path:" + file.getAbsolutePath(), e);
                _500_Server_Error();
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        logger.error("close fis error; path:" + file.getAbsolutePath(), e);
                        _500_Server_Error();
                    }
                }
            }
        }
    }

    private void _404_notFound() {
        this.header.setStatus(SeHeader.NOT_FOUND_404);
        this.header.setContentLength(0);
        flush();
    }

    private void _500_Server_Error() {
        this.header.setStatus(SeHeader.SERVER_ERROR_500);
        this.header.setContentLength(0);
        flush();
    }

    /**
     * 主要功能是发送, 必须执行flush才能发送
     */
    public void flush() {
        ByteBuffer byteBuffer = null;
        if (outStream == null) {
            // 再次判断contents是否有, 因此 首先推荐使用outStream
            if (contents == null) {
                byte[] headerBytes = header.toString().getBytes();
                byteBuffer = ByteBuffer.allocate(headerBytes.length);
                byteBuffer.put(headerBytes);
                byteBuffer.flip();
            } else {
                header.setContentLength(contents.length);
                byte[] headerBytes = header.toString().getBytes();
                byteBuffer = ByteBuffer.allocate(headerBytes.length + contents.length);
                byteBuffer.put(headerBytes, 0, headerBytes.length);
                byteBuffer.put(contents, 0, contents.length);
                byteBuffer.flip();
            }
        } else {
            // 首先判断out是否有
            header.setContentLength(outStream.nextBytes);
            byte[] headerBytes = header.toString().getBytes();
            byte[] outs = outStream.bytes;
            byteBuffer = ByteBuffer.allocate(headerBytes.length + outStream.nextBytes);
            byteBuffer.put(headerBytes, 0 , headerBytes.length);
            byteBuffer.put(outs, 0, outStream.nextBytes);
            byteBuffer.flip();
        }

        logger.debug("response header:\n" + header.toString());
        socketChannel.write(byteBuffer, socketChannel, new WriterHandler());

    }

    public OutputStream getOutStream() {
        if (outStream == null) {
            outStream = new OutStream();
        }
        return outStream;
    }

    private class OutStream extends OutputStream{

        private byte[] bytes;
        private int nextBytes;

        OutStream() {
            bytes = new byte[Constants.DEFAULT_UPLOAD_SIZE];
            nextBytes = 0;
        }

        @Override
        public void write(int b) throws IOException {
            if (nextBytes >= bytes.length) {
                byte[] newBytes = new byte[bytes.length * 2];
                System.arraycopy(bytes, 0, newBytes, 0, nextBytes);
                bytes = newBytes;
            }
            bytes[nextBytes++] = (byte) b;
        }

        /**
         * 调用外部类的flush
         * @throws IOException
         */
        @Override
        public void flush() throws IOException {
            SeResponse.this.flush();
        }
    }

}
