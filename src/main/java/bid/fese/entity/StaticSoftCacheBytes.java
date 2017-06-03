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
public class StaticSoftCacheBytes {

    private Map<String, SoftReference<CacheEntityBytes>> cache = new ConcurrentHashMap<>();

    public class CacheEntityBytes {
        private byte[] byteBuffer;
        private ZonedDateTime time;
        private String fileType;
        CacheEntityBytes(byte[] byteBuffer, ZonedDateTime time, String fileType) {
            this.byteBuffer = byteBuffer;
            this.time = time;
            this.fileType = fileType;
        }

        public byte[] getByteBuffer() {
            return byteBuffer;
        }

        public ZonedDateTime getTime() {
            return time;
        }

        public String getFileType() {
            return fileType;
        }
    }



    public CacheEntityBytes get(String url) {
        SoftReference<CacheEntityBytes> value = cache.get(url);
        if (value != null) {
            CacheEntityBytes res = value.get();
            if (res == null) {
                // 已被清理
                cache.remove(url);
            } else {
                return res;
            }
        }
        return null;
    }

    public CacheEntityBytes put(String url, byte[] body, ZonedDateTime time, String fileType) {
        CacheEntityBytes cacheEntity = new CacheEntityBytes(body, time, fileType);
        cache.put(url, new SoftReference<>(cacheEntity));
        return cacheEntity;
    }

}
