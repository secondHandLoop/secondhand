package com.secondhand.common.doc.entity;



import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class Doc {

    //请求参数
    List<Param> request;
    //相应参数
    List<Param> response;
    //接口名称
    private String name;
    //服务名称
    private String service;
    //接口版本
    private String version;
    //接口作者
    private String author;
    //微服务模块
    private String module;
    //接口描述
    private String desc;
    //文档构建时间
    private long update;
    private long creatAt;
    private List<String> platform;

}

