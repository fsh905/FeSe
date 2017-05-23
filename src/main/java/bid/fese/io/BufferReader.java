package bid.fese.io;

/**
 * Created by feng_sh on 17-5-22.
 * 获取header,
 * 但是这种方式有问题， 解析的时候相当与再次对字符串操作了一遍， 浪费效率
 */
public class BufferReader {

    private OldBufferInStream inStream;
    private char[] charBuffer;
    private static int defaultCharBufferSize = 8 * 1024; // 8k
    private int nextChar, nChars;
    private boolean isEndLine = false;

    public BufferReader(OldBufferInStream inStream, int bufferSize) {
        this.inStream = inStream;
        charBuffer = new char[bufferSize];
        nextChar = 0;
        nChars = 0;
    }

    public BufferReader(OldBufferInStream inStream) {
        this(inStream, defaultCharBufferSize);
    }

    /**
     * 按行读取
     * @return 返回一行
     */
    public String readLine() {
        if (nextChar >= nChars) {
            fill();
        }
        String line = new String(charBuffer, nextChar, nChars - nextChar);
        nextChar = nChars;
        return line;
    }

    /**
     * 读取请求中的请求头
     * @return 请求头
     */
    public String getHeader() {
        StringBuilder sb = new StringBuilder();
        while (!isEnd()) {
            if (nextChar >= nChars) {
                fill();
            }
            sb.append(charBuffer, nextChar, nChars);
            sb.append('\n');
            nextChar = nChars;
        }

        return sb.toString();
    }

    private void fill() {
        if (nextChar >= nChars && !isEndLine) {
            int r = -1;
            int i = 0;
            // 一个一个进行读取判断
            while ((r = inStream.read()) != -1 && i < charBuffer.length) {
                // 这里可以将整个请求头都放进去
                if (r == '\r') {
                    r = inStream.read();
                    if (r == '\n') {
                        r = inStream.read();
                        if (r == '\r') {
                            inStream.read();
                            isEndLine = true;
                            break;
                        } else {
                            // \r\n
                            charBuffer[i++] = (char)r;
                            break;
                        }
                    } else {
                        charBuffer[i++] = (char)r;

                    }
                } else {
                    charBuffer[i++] = (char)r;
                }
            }
            // todo 当请求头太大时如何处理
            if (i > charBuffer.length) {
                // 头部太大， 错误
                nextChar = 0;
                nChars = 0;
                return;
            }
            nextChar = 0;
            nChars = i;
        }
    }

    public boolean isEnd() {
        return isEndLine;
    }

}
