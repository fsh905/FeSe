package bid.fese.io;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by feng_sh on 17-5-22.
 * 对socket的io进行封装
 */
public class BufferInStream {
    private AsynchronousSocketChannel socketChannel;

    private static final int defaultBufferSize = 1024 * 8;
    private int nextByte, nBytes;
    private ByteBuffer byteBuffer;

    public BufferInStream(AsynchronousSocketChannel socketChannel, int bufferSize) {
        this.socketChannel = socketChannel;
        byteBuffer = ByteBuffer.allocate(bufferSize);
        nextByte = 0;
        nBytes = 0;
    }

    public BufferInStream(AsynchronousSocketChannel socketChannel) {
        this(socketChannel, defaultBufferSize);
    }

    /**
     * 读取单个字符， 当结尾时返回-1
     * @return ascii or -1
     */
    public int read() {
        if (nextByte >= nBytes) {
            fill();
        }
        if (nextByte >= nBytes) {
            // eof
            return -1;
        }
        return byteBuffer.get(nextByte++);
    }

    /**
     * 将缓冲区内容更新
     */
    private void fill() {
        if (nextByte >= nBytes) {

            // 缓冲区已被使用
            // 清空byteBuffer
            byteBuffer.flip();
            // 这里还是普通的阻塞模式
            //todo 这里可以进行修改
            Future<Integer> res = socketChannel.read(byteBuffer);
            int readCount = 0;
            try {
                // 1s之内不能读取完毕即报错
                readCount = res.get(1000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
            if (readCount >= 0) {
                // 清零
                nBytes = readCount;
                nextByte = 0;
            }
        }
    }

    /**
     *
     * @param bytes 目的数组
     * @param start 从数组的什么位置开始
     * @param len   复制的长度
     * @return 复制的长度
     */
    private int read(byte[] bytes, int start, int len) {
        if (nextByte >= nBytes) {
            fill();
        }
        if (nextByte >= nBytes) {
            // eof or timeout
            return -1;
        }

        if (start + len > bytes.length) {
            len = bytes.length - start;
        }
        // 长度比较短的情况
        if (len <= (nBytes - nextByte)) {
            System.arraycopy(byteBuffer.array(), nextByte, bytes, start, len);
            nextByte += len;
            return len;
        } else {
            // 长度很长的话， 进行多次读取
            int i = 0;
            while (len > (i + (nBytes - nextByte))) {
                System.arraycopy(byteBuffer.array(), nextByte, bytes, i + start, nBytes - nextByte);
                i += (nBytes - nextByte);
                nextByte = nBytes;
                fill();
                // 读取完毕
                if (nextByte >= nBytes) {
                    return i;
                }
            }
            if (len > i) {
                // 剩下的长度小于 len - i
                System.arraycopy(byteBuffer.array(), nextByte, bytes, i, len - i);
                return len;
            }
            return i;
        }
    }

}
