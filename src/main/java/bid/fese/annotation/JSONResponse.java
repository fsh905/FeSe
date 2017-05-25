package bid.fese.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by feng_sh on 17-5-25.
 * 将请求转化为json
 */
@Target(value={ElementType.METHOD})				//方法支持的注解声明
@Retention(RetentionPolicy.RUNTIME)		//运行期间保留
public @interface JSONResponse {
    boolean isConvert() default true;
}
