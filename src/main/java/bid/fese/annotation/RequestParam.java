package bid.fese.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by feng_sh on 17-5-25.
 * 请求参数注解
 */
@Target(ElementType.PARAMETER)				//参数声明
@Retention(RetentionPolicy.RUNTIME)		//运行期间保留
public @interface RequestParam {
    String value() default "";
}
