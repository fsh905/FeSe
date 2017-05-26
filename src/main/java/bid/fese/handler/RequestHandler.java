package bid.fese.handler;

import bid.fese.entity.SeRequest;
import bid.fese.entity.SeResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by feng_sh on 5/23/2017.
 * 多线程用于处理连接
 */
public class RequestHandler implements Runnable {

    // 每个线程一个请求队列, 但是插入和删除是属于不同的线程
    // 采用ArrayList， 指定初始大小为10
    // 可通过制定不同的大小来应对不同的环境
    private List<SeRequest> requests = new ArrayList<>(10);
    private final static Logger logger = LogManager.getLogger(RequestHandler.class);

    public void addRequest(SeRequest request) {
        logger.debug("syn start:" + System.currentTimeMillis());
        synchronized (requests) {
            requests.add(request);
            requests.notifyAll();
        }
        logger.debug("syn end:" + System.currentTimeMillis());
        // 请求数计算， 用于负载均衡
        RequestHandlers.addRequestCount();
    }


    @Override
    public void run() {

        SeRequest request = null;
        RequestHandler handler = null;

        while (true) {
            synchronized (requests) {
                while (requests.isEmpty()) {
                    try {
                        requests.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                request = requests.remove(0);
            }
            RequestHandlers.minusRequestCount();
            SeResponse response = new SeResponse(request);
            logger.debug("start dispatcher request:" + System.currentTimeMillis());
            // 这里使用的是ThreadLocal， 针对不同的线程分配单独的handler
            RequestHandlers.getRequestDispatcherHandler().handlerRequest(request, response);
        }
    }
}
