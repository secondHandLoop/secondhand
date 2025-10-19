package com.secondhand.common.cashe;

/**
 * jerry on 2016/12/23.
 */
@java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE})
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface ServiceCache {

    int time() default 300;

    String cacheProvider() default "redisCacheProvider";

    String[] keys() default "";
}
