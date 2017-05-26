package bid.fese.handler;

import bid.fese.entity.SeRequest;

import java.util.*;

/**
 * Created by feng_sh on 5/23/2017.
 * requestHandler队列
 */
public class RequestHandlers {

    private static final List<RequestHandler> handlers = new ArrayList<>(4);
//    private static Map<SeRequest, RequestHandler> requestMap = new WeakHashMap<>();
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

    public static RequestHandler getHandler() {
        int i = (int) (Math.random() * 10) % handlers.size();
        return handlers.get(i);
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
        // 有什么用呢
//        RequestHandler handler = requestMap.get(request);
//        if (handler != null) {
//            handler.addRequest(request);
//        } else {
        // 随机分配

        int i = (int) (Math.random() * 10) % handlers.size();
        handlers.get(i).addRequest(request);
//        }
    }

    public static void addRequestCount() {
        requestCount.set(requestCount.get() + 1);
    }

    public static void minusRequestCount() {
        requestCount.set(requestCount.get() - 1);
    }


}
