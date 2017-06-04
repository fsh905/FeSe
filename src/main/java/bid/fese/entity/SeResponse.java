package bid.fese.entity;

import bid.fese.common.ApplicationContext;
import bid.fese.common.Constants;
import bid.fese.common.FileUtil;
import bid.fese.handler.RequestHandlers;
import bid.fese.handler.WriteHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

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
    private PrintWriter printWriter;
    private StaticSoftCacheBytes.CacheEntityBytes entityBytes;
    private boolean isKeepAlive;
    private boolean isSupportGZIP;
    private String remoteAddress;

    public SeResponse(SeRequest request) {
        this.socketChannel = request.getSocketChannel();
        // 判断是否支持gzip
        isSupportGZIP = false;
        // new header new cookie
        this.header = new SeHeader();
        this.cookies = new SeCookies();
        this.header.setUrl(request.getUrl());
        this.header.setCookies(this.cookies);
        this.header.setStatus(SeHeader.OK_200);
        this.header.addHeaderParameter(SeHeader.SERVER, "FeSe");
        this.outStream = new OutStream();
        this.remoteAddress = request.getRemoteAddress();

        if (request.isKeepAlive()) {
            this.header.addHeaderParameter(SeHeader.CONNECTION, SeHeader.KEEP_ALIVE);
            isKeepAlive = true;
        } else {
            isKeepAlive = false;
        }
    }

    public SeCookies getCookies() {
        return cookies;
    }

    public SeHeader getHeader() {
        return header;
    }

    public boolean isKeepAlive() {
        return isKeepAlive;
    }

    public boolean isSupportGZIP() {
        return isSupportGZIP;
    }

    public void setSupportGZIP(boolean supportGZIP) {
        isSupportGZIP = supportGZIP;
    }

    public void writeFile(String filePath) {
        writeFile(new File(filePath));
    }

    public void writeFile(File file) {
        writeFile(file, true);
    }

    private void writeFile(File file, boolean isShowPage) {
        // 目前的逻辑是当请求静态文件时， 如果文件不存在怎首先返回404页面，
        // 但是如果静态文件类型是图片或者视频时应该返回404请求而不是页面
        logger.debug("write file:" + file.getAbsolutePath());
        if (!file.exists()) {
            logger.error("not found:" + file.getAbsolutePath() + " -" + remoteAddress);
            if (isShowPage)
                _404_notFoundPage();
            else
                _404_notFound();
        } else {
            // 检查缓存
            String url = header.getUrl();
            if (isSupportGZIP) {
                url += "GZIP";
            }
            // 检查文件是否修改
            entityBytes = RequestHandlers.getCache().get(url);
            if (entityBytes == null) {
                logger.info("not cache " + url);
                try {
                    byte[] body = FileUtil.file2ByteArray(file, isSupportGZIP);
                    String fileType = Files.probeContentType(Paths.get(file.getAbsolutePath()));
                    entityBytes = RequestHandlers.getCache().put(url,
                            body, ZonedDateTime.now(Constants.ZONE_ID), fileType == null ? "none": fileType);
                } catch (IOException e) {
                    logger.error("write file occur error", e);
                    if (isShowPage) {
                        _500_Server_Error_Page();
                    } else {
                        _500_Server_Error();
                    }
                }
            } else {
                logger.info("hit cache " + url);
            }
            if (entityBytes != null) {
                if (isSupportGZIP) {
                    header.addHeaderParameter(SeHeader.CONTENT_ENCODING, SeHeader.GZIP);
                    logger.debug("use gzip");
                }
                header.addHeaderParameter(SeHeader.CONTENT_TYPE, entityBytes.getFileType());
                header.addHeaderParameter(SeHeader.LAST_MODIFIED, entityBytes.getTime().format(DateTimeFormatter.RFC_1123_DATE_TIME));
            } else {
                _404_notFound();
            }
        }
    }

    private void _404_notFoundPage() {
        writeFile(new File(ApplicationContext.get(Constants.CONFIG_STATIC_RESOURCE_PATH).toString()
            + ApplicationContext.get(Constants.CONFIG_PAGE_404).toString()), false);
    }

    private void _500_Server_Error_Page() {
        writeFile(new File(ApplicationContext.get(Constants.CONFIG_STATIC_RESOURCE_PATH).toString()
                + ApplicationContext.get(Constants.CONFIG_PAGE_500).toString()), false);
    }

    private void _404_notFound() {
        this.header.setStatus(SeHeader.NOT_FOUND_404);
        this.header.setContentLength(0);
        this.header.addHeaderParameter(SeHeader.CONNECTION, SeHeader.CONNECTION_CLOSE);
    }

    private void _500_Server_Error() {
        this.header.setStatus(SeHeader.SERVER_ERROR_500);
        this.header.setContentLength(0);
        this.header.addHeaderParameter(SeHeader.CONNECTION, SeHeader.CONNECTION_CLOSE);
    }

    /**
     * 主要功能是发送, 必须执行flush才能发送
     */
    public void flush() {
        // 不对头部进行缓存是因为有可能会进行cookie的修改等
        // 因此头部是动态的，不能进行缓存

        WriteHandler writeHandler = new WriteHandler(socketChannel, this, remoteAddress);
        // 静态资源会进行缓存， 因此使用不同的发送方式
        if (entityBytes != null) {
            ByteBuffer buffer = ByteBuffer.wrap(entityBytes.getByteBuffer());
            buffer.rewind();
            header.setContentLength(buffer.limit());
            logger.debug("response header len:" + header.getHeaderParameter(SeHeader.CONTENT_LENGTH));
            byte[] headerBytes = header.toString().getBytes();
            writeHandler.sendResponse(headerBytes, buffer);
        } else {
            header.setContentLength(outStream.nextBytes);
            logger.debug("response header len:" + header.getHeaderParameter(SeHeader.CONTENT_LENGTH));
            byte[] headerBytes = header.toString().getBytes();
            writeHandler.sendResponse(headerBytes, outStream.bytes, outStream.nextBytes);
        }
    }

    public OutputStream getOutStream() {
        if (outStream == null) {
            outStream = new OutStream();
        }
        return outStream;
    }

    public PrintWriter getPrintWriter() {
        if (printWriter == null) {
            printWriter = new PrintWriter(getOutStream());
        }
        return printWriter;
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
    }
}
