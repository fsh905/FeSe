package bid.fese.entity;

import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by feng_sh on 6/2/2017.
 * 静态资源缓存
 * 采用SoftReference进行连接， 当为软引用时，只有当快要内存溢出时才会进行回收
 */
public class StaticSoftCache {

    private Map<String, SoftReference<ByteBuffer>> cache = new ConcurrentHashMap<>();

    public ByteBuffer get(String url) {
        SoftReference<ByteBuffer> value = cache.get(url);
        if (value != null) {
            ByteBuffer res = value.get();
            if (res == null) {
                // 已被清理
                cache.remove(url);
            } else {
                return res;
            }
        }
        return null;
    }

    public ByteBuffer put(String url, byte[] body) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(body.length);
        buffer.put(body);
        buffer.rewind();
        cache.put(url, new SoftReference<>(buffer));
        return buffer;
    }

}
