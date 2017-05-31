package xyz.fefine.entity;

import bid.fese.entity.SeRequest;
import bid.fese.entity.SeResponse;

import java.lang.reflect.Method;

/**
 * Created by feng_sh on 17-5-31.
 */
public interface Interceptor {


    void before(SeRequest request, SeResponse response, Method method, Object[] args);

    void after(SeRequest request,SeResponse response,Method method, Object[] args);

    void afterThrowing(SeRequest request,SeResponse response,Method method, Object[] args, Throwable throwable);

    void afterFinally(SeRequest request,SeResponse response,Method method, Object[] args);

}