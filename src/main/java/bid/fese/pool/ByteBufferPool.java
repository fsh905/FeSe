package bid.fese.pool;

import java.util.concurrent.TimeUnit;

/**
 * Author: FengShaohua
 * Date: 2018/1/12 20:24
 * Description: bytebuffer池
 */
public interface ByteBufferPool {

    DynamicByteBuffer wrap(byte[] bytes);

    /**
     * 获取
     *
     * @return
     */
    DynamicByteBuffer get(int cap);

    DynamicByteBuffer get(int cap, long timeout, TimeUnit timeUnit);

    DynamicByteBuffer get();

    DynamicByteBuffer get(long timeout, TimeUnit timeUnit);

    void release(DynamicByteBuffer dynamicByteBuffer);

}
