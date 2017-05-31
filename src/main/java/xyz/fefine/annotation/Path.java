package xyz.fefine.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by feng_sh on 17-5-31.
 * 路径注解
 */
@Target(value={ElementType.METHOD,ElementType.TYPE})				//方法和类均支持的注解声明
@Retention(RetentionPolicy.RUNTIME)		//运行期间保留
public @interface Path {

    public String value() default "/";				//默认为/
    public String requestMethod() default "GET";	//默认为get请求//暂时不实现


}
