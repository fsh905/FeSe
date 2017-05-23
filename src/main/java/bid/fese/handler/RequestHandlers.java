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

    private RequestHandlers() {

    }

    public static RequestHandler getHandler() {
        int i = (int) (Math.random() * 10) % handlers.size();
        return handlers.get(i);
    }

    public static void addRequestHandler(RequestHandler handler) {
        handlers.add(handler);
    }

    public static void addRequest(SeRequest request) {
        RequestHandler handler = requestMap.get(request);
        if (handler != null) {
            handler.addRequest(request);
        } else {
            int i = (int) (Math.random() * 10) % handlers.size();
            handlers.get(i).addRequest(request);
        }
    }


}
