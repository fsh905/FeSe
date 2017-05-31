package xyz.fefine.entity;

import bid.fese.entity.SeRequest;
import bid.fese.entity.SeResponse;

import java.lang.reflect.Method;

/**
 * Created by feng_sh on 17-5-31.
 * 默认拦截器
 */
public class DefaultInterceptor implements Interceptor
{
    @Override
    public void before(SeRequest request, SeResponse response, Method method, Object[] args) {

    }

    @Override
    public void after(SeRequest request, SeResponse response, Method method, Object[] args) {

    }

    @Override
    public void afterThrowing(SeRequest request, SeResponse response, Method method, Object[] args, Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void afterFinally(SeRequest request, SeResponse response, Method method, Object[] args) {

    }
}
