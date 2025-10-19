package com.secondhand.common.doc;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Document {
    //接口作者
    String author();

    //接口名称
    String name();

    //接口描述
    String desc();

    //接口模块
    String module();

    String update() default "2021-07-01";

    String creatAt() default "2021-07-01";

    String version() default "1.0";

    Platform[] platform() default {Platform.APP};

    ApiParam[] request() default {};

    ApiParam[] response() default {};
}
