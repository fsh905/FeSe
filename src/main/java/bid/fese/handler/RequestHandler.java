package bid.fese.handler;

import bid.fese.entity.SeRequest;
import bid.fese.entity.SeResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by feng_sh on 5/23/2017.
 * 多线程用于处理连接
 */
public class RequestHandler implements Runnable {

    // 每个线程一个请求队列
    // 插入删除较多， 采用linkedList
    private List<SeRequest> requests = new LinkedList<>();

    public void addRequest(SeRequest request) {
        synchronized (requests) {
            requests.add(request);
            requests.notifyAll();
        }
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
            // 这里使用的是ThreadLocal， 针对不同的线程分配单独的handler
            RequestHandlers.getRequestDispatcherHandler().handlerRequest(request, response);
        }
    }
}
