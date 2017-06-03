package bid.fese.entity;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by feng_sh on 6/2/2017.
 * 静态资源缓存
 * 采用SoftReference进行连接， 当为软引用时，只有当快要内存溢出时才会进行回收
 */
public class StaticSoftCache {

    private Map<String, SoftReference<CacheEntity>> cache = new ConcurrentHashMap<>();

    public class CacheEntity {
        private ByteBuffer byteBuffer;
        private ZonedDateTime time;
        private String fileType;
        CacheEntity(ByteBuffer byteBuffer, ZonedDateTime time, String fileType) {
            this.byteBuffer = byteBuffer;
            this.time = time;
            this.fileType = fileType;
        }

        public ByteBuffer getByteBuffer() {
            return byteBuffer;
        }

        public ZonedDateTime getTime() {
            return time;
        }

        public String getFileType() {
            return fileType;
        }
    }



    public CacheEntity get(String url) {
        SoftReference<CacheEntity> value = cache.get(url);
        if (value != null) {
            CacheEntity res = value.get();
            if (res == null) {
                // 已被清理
                cache.remove(url);
            } else {
                return res;
            }
        }
        return null;
    }

    public CacheEntity put(String url, byte[] body, ZonedDateTime time, String fileType) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(body.length);
        buffer.put(body);
        buffer.rewind();
        CacheEntity cacheEntity = new CacheEntity(buffer, time, fileType);
        cache.put(url, new SoftReference<>(cacheEntity));
        return cacheEntity;
    }

}
