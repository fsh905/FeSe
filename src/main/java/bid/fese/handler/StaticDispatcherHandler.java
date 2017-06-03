package bid.fese.handler;

import bid.fese.common.ApplicationContext;
import bid.fese.common.Constants;
import bid.fese.entity.SeHeader;
import bid.fese.entity.SeRequest;
import bid.fese.entity.SeResponse;

/**
 * Created by feng_sh on 17-5-25.
 * 静态资源分配
 */
public class StaticDispatcherHandler implements DispatcherHandler {
    @Override
    public void initHandler() {}

    @Override
    public void handlerRequest(SeRequest request, SeResponse response) {
        String url = request.getUrl();
        if (request.getHeader().getHeaderParameter(SeHeader.ACCEPT_ENCODING) != null && isSupportGZIP(url)) {
            response.setSupportGZIP(request.getHeader().getHeaderParameter(SeHeader.ACCEPT_ENCODING).contains("gzip"));
        }

        if (url.length() == 0 || url.length() == 1) {
            response.writeFile(ApplicationContext.get(Constants.CONFIG_STATIC_RESOURCE_PATH) + "/" + ApplicationContext.get(Constants.CONFIG_INDEX));
            response.flush();
        } else {
            response.writeFile(ApplicationContext.get(Constants.CONFIG_STATIC_RESOURCE_PATH) + url);
            response.flush();
        }
    }

    /**
     * 获取后缀
     * @param url 链接
     * @return 后缀 or null
     */
    private boolean isSupportGZIP(String url) {
        int l = url.lastIndexOf('.');
        if (l != -1) {
            String postfix = url.substring(l + 1).toLowerCase();
            return !Constants.NO_GZIP_STATIC_RESOURCE.matcher(postfix).matches();
        }
        return true;
    }

}
