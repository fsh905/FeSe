package bid.fese.common;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by feng_sh on 5/23/2017.
 * 存放全局的一些参数, 单例模式
 */
public class ApplicationContext {

    private static ApplicationContext applicationContext ;
    private static final Map<String, Object> context = new ConcurrentHashMap<>();

    private ApplicationContext() {}

    public static ApplicationContext getApplicationContext() {
        if (applicationContext == null) {
            applicationContext = new ApplicationContext();
        }
        return applicationContext;
    }

    public static String getClassPath() {
        String path = (String) get(Constants.CLASS_PATH);
        if (path == null) {
            path = ClassLoader.class.getResource("/").getPath();
            put(Constants.CLASS_PATH, path);
        }
        return path;
    }

    public static Object get(String serviceName) {
        return context.get(serviceName);
    }

    public static void put(String serviceName, Object service) {
        context.put(serviceName, service);
    }

}
