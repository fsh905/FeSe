package bid.fese.handler;

import bid.fese.common.ApplicationContext;
import bid.fese.common.Constants;
import bid.fese.entity.SeRequest;
import bid.fese.entity.SeResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by feng_sh on 17-5-25.
 * 单例模式， 用于快速加载
 */
public class RequestDispatcherHandler implements DispatcherHandler {

    private static char[] postfix = (char[]) ApplicationContext.get(Constants.CONFIG_REQUEST_POSTFIX);
    private static Logger logger = LogManager.getLogger(RequestDispatcherHandler.class);

    @Override
    public void handlerRequest(SeRequest request, SeResponse response) {
        if (isStatic(request.getUrl())) {
            logger.info(request.getRemoteAddress() + " ["+request.getMethod()+"] " + request.getUrl() + " [static]" + (request.isKeepAlive() ? "[keep-Alive]" : "[no-keep-alive]"));
            logger.debug(request.getUrl() + " is assign to static:" + System.currentTimeMillis());
            RequestHandlers.getStaticDispatcherHandler().handlerRequest(request, response);
        } else {
            logger.info(request.getRemoteAddress() + " ["+request.getMethod()+"] " + request.getUrl() + " [dynamic]" + (request.isKeepAlive() ? "[keep-Alive]" : "[no-keep-alive]"));
            logger.debug(request.getUrl() + " is assign to dynamic:" + System.currentTimeMillis());
            RequestHandlers.getDynamicDispathcerHandler().handlerRequest(request, response);
        }
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

}
