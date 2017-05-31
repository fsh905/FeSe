package xyz.fefine.entity;

import bid.fese.entity.SeRequest;
import bid.fese.entity.SeResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.fefine.handler.RequestHandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Created by feng_sh on 17-5-31.
 */

public class MethodInterceptor implements InvocationHandler {

    private Object target;
    private Interceptor interceptor;
    private SeRequest request;
    private SeResponse response;
    private RequestHandler handler;

    private static final Logger logger = LogManager.getLogger(MethodInterceptor.class);

    public MethodInterceptor(RequestHandler handler , Interceptor interceptor,SeRequest request,SeResponse response) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        this.interceptor = interceptor;
        this.request = request;
        this.response = response;
        this.target = Class.forName(handler.getClassName()).newInstance();
        this.handler = handler;
    }

    /**
     *
     * @return 执行method返回
     * @throws Throwable　错误
     */
    public Object invokeMethod() throws Throwable{

        return invoke(null, this.handler.getMethod(), this.handler.getObjs());

    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = null;
        try{
            interceptor.before(request, response, method, args);
            result = method.invoke(target, args);
            interceptor.after(request, response, method, args);
        }catch(Throwable throwable){
            logger.error(method.getName() + " argument doesn't match, the args is:" + Arrays.toString(args), throwable);
            interceptor.afterThrowing(request, response, method, args, throwable);
        }finally{
            interceptor.afterFinally(request, response, method, args);
        }
        return result;
    }
}

