package bid.fese.handler;

import bid.fese.common.ApplicationContext;
import bid.fese.common.Constants;
import bid.fese.entity.SeRequest;
import bid.fese.entity.StaticSoftCacheBytes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by feng_sh on 5/23/2017.
 * requestHandler队列
 */
public class RequestHandlers {

    private static final Logger logger = LogManager.getLogger(RequestHandlers.class);
    // 这里可以采用优先队列
    private final List<RequestHandler> handlers = new ArrayList<>(4);
    // 静态文件处理
    // 这里没有必要使用ThreadLocal, 因为staticDispatcherHandler中不会出现多线程冲突的问题
//    private static ThreadLocal<DispatcherHandler> staticDispatcherHandlerThreadLocal = new ThreadLocal<>();
    // 每个线程正在处理的请求数, 可用于负载均衡
    // 暂时没有太好的方法实现
//    private static ThreadLocal<Integer> requestCount = new ThreadLocal<Integer>() {
//        @Override
//        protected Integer initialValue() {
//            return 0;
//        }
//    };

    private DispatcherHandler dynamicDispatcherHandler;
    private DispatcherHandler staticDispatcherHandler;

    // 这里之所以使用ThreadLocal是因为
    // １,底层采用ｍａｐ实现
    // 多个线程同时对
    private ThreadLocal<StaticSoftCacheBytes> cacheThreadLocal = new ThreadLocal<>();

    /**
     * 进行资源初始化
     */
    public void initHandlers() {
        // 全局单例的进行初始化
        initDynamicDispatcherHandler();
        initStaticDispatcherHandler();
    }

    /**
     * 初始化动态资源处理器
     */
    private void initDynamicDispatcherHandler() {
        if (dynamicDispatcherHandler == null) {
            String name = (String) ApplicationContext.get(Constants.CONFIG_DYNAMIC_REQUEST_HANDLER);
            if (name == null) {
                dynamicDispatcherHandler = new DynamicDispatcherHandler();
            } else {
                try {
                    dynamicDispatcherHandler = (DispatcherHandler) Class.forName(name).newInstance();
                    logger.info("use specific dynamic dispatcher handler;" + name);
                } catch (InstantiationException e) {
                    logger.error("can't convert , you must implement! use default", e);
                    dynamicDispatcherHandler = new DynamicDispatcherHandler();
                } catch (IllegalAccessException e) {
                    logger.error("can't use dynamic request handler! use default", e);
                    dynamicDispatcherHandler = new DynamicDispatcherHandler();
                } catch (ClassNotFoundException e) {
                    logger.error("can't find dynamic request handler! use default", e);
                    dynamicDispatcherHandler = new DynamicDispatcherHandler();
                }
            }
        }
        // 初始化
        dynamicDispatcherHandler.initHandler();
    }

    private void initStaticDispatcherHandler() {
        staticDispatcherHandler = new StaticDispatcherHandler();
    }

    /**
     * 这里的随机方式有问题， 可以采用负载均衡，根据当前handler的连接数来判断选取哪一个
     *
     * @return handler
     */
    public RequestHandler getHandler() {
        int i = (int) (Math.random() * 10) % handlers.size();
        logger.info("dispatcher to handler-" + i);
        return handlers.get(i);
    }

    /**
     * 静态资源处理
     *
     * @return 静态资源处理器， 线程独享
     */
    public DispatcherHandler getStaticDispatcherHandler() {
        return staticDispatcherHandler;
    }

    /**
     * 这里的cache使用threadLocal， 防止冲突
     * todo 需要重新测试缓存机制的使用方式
     *
     * @return 缓存器
     */
    public StaticSoftCacheBytes getCache() {
        StaticSoftCacheBytes cache = cacheThreadLocal.get();
        if (cache == null) {
            cache = new StaticSoftCacheBytes();
            cacheThreadLocal.set(cache);
        }
        return cache;
    }

    /**
     * 线程动态资源分配
     *
     * @return 动态资源分陪器， 线程独享
     */
    public DispatcherHandler getDynamicDispatcherHandler() {
        return dynamicDispatcherHandler;
    }

    /**
     * 增加请求处理器
     *
     * @param handler 请求处理器
     */
    public void addRequestHandler(RequestHandler handler) {
        handlers.add(handler);
    }

    /**
     * 为新请求分配请求处理器
     *
     * @param request 请求
     */
    public void addRequest(SeRequest request) {
        getHandler().addRequest(request);
    }

//    public static void addRequestCount() {
//        requestCount.set(requestCount.get() + 1);
//    }

//    public static void minusRequestCount() {
//        requestCount.set(requestCount.get() - 1);
//    }


}
