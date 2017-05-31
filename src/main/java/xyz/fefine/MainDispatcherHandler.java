package xyz.fefine;

import bid.fese.common.ApplicationContext;
import bid.fese.common.Constants;
import bid.fese.entity.SeRequest;
import bid.fese.entity.SeResponse;
import bid.fese.handler.DispatcherHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.fefine.exception.NoHandlerFoundException;
import xyz.fefine.handler.RequestHandler;
import xyz.fefine.handler.RequestHelper;

import java.io.IOException;

/**
 * Created by feng_sh on 17-5-31.
 * 动态请求分配
 */
public class MainDispatcherHandler implements DispatcherHandler{
    private RequestHelper reqHelper = new RequestHelper();
    private static final Logger logger = LogManager.getLogger(MainDispatcherHandler.class);
    @Override
    public void initHandler() {
        String packagePath = (String) ApplicationContext.get(Constants.CLASS_PATH) +
                ApplicationContext.get("rest_config_file");
        logger.info("scanner package path:" + packagePath);
        // 初始化配置
        reqHelper.initRequestHandler(packagePath);
    }

    @Override
    public void handlerRequest(SeRequest request, SeResponse response) {

        String url = request.getUrl();
        logger.info("request url:" + url);
        // handler处理
        RequestHandler handler = null;
        try {
            handler = reqHelper.findRequestHandler(url);
        } catch (NoHandlerFoundException e) {
            logger.error("can't find request handler, url is:" + url);
            try {
                response.getOutStream().write("no this handler,please inout a correct url".getBytes());
                response.flush();
            } catch (IOException es) {
                logger.error("response writer error", es);
            }
            return ;
        }

        //判断请求方式，增加PUT和DELETE请求方式
        if(!handler.getRequestMethod().toLowerCase().equals(reqHelper.getRealRequestMathod(request)) ){
            logger.error("request method wrong");
            try {
                response.getOutStream().write("requestMathod is not support".getBytes());
                response.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return ;
        }
        // 执行
        try {
            handler.invokeMethod(request, response,reqHelper.getInterceptor());
        } catch (NoHandlerFoundException e) {
            logger.error("handler invoke method error, url:" + url, e);
        }
    }
}