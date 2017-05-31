package bid.fese.handler;

import bid.fese.entity.SeHeader;
import bid.fese.entity.SeRequest;
import bid.fese.entity.SeResponse;

/**
 * Created by feng_sh on 17-5-25.
 * 动态请求处理
 */
public class DynamicDispatcherHandler implements DispatcherHandler {
    @Override
    public void initHandler() {
        System.out.println("init dynamic");
    }

    @Override
    public void handlerRequest(SeRequest request, SeResponse response) {
        response.getPrintWriter().print("Dynamic request handler is not implement");
        response.getPrintWriter().flush();
        response.flush();
    }
}
