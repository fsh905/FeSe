package bid.fese.handler;

import bid.fese.entity.SeRequest;
import bid.fese.entity.SeResponse;

/**
 * Created by feng_sh on 17-5-25.
 * 转发
 */
public interface DispatcherHandler {
    void handlerRequest(SeRequest request, SeResponse response);
}
