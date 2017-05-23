package bid.fese.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by feng_sh on 5/23/2017.
 * 存放全局的一些参数, 单例模式
 */
public class ApplicationContext {

    private static ApplicationContext applicationContext ;
    private final Map<String, Object> context = new ConcurrentHashMap<>();

    private ApplicationContext() {}

    public static ApplicationContext getApplicationContext() {
        if (applicationContext == null) {
            applicationContext = new ApplicationContext();
        }
        return applicationContext;
    }

    public Object get(String serviceName) {
        return context.get(serviceName);
    }

    public void put(String serviceName, Object service) {
        context.put(serviceName, service);
    }

}
