package bid.fese.pool;

import bid.fese.common.Constants;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.nio.ByteBuffer;

/**
 * Author: FengShaohua
 * Date: 2018/1/13 11:29
 * Description: todo
 */
public class DirectDynamicByteBuffer implements DynamicByteBuffer {
    protected int bufferIndex; // byteBuffer数组的索引
    private int capital; // 最大容量
    private int writeIndex; // 当前写的位置, 相当于limit
    private int readIndex;  // 当前读的位置

    private int bufferUnitSize = Constants.DEFAULT_UPLOAD_SIZE;

    private ByteBuffer[] byteBuffers;

    public DirectDynamicByteBuffer(int cap, ByteBuffer... byteBuffers) {
        this.capital = cap;
        this.byteBuffers = byteBuffers;
        // todo: 评估有没有必要移走
        for (ByteBuffer byteBuffer : byteBuffers) {
            byteBuffer.clear();
        }
    }

    public DirectDynamicByteBuffer(ByteBuffer... byteBuffers) {
        this(byteBuffers.length * Constants.DEFAULT_BUFFER_SIZE, byteBuffers);
    }

    @Override
    public DynamicByteBuffer subBuffer(int start, int length) {
        // 暂不实现
        throw new NotImplementedException();
    }

    @Override
    public DynamicByteBuffer catBuffer(DynamicByteBuffer byteBuffer) {
        // 暂不实现
        throw new NotImplementedException();
    }

    @Override
    public int getByteBufferCount() {
        return byteBuffers.length;
    }

    @Override
    public ByteBuffer[] getByteBuffers() {
        return byteBuffers;
    }

    @Override
    public byte get() {
        byte b = byteBuffers[readIndex / bufferUnitSize].get(readIndex % bufferUnitSize);
        readIndex++;
        return b;
    }

    @Override
    public void put(byte b) {
        byteBuffers[writeIndex / bufferUnitSize].position(writeIndex % bufferUnitSize);
        byteBuffers[writeIndex / bufferUnitSize].put(b);
        writeIndex++;
    }

    @Override
    public void put(byte[] bytes) {
        int len = bytes.length;
        if (len > capital - writeIndex) {
            throw new IndexOutOfBoundsException("bytes size too large");
        }
        int nowRewind = 0;
        if (writeIndex % bufferUnitSize != 0) {
            byteBuffers[writeIndex / bufferUnitSize].position(writeIndex % bufferUnitSize);
            // 从下一个byteBuffer开始放
            nowRewind = bufferUnitSize - (writeIndex % bufferUnitSize);
            nowRewind = nowRewind > len ? len : nowRewind;
            byteBuffers[writeIndex / bufferUnitSize].put(bytes, 0, nowRewind);
            writeIndex += nowRewind;
        }
        while (nowRewind < len) {
            byteBuffers[writeIndex / bufferUnitSize].position(0);
            int putSize = bufferUnitSize > len - nowRewind ? len - nowRewind : bufferUnitSize;
            byteBuffers[writeIndex / bufferUnitSize].put(bytes, nowRewind, putSize);
            nowRewind += putSize;
            writeIndex += putSize;
        }
    }

    @Override
    public byte[] get(int start, int length) {
        if (writeIndex - start < length) {
            throw new IndexOutOfBoundsException("length is too large");
        }
        byte[] res = new byte[length];
        int end = start + length;
        int nowRewind = 0;
        if (start % bufferUnitSize != 0) {
            byteBuffers[writeIndex / bufferUnitSize].position(start % bufferUnitSize);
            nowRewind = bufferUnitSize - (bufferUnitSize - readIndex % bufferUnitSize);
            nowRewind = nowRewind > length ? length : nowRewind;
            byteBuffers[start / bufferUnitSize].get(res, 0, nowRewind);
            readIndex += nowRewind;
            start += nowRewind;
        }
        while (start < end) {
            byteBuffers[writeIndex / bufferUnitSize].position(0);
            int getLen = end - start >= bufferUnitSize ? bufferUnitSize : end - start;
            byteBuffers[start / bufferUnitSize].get(res, nowRewind, getLen);
            nowRewind += getLen;
            writeIndex += getLen;
            start += getLen;
        }
        return res;
    }


    @Override
    public void resetReadIndex() {
        readIndex = 0;
    }

    @Override
    public void resetWriteIndex() {
        writeIndex = 0;
    }

    @Override
    public int capital() {
        return capital;
    }

    @Override
    public ByteBuffer getByteBuffer(int index) {
        return byteBuffers[index];
    }

    @Override
    public int writeIndex() {
        return writeIndex;
    }

    @Override
    public int readIndex() {
        return readIndex;
    }
}
