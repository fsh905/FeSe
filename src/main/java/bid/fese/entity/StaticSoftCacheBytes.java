package bid.fese.entity;

import java.lang.ref.SoftReference;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by feng_sh on 6/2/2017.
 * 静态资源缓存， 仅缓存byte[]防止使用ByteBuffer出现的bug
 * 采用SoftReference进行连接， 当为软引用时，只有当快要内存溢出时才会进行回收
 */
public class StaticSoftCacheBytes {
    // 之所以使用HashMap而不是CurrentHashMap
    // 因为前面使用的是ThreadLocal，没有必要进行线程控制
    private Map<String, SoftReference<CacheEntityBytes>> cache = new HashMap<>();

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

}
