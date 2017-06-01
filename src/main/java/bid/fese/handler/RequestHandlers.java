package bid.fese.handler;

import bid.fese.common.ApplicationContext;
import bid.fese.common.Constants;
import bid.fese.entity.SeRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.zip.DeflaterInputStream;

/**
 * Created by feng_sh on 5/23/2017.
 * requestHandler队列
 */
public class RequestHandlers {

    private static final Logger logger = LogManager.getLogger(RequestHandlers.class);
    // 这里可以采用优先队列
    private static final List<RequestHandler> handlers = new ArrayList<>(4);
    // 静态文件处理
    private static ThreadLocal<DispatcherHandler> staticDispatcherHandlerThreadLocal = new ThreadLocal<>();
    // 每个线程正在处理的请求数, 可用于负载均衡
    private static ThreadLocal<Integer> requestCount = new ThreadLocal<Integer>() {
        @Override
        protected Integer initialValue() {
            return 0;
        }
    };

    private static DispatcherHandler dynamicDispatcherHandler;

    private RequestHandlers() {}

    /**
     * 进行资源初始化
     */
    public static void initHandlers() {
        // 全局单例的进行初始化
        initDynamicDispatcherHandler();
    }

    /**
     * 初始化动态资源处理器
     */
    private static void initDynamicDispatcherHandler() {
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

    /**
     * 这里的随机方式有问题， 可以采用负载均衡，根据当前handler的连接数来判断选取哪一个
     * @return handler
     */
    public static RequestHandler getHandler() {
        int i = (int) (Math.random() * 10) % handlers.size();
        logger.info("dispatcher to handler-" + i);
        return handlers.get(i);
    }

    /**
     * 静态资源处理
     * @return 静态资源处理器， 线程独享
     */
    public static DispatcherHandler getStaticDispatcherHandler() {
        DispatcherHandler handler = staticDispatcherHandlerThreadLocal.get();
        if (handler == null) {
            handler = new StaticDispatcherHandler();
            staticDispatcherHandlerThreadLocal.set(handler);
        }
        logger.debug("get static dispatcher");
        return handler;
    }

    /**
     * 线程动态资源分配
     * @return 动态资源分陪器， 线程独享
     */
    public static DispatcherHandler getDynamicDispatcherHandler() {
        return dynamicDispatcherHandler;
    }

    /**
     * 增加请求处理器
     * @param handler 请求处理器
     */
    public static void addRequestHandler(RequestHandler handler) {
        handlers.add(handler);
    }

    /**
     * 为新请求分配请求处理器
     * @param request 请求
     */
    public static void addRequest(SeRequest request) {
        getHandler().addRequest(request);
    }

    public static void addRequestCount() {
        requestCount.set(requestCount.get() + 1);
    }

    public static void minusRequestCount() {
        requestCount.set(requestCount.get() - 1);
    }


}
