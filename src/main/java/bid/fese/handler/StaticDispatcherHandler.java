package bid.fese.handler;

import bid.fese.common.ApplicationContext;
import bid.fese.common.Constants;
import bid.fese.entity.SeRequest;
import bid.fese.entity.SeResponse;

/**
 * Created by feng_sh on 17-5-25.
 * 静态资源分配
 */
public class StaticDispatcherHandler implements DispatcherHandler {
    @Override
    public void handlerRequest(SeRequest request, SeResponse response) {
        String url = request.getUrl();
        System.out.println("start handler:" + url);
        if (url.length() == 0 || url.length() == 1) {
            response.writeFile(ApplicationContext.get(Constants.CONFIG_STATIC_RESOURCE_PATH) + "/" + ApplicationContext.get(Constants.CONFIG_INDEX));
            response.flush();
        } else {
            response.writeFile(ApplicationContext.get(Constants.CONFIG_STATIC_RESOURCE_PATH) + url);
            response.flush();
        }
    }
}
