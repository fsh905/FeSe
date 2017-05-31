package xyz.fefine;

import bid.fese.common.ApplicationContext;
import bid.fese.common.Constants;
import bid.fese.entity.SeRequest;
import bid.fese.entity.SeResponse;
import bid.fese.handler.DispatcherHandler;
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
    @Override
    public void initHandler() {
        String packagePath = (String) ApplicationContext.get(Constants.CLASS_PATH);
        // 初始化配置
        reqHelper.initRequestHandler(packagePath);
    }

    @Override
    public void handlerRequest(SeRequest request, SeResponse response) {

        String url = request.getUrl();
        // handler处理
        RequestHandler handler = null;
        try {
            handler = reqHelper.findRequestHandler(url);
        } catch (NoHandlerFoundException e) {
            e.printStackTrace();
        }

        if(handler == null){

            try {
                response.getOutStream().write("no this handler,please inout a correct url".getBytes());
                response.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return ;
        }
        //2016/4/3
        //判断请求方式，增加PUT和DELETE请求方式
        if(!handler.getRequestMethod().toLowerCase().equals(reqHelper.getRealRequestMathod(request)) ){
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


}
