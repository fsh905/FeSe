package bid.fese.prepare;

import bid.fese.pool.ByteBufferPool;
import bid.fese.pool.DirectByteBufferPool;
import bid.fese.pool.DirectDynamicByteBuffer;
import bid.fese.pool.DynamicByteBuffer;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Author: FengShaohua
 * Date: 2018/1/13 17:17
 * Description: todo
 */
public class PoolTest {

    @Test
    public void dynamicBufferTest() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1000);
        System.out.println(byteBuffer.capacity());
        DynamicByteBuffer buffer = new DirectDynamicByteBuffer(byteBuffer);
        System.out.println(String.format("cap: %d, read: %d, put: %d",
                buffer.capital(), buffer.readIndex(), buffer.writeIndex()));
        buffer.put("this is a sample test".getBytes());
        for (int i = 0; i < 5; i++) {
            buffer.put(("index+" + i + "\n").getBytes());
        }
        for (int i = 0; i < 3; i++) {
            buffer.put((byte) (i + 68));
        }
        System.out.println(String.format("cap: %d, read: %d, put: %d",
                buffer.capital(), buffer.readIndex(), buffer.writeIndex()));
        System.out.println(new String(buffer.get(0, buffer.writeIndex())));
    }

    @Test
    public void directBufferTest() {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(100);
        byteBuffer.put("he he da".getBytes());
        byteBuffer.flip();
//        byteBuffer.position(10);
    }

    @Test
    public void poolTest() {
        final ByteBufferPool pool = new DirectByteBufferPool();
        CountDownLatch countDownLatch = new CountDownLatch(10);
        ExecutorService service = Executors.newFixedThreadPool(20);
        for (int i = 0; i < 10; i++) {
            final int t = i;
            service.execute(() -> {
                DynamicByteBuffer dynamicByteBuffer = pool.get(100);
                System.out.println("thread-" + t + " get dyna");
                dynamicByteBuffer.put(("put data: thread - " + t).getBytes());
                try {
//                        TimeUnit.MILLISECONDS.sleep((long) (Math.random() * 1000));
                    Thread.sleep((long) (Math.random() * 1000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(new String(dynamicByteBuffer.get(0, dynamicByteBuffer.writeIndex())));
                pool.release(dynamicByteBuffer);
                System.out.println("thread-" + t + " rel dyna");
                countDownLatch.countDown();
            });
        }
        service.shutdown();
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        DynamicByteBuffer db = pool.wrap("last demo".getBytes());
        System.out.println(new String(db.get(0, db.writeIndex())));
    }

}
