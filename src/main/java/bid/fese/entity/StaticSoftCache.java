package bid.fese.entity;

import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by feng_sh on 6/2/2017.
 * 静态资源缓存
 */
public class StaticSoftCache {

    private Map<String, SoftReference<byte[]>> cache = new ConcurrentHashMap<>();

    public byte[] get(String url) {
        SoftReference<byte[]> value = cache.get(url);
        if (value != null) {
            byte[] res = value.get();
            if (res == null) {
                // 已被清理
                cache.remove(url);
            } else {
                return res;
            }
        }
        return null;
    }

    public void put(String url, byte[] body) {
        cache.put(url, new SoftReference<byte[]>(body));
    }

}
