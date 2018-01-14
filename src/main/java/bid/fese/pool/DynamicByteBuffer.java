package bid.fese.pool;

import java.nio.ByteBuffer;

/**
 * Author: FengShaohua
 * Date: 2018/1/12 20:40
 * Description: 不定长byteBuffer, 是否与pool之间进行强依赖
 */
public interface DynamicByteBuffer {


    DynamicByteBuffer subBuffer(int start, int length);

    /**
     * 不允许subBuffer与fullBuffer, subBuffer与subBuffer之间进行cat
     *
     * @param byteBuffer 非subBuffer
     */
    DynamicByteBuffer catBuffer(DynamicByteBuffer byteBuffer);

    ByteBuffer getByteBuffer(int index);

    int getByteBufferCount();

    ByteBuffer[] getByteBuffers();

    byte get();

    void put(byte b);

    void put(byte[] bytes);

    byte[] get(int offset, int length);

    void resetReadIndex();

    void resetWriteIndex();

    int capital();

    int readIndex();

    int writeIndex();

}
