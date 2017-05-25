package bid.fese.handler;

import bid.fese.entity.SeRequest;
import bid.fese.entity.SeResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by feng_sh on 5/23/2017.
 * 多线程用于处理连接
 */
public class RequestHandler implements Runnable {

    // 请求队列
    private static List<SeRequest> requests = Collections.synchronizedList(new ArrayList<>());

    public void addRequest(SeRequest request) {
        synchronized (requests) {
            requests.add(request);
            requests.notify();
        }
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
            System.out.println("----------request handler------------");
            System.out.println(request.getUrl());
            System.out.println(request.getMethod());

            SeResponse response = new SeResponse(request);

            RequestHandlers.getRequestDispatcherHandler().handlerRequest(request, response);

            System.out.println("------------handler end--------------");
        }
    }
}
