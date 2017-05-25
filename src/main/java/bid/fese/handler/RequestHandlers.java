package bid.fese.handler;

import bid.fese.entity.SeRequest;

import java.util.*;

/**
 * Created by feng_sh on 5/23/2017.
 * requestHandler队列
 */
public class RequestHandlers {

    private static final List<RequestHandler> handlers = Collections.synchronizedList(new ArrayList<>());
    private static Map<SeRequest, RequestHandler> requestMap = new WeakHashMap<>();
    // 静态文件处理
    private static ThreadLocal<StaticDispatcherHandler> staticDispatcherHandlerThreadLocal = new ThreadLocal<>();
    // 动态请求处理
    private static ThreadLocal<DynamicDispatcherHandler> dynamicDispatcherHandlerThreadLocal = new ThreadLocal<>();
    // 请求分配
    private static ThreadLocal<RequestDispatcherHandler> requestDispatcherHandlerThreadLocal = new ThreadLocal<>();

    private RequestHandlers() {}

    public static RequestHandler getHandler() {
        int i = (int) (Math.random() * 10) % handlers.size();
        return handlers.get(i);
    }

    public static StaticDispatcherHandler getStaticDispatcherHandler() {
        StaticDispatcherHandler handler = staticDispatcherHandlerThreadLocal.get();
        if (handler == null) {
            handler = new StaticDispatcherHandler();
            staticDispatcherHandlerThreadLocal.set(handler);
        }
        return handler;
    }

    public static DynamicDispatcherHandler getDynamicDispathcerHandler() {
        DynamicDispatcherHandler handler = dynamicDispatcherHandlerThreadLocal.get();
        if (handler == null) {
            handler = new DynamicDispatcherHandler();
            dynamicDispatcherHandlerThreadLocal.set(handler);
        }
        return handler;
    }

    public static RequestDispatcherHandler getRequestDispatcherHandler() {
        RequestDispatcherHandler handler = requestDispatcherHandlerThreadLocal.get();
        if (handler == null) {
            handler = new RequestDispatcherHandler();
            requestDispatcherHandlerThreadLocal.set(handler);
        }
        return handler;
    }

    public static void addRequestHandler(RequestHandler handler) {
        handlers.add(handler);
    }

    public static void addRequest(SeRequest request) {
        // 有什么用呢
        RequestHandler handler = requestMap.get(request);
        if (handler != null) {
            handler.addRequest(request);
        } else {
            int i = (int) (Math.random() * 10) % handlers.size();
            handlers.get(i).addRequest(request);
        }
    }


}
