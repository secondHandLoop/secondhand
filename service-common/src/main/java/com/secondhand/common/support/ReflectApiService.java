package com.secondhand.common.support;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.secondhand.common.cashe.CacheProvider;
import com.secondhand.common.cashe.ServiceCache;
import com.secondhand.common.exception.ApiException;
import com.secondhand.common.exception.ResponseCode;
import com.secondhand.common.handler.ServiceHandler;
import com.secondhand.common.input.RequestParameter;
import com.secondhand.common.output.ApiResponseHelper;
import com.secondhand.common.tool.DigestUtils;
import com.secondhand.common.tool.ThreadLocalUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;                // ✅ explicit logger
import org.slf4j.LoggerFactory;        // ✅ explicit logger
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Map;

@Component
public abstract class ReflectApiService extends ApiResponseHelper implements IApiService<Map<String, Object>> {

    private static final Logger log = LoggerFactory.getLogger(ReflectApiService.class);

    @Autowired(required = false)
    protected CacheProvider cacheProvider;
    @Resource
    private EntryService entryService;

    @Override
    public Map<String, Object> handle(RequestParameter requestParameter) {
        return ServiceHandler.execute(requestParameter, new ServiceHandler.ServiceHandlerProcess() {
            @Override
            @SuppressWarnings("unchecked")
            public LinkedHashMap<String, Object> parseParam(String data) {
                // if Lombok getters aren’t available, ensure RequestParameter has explicit getData()
                return JSON.parseObject(requestParameter.getData(), LinkedHashMap.class, Feature.OrderedField);
            }

            @Override
            public void ip(RequestParameter rp, LinkedHashMap<String, Object> request) {
                // add caller IP to request (safe even if null)
                request.put("ip", rp.getIp());
            }

            @Override
            public void paramValidate(LinkedHashMap<String, Object> request) {
                // hook for your custom validation if needed
                ThreadLocalUtils.add(request);
            }

            @Override
            public void userValidate(LinkedHashMap<String, Object> request, boolean inner) {
                // token verification removed in this minimal build
            }

            @Override
            public Map<String, Object> cacheCheck(LinkedHashMap<String, Object> request) throws Exception {
                // Optional caching via @ServiceCache and @Component bean name
                if (ReflectApiService.this.getClass().isAnnotationPresent(ServiceCache.class)
                        && ReflectApiService.this.getClass().isAnnotationPresent(Component.class)) {

                    if (cacheProvider == null) {
                        return entryService.handle(ReflectApiService.this, request);
                    }

                    Component component = ReflectApiService.this.getClass().getAnnotation(Component.class);
                    ServiceCache serviceCache = ReflectApiService.this.getClass().getAnnotation(ServiceCache.class);

                    StringBuilder key = new StringBuilder("protocol_").append(component.value()).append("_");
                    Arrays.stream(serviceCache.keys()).forEach(k -> key.append(String.valueOf(request.get(k))));
                    String md5Key = DigestUtils.md5(key.toString());

                    String cached = cacheProvider.get(md5Key);
                    log.info("cache key: {}", key);

                    if (StringUtils.isBlank(cached)) {
                        Map<String, Object> result = entryService.handle(ReflectApiService.this, request);
                        cacheProvider.set(md5Key, serviceCache.time(), JSON.toJSONString(result));
                        log.info("cache miss → stored");
                        return result;
                    } else {
                        log.info("cache hit");
                        return JSON.parseObject(cached, (Type) HashMap.class);
                    }
                }

                log.info("no cache → executing");
                return entryService.handle(ReflectApiService.this, request);
            }

            @Override
            public void log(LinkedHashMap<String, Object> request, Map<String, Object> result) {
                // remote logging removed; always clear thread local
                ThreadLocalUtils.removeAuth();
            }

            @Override
            public Map<String, Object> error(String msg, int code) {
                return fail(msg, code);
            }
        });
    }
}
