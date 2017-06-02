package bid.fese.entity;

import bid.fese.common.ApplicationContext;
import bid.fese.common.Constants;
import bid.fese.handler.WriteHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.GZIPOutputStream;

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
    //    private byte[] contents;
    private boolean isKeepAlive;
    private boolean isSupportGZIP;
    private String remoteAddress;

    public SeResponse(SeRequest request) {
        this.socketChannel = request.getSocketChannel();
        // 判断是否支持gzip
        if (request.getHeader().getHeaderParameter(SeHeader.ACCEPT_ENCODING) != null) {
            isSupportGZIP = request.getHeader().getHeaderParameter(SeHeader.ACCEPT_ENCODING).contains("gzip");
        }
        // new header new cookie
        this.header = new SeHeader();
        this.cookies = new SeCookies();
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
            // 文件存在
            FileInputStream fis = null;
            GZIPOutputStream goz = null;
            try {
                fis = new FileInputStream(file);
                // 这里采用gzip
                if (isSupportGZIP) {
                    header.addHeaderParameter(SeHeader.CONTENT_ENCODING, SeHeader.GZIP);
                    logger.debug("use gzip");
                    goz = new GZIPOutputStream(outStream);
                    int l = 0;
                    byte[] bytes = new byte[Constants.DEFAULT_UPLOAD_SIZE];
                    while ((l = fis.read(bytes)) != -1) {
                        goz.write(bytes, 0, l);
                    }
                    goz.flush();
                } else {
                    logger.debug("not use gzip");
                    byte[] bytes = new byte[Constants.DEFAULT_UPLOAD_SIZE];
                    int len = 0;
                    while ((len = fis.read(bytes)) != -1) {
                        outStream.write(bytes, 0, len);
                    }
                }
                String fileType = Files.probeContentType(Paths.get(file.getAbsolutePath()));
                if (fileType != null) {
                    header.addHeaderParameter(SeHeader.CONTENT_TYPE, fileType);
                }
            } catch (FileNotFoundException e) {
                logger.error("file not found; path:" + file.getAbsolutePath() + " -" + remoteAddress, e);
                if (isShowPage)
                    _404_notFoundPage();
                else
                    _404_notFound();
            } catch (IOException e) {
                logger.error("read error; path:" + file.getAbsolutePath() + " -" + remoteAddress, e);
                if (isShowPage)
                    _500_Server_Error_Page();
                else
                    _500_Server_Error();
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        logger.error("close fis error; path:" + file.getAbsolutePath()  + " -" + remoteAddress, e);
                        if (isShowPage)
                            _500_Server_Error_Page();
                        else
                            _500_Server_Error();
                    }
                }
                if (goz != null) {
                    try {
                        goz.close();
                    } catch (IOException e) {
                        logger.error("close giz error: path:" + file.getAbsolutePath() + " -" + remoteAddress, e);
                        if (isShowPage)
                            _500_Server_Error_Page();
                        else
                            _500_Server_Error();
                    }
                }
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
        logger.debug("flush start");
        // 统一使用outStream接口
        header.setContentLength(outStream.nextBytes);
        byte[] headerBytes = header.toString().getBytes();
        logger.debug("response header:\n" + header.toString());
        logger.debug("start send response header");
        new WriteHandler(socketChannel, this, remoteAddress)
                .sendResponse(headerBytes, outStream.bytes, outStream.nextBytes);
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
