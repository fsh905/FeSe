package bid.fese.handler;

import bid.fese.entity.SeRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Created by feng_sh on 5/23/2017.
 * requestHandler队列
 */
public class RequestHandlers {

    private static final Logger logger = LogManager.getLogger(RequestHandlers.class);
    // 这里可以采用优先队列
    private static final List<RequestHandler> handlers = new ArrayList<>(4);
    // 静态文件处理
    private static ThreadLocal<StaticDispatcherHandler> staticDispatcherHandlerThreadLocal = new ThreadLocal<>();
    // 动态请求处理
    private static ThreadLocal<DynamicDispatcherHandler> dynamicDispatcherHandlerThreadLocal = new ThreadLocal<>();
    // 请求分配
    private static ThreadLocal<RequestDispatcherHandler> requestDispatcherHandlerThreadLocal = new ThreadLocal<>();
    // 每个线程正在处理的请求数, 可用于负载均衡
    private static ThreadLocal<Integer> requestCount = new ThreadLocal<Integer>() {
        @Override
        protected Integer initialValue() {
            return 0;
        }
    };

    private RequestHandlers() {}

    /**
     * 这里的随机方式有问题， 可以采用负载均衡，根据当前handler的连接数来判断选取哪一个
     * @return handler
     */
    public static RequestHandler getHandler() {
        int i = (int) (Math.random() * 10) % handlers.size();
        RequestHandler handler = handlers.get(i);
        logger.debug("this request is assign to handler:");
        return handler;
    }

    /**
     * 静态资源处理
     * @return 静态资源处理器， 线程独享
     */
    public static StaticDispatcherHandler getStaticDispatcherHandler() {
        StaticDispatcherHandler handler = staticDispatcherHandlerThreadLocal.get();
        if (handler == null) {
            handler = new StaticDispatcherHandler();
            staticDispatcherHandlerThreadLocal.set(handler);
        }
        return handler;
    }

    /**
     * 线程动态资源分配
     * @return 动态资源分陪器， 线程独享
     */
    public static DynamicDispatcherHandler getDynamicDispathcerHandler() {
        DynamicDispatcherHandler handler = dynamicDispatcherHandlerThreadLocal.get();
        if (handler == null) {
            handler = new DynamicDispatcherHandler();
            dynamicDispatcherHandlerThreadLocal.set(handler);
        }
        return handler;
    }

    /**
     * @return 资源分配器， 线程独享
     */
    public static RequestDispatcherHandler getRequestDispatcherHandler() {
        RequestDispatcherHandler handler = requestDispatcherHandlerThreadLocal.get();
        if (handler == null) {
            handler = new RequestDispatcherHandler();
            requestDispatcherHandlerThreadLocal.set(handler);
        }
        return handler;
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
        int i = (int) (Math.random() * 10) % handlers.size();
        handlers.get(i).addRequest(request);
    }

    public static void addRequestCount() {
        requestCount.set(requestCount.get() + 1);
    }

    public static void minusRequestCount() {
        requestCount.set(requestCount.get() - 1);
    }


}
