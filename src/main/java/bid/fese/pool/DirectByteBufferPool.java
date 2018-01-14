package bid.fese.pool;

import java.nio.ByteBuffer;

/**
 * Author: FengShaohua
 * Date: 2018/1/13 12:42
 * Description: todo
 */
public class DirectByteBufferPool extends AbstractByteBufferPool {

    @Override
    ByteBuffer[] buildBuffers(int count, int size) {
        ByteBuffer[] byteBuffers = new ByteBuffer[count];
        for (int i = 0; i < count; i++) {
            byteBuffers[i] = ByteBuffer.allocateDirect(size);
        }
        return byteBuffers;
    }

    @Override
    DynamicByteBuffer buildDynamicByteBuffer(ByteBuffer[] byteBuffers) {
        return new DirectDynamicByteBuffer(byteBuffers);
    }
}
