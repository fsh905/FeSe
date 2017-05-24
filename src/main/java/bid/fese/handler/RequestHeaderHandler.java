package bid.fese.handler;

import bid.fese.entity.SeHeader;

/**
 * Created by feng_sh on 17-5-23.
 * 解析头部
 */

public class RequestHeaderHandler {

    private final byte[] END = {13, 10, 13, 10};
    private String header;
    private byte[] bytes;

    public RequestHeaderHandler(byte[] bytes) {
        this.bytes = bytes;
    }

    public SeHeader firstParse() {
        int index = find(END, 10);
        if (index == bytes.length) {
            return null;
        } else {
            header = new String(bytes, 0, index);
        }
        return null;
    }


    private int find(byte[] toFind, int start) {
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

    public static void main(String[] args) {
        RequestHeaderHandler headerHandler = new RequestHeaderHandler(new byte[]{
                13,10,13,12,
                60,34,54,64,
                13,10,13,10,
                12,14,15,36});
        System.out.println(headerHandler.find(headerHandler.END, 0));

    }

}
