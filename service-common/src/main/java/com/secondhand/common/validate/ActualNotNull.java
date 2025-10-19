package com.secondhand.common.validate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 确实为null时才不通过验证
 *
 * @author xiaohua
 * @version 1.0
 * @date 2019-12-31 11:34
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ActualNotNull {

    String[] name();

    String[] message() default {};
}