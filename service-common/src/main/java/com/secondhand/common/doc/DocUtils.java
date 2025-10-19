package com.secondhand.common.doc;



import com.alibaba.fastjson.JSON;
import com.secondhand.common.doc.entity.Doc;
import com.secondhand.common.doc.entity.Param;
import com.secondhand.common.tool.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
public class DocUtils {
    //后期直接上传到oss
    private static final String path = new File("").getAbsolutePath() + File.separator+"doc";

    public static void generate(String packagePath) {
        if (StringUtils.isEmpty(packagePath)) {
            log.error("package path is null");
            return;
        }
        log.info("starting generate doc......" + path);
        List<Class<?>> classes = PackageUtils.getClasses(packagePath);
        List<Doc> list = new ArrayList<>();
        classes.forEach(aClass -> {
            log.info(aClass.getName());
            if (aClass.isAnnotationPresent(Document.class)) {
                Document document = aClass.getAnnotation(Document.class);
                Component component = aClass.getAnnotation(Component.class);
                String service = component.value();
                //接口名称
                String apiName = document.name();
                //接口所属微服务模块
                String module = document.module();
                //接口描述
                String desc = document.desc();
                //接口版本
                String version = document.version();
                //接口作者
                String author = document.author();
                //修改时间
                String update = DateUtils.dateString(new Date(), "yyyy-MM-dd");
                String creatAt = document.creatAt();
                // 使用平台
                Platform[] platforms = document.platform();

                Doc doc = new Doc().setName(apiName).setAuthor(author).setUpdate(new DateTime(update).getMillis() / 1000).setCreatAt(new DateTime(creatAt).getMillis() / 1000)
                        .setService(service.replace(version, "")).setModule(module).setDesc(desc).setVersion(version);
                //获取平台集合
                if (!ObjectUtils.isEmpty(platforms)) {
                    List<String> platformStrings = new ArrayList<>();
                    for (Platform platform : platforms) {
                        platformStrings.add(platform.name());
                    }
                    doc.setPlatform(platformStrings);
                }
                //获取请求参数文档
                ApiParam[] request = document.request();
                if (!ObjectUtils.isEmpty(request)) {
                    List<Param> requestParams = assembleParams(request);
                    doc.setRequest(requestParams);
                }
                //获取请求参数文档
                ApiParam[] response = document.response();
                if (!ObjectUtils.isEmpty(response)) {
                    List<Param> requestParams = assembleParams(response);
                    doc.setResponse(requestParams);
                }
                list.add(doc);
            }
        });
        try {
            log.info("start write doc......{}" ,path);
            String fileName = packagePath.split("\\.")[2];
            log.info("fileName: {}, doc size: {}", fileName, list.size());
            FileUtils.write(new File(path + File.separator + fileName + ".json"), JSON.toJSONString(list), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error(e.getMessage());
            log.error("write json file error:{}", packagePath);
        }
        log.info("doc:{}", JSON.toJSONString(list));
        log.info("generate doc success......");
    }

    private static List<Param> assembleParams(ApiParam[] response) {
        List<Param> params = new ArrayList<>();
        for (ApiParam apiParam : response) {
            String name = apiParam.name();
            String dataType = apiParam.dataType();
            String value = apiParam.value();
            boolean required = apiParam.required();
            String note = apiParam.note();
            params.add(new Param().setName(name).setDataType(dataType).setValue(value).setRequired(required).setNote(note));
        }
        return params;
    }
}
