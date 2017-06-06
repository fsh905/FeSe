package bid.fese.handler;

import bid.fese.entity.SeRequest;
import bid.fese.entity.SeResponse;

/**
 * Created by feng_sh on 17-5-25.
 * 转发
 */
public interface DispatcherHandler {
    /**
     * 初始化时调用
     */
    void initHandler();

    void handlerRequest(SeRequest request, SeResponse response);
}
