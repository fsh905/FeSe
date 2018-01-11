package bid.fese.handler;

import bid.fese.common.ApplicationContext;
import bid.fese.common.Constants;
import bid.fese.entity.SeRequest;
import bid.fese.entity.SeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;

/**
 * Created by feng_sh on 5/23/2017.
 * 多线程用于处理连接
 */
public class RequestHandler implements Runnable {


    private final static Logger logger = LoggerFactory.getLogger(RequestHandler.class);
    private static final ApplicationContext context = ApplicationContext.getInstance();
    
    private static char[] postfix = context.getString(Constants.REQUEST_POSTFIX).toCharArray();
    private final RequestHandlers requestHandlers;
    // 每个线程一个请求队列, 但是插入和删除是属于不同的线程
    // 采用ArrayList， 指定初始大小为10
    // 可通过制定不同的大小来应对不同的环境
//    private List<SeRequest> requests = new ArrayList<>(10);
    private final LinkedList<SeRequest> requests = new LinkedList<>();

    public RequestHandler(RequestHandlers requestHandlers) {
        this.requestHandlers = requestHandlers;
    }

    private static boolean isStatic(String url) {
        if (url.length() <= postfix.length) {
            return true;
        }

        // index.do
        //      .do
        //      012
        // 01234567
        char[] chars = url.toCharArray();
        for (int i = 0; i < postfix.length; i++) {
            if (chars[chars.length - postfix.length + i] - postfix[i] != 0 && chars[chars.length - postfix.length + i] != 32) {
                return true;
            }
        }
        return false;
    }

    public void addRequest(SeRequest request) {
        synchronized (requests) {
            requests.addLast(request);
            requests.notify();
        }
        logger.debug("add end " + requests.size());
    }

    @Override
    public void run() {
        SeRequest request = null;
        outter:
        while (true) {
            synchronized (requests) {
                while (requests.isEmpty()) {
                    try {
                        requests.wait();
                    } catch (InterruptedException e) {
                        logger.error("wait for request thread has been interrupted", e);
                        continue outter;
                    }
                }
                request = requests.removeFirst();
            }
            SeResponse response = new SeResponse(request, requestHandlers);
            dispatcherRequest(request, response);
        }
    }

    /**
     * 请求分配
     *
     * @param request  请求
     * @param response 相应
     */
    private void dispatcherRequest(SeRequest request, SeResponse response) {
        logger.debug("start dispatcher request:" + request.getUrl());
        // 这里使用的是ThreadLocal， 针对不同的线程分配单独的handler
        if (isStatic(request.getUrl())) {
            logger.info(request.getRemoteAddress() + " [" + request.getMethod() + "] " + request.getUrl() + " [static]");
            requestHandlers.getStaticDispatcherHandler().handlerRequest(request, response);
        } else {
            logger.info(request.getRemoteAddress() + " [" + request.getMethod() + "] " + request.getUrl() + " [dynamic]");
            requestHandlers.getDynamicDispatcherHandler().handlerRequest(request, response);
        }
    }

}
