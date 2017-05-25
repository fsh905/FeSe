package bid.fese.annotation;

import bid.fese.entity.SeRequest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by feng_sh on 17-5-25.
 * 地址注解
 */
@Target(value={ElementType.METHOD, ElementType.TYPE})				//方法和类均支持的注解声明
@Retention(RetentionPolicy.RUNTIME)		//运行期间保留
public @interface Path {

    String value() default "/";				//默认为/
    SeRequest.METHOD requestMethod() default SeRequest.METHOD.GET;	//默认为get请求//暂时不实现

}
