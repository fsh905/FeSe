package xyz.fefine.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by feng_sh on 17-5-31.
 * 返回json数据注解
 */
@Target(value={ElementType.METHOD})		//方法支持的注解声明
@Retention(RetentionPolicy.RUNTIME)		//运行期间保留
public @interface JsonData {
}
