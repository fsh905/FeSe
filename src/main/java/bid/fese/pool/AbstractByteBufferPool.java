package bid.fese.pool;

import bid.fese.common.Constants;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Author: FengShaohua
 * Date: 2018/1/13 12:43
 * Description: todo
 */
public abstract class AbstractByteBufferPool implements ByteBufferPool {

    // 默认buffer大小
    private final int defaultBufferSize = Constants.DEFAULT_BUFFER_SIZE; // 32KB

    // 默认buffer池大小 (pow(2, n) - 1)
    private int defaultPoolCount = Constants.DEFAULT_POOL_COUNT;

    //
    private ByteBuffer[] byteBuffers;

    private Lock lock;
    private Condition notEmpty;

    private volatile int getIndex = 0;
    private volatile int putIndex = defaultPoolCount;


    AbstractByteBufferPool() {
        byteBuffers = buildBuffers(defaultPoolCount, defaultBufferSize);
        lock = new ReentrantLock();
        notEmpty = lock.newCondition();
    }

    @Override
    public DynamicByteBuffer get(int cap) {
        int count = cap % defaultBufferSize == 0 ? cap / defaultBufferSize : cap / defaultBufferSize + 1;
        lock.lock();
        try {
            while (true) {
                if (putIndex - getIndex <= defaultPoolCount && putIndex - getIndex >= count) {
                    return doGet(count);
                }
                try {
                    notEmpty.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("get");
            }
        } finally {
            lock.unlock();
        }
    }

    private DynamicByteBuffer doGet(int count) {
        ByteBuffer[] resBuffer = new ByteBuffer[count];
        for (int i = 0; i < count; i++) {
            resBuffer[i] = byteBuffers[realIndex(getIndex++)];
        }
        return buildDynamicByteBuffer(resBuffer);
    }

    @Override
    public DynamicByteBuffer get(int cap, long timeout, TimeUnit timeUnit) {
        int count = cap % defaultBufferSize == 0 ? cap / defaultBufferSize : cap / defaultBufferSize + 1;
        lock.lock();
        try {
            long nanos = timeUnit.toNanos(timeout);
            while (true) {
                if (nanos <= 0) {
                    return null;
                }
                if (putIndex - getIndex <= defaultPoolCount && putIndex - getIndex >= count) {
                    return doGet(count);
                }
                try {
                    nanos = notEmpty.awaitNanos(nanos);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            lock.unlock();
        }
    }


    @Override
    public DynamicByteBuffer get() {
        return get(1);
    }

    @Override
    public DynamicByteBuffer get(long timeout, TimeUnit timeUnit) {
        return get(1, timeout, timeUnit);
    }

    private int realIndex(int nowIndex) {
        return nowIndex & (defaultPoolCount - 1);
    }

    @Override
    public DynamicByteBuffer wrap(byte[] bytes) {
        DynamicByteBuffer dynamicByteBuffer = get(bytes.length);
        dynamicByteBuffer.put(bytes);
        return dynamicByteBuffer;
    }


    @Override
    public void release(DynamicByteBuffer dynamicByteBuffer) {
        lock.lock();
        try {
            put(dynamicByteBuffer);
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 这里不需要进行校验, 只要get不出错, put不会出现越界问题
     *
     * @param dynamicByteBuffer 需要释放的buffer
     */
    private void put(DynamicByteBuffer dynamicByteBuffer) {
        ByteBuffer[] bs = dynamicByteBuffer.getByteBuffers();
        for (ByteBuffer b : bs) {
            byteBuffers[realIndex(putIndex++)] = b;
        }
    }

    abstract ByteBuffer[] buildBuffers(int count, int size);

    abstract DynamicByteBuffer buildDynamicByteBuffer(ByteBuffer[] byteBuffers);

}
