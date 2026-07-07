package com.secondhand.common.handler;

import com.secondhand.common.exception.ApiException;
import com.secondhand.common.exception.JSONException;
import com.secondhand.common.input.RequestParameter;
import com.secondhand.common.validate.exception.ValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

public class ServiceHandler {

    private static final Logger log = LoggerFactory.getLogger(ServiceHandler.class);

    public interface ServiceHandlerProcess {
        /** 解析请求参数 */
        LinkedHashMap<String, Object> parseParam(String data);

        /** 获取请求IP */
        void ip(RequestParameter requestParameter, LinkedHashMap<String, Object> request);

        /** 验证参数合法性 */
        void paramValidate(LinkedHashMap<String, Object> request);

        /** 验证用户登录合法性 */
        void userValidate(LinkedHashMap<String, Object> request, boolean inner);

        /** 读取接口缓存 / 执行业务 */
        Map<String, Object> cacheCheck(LinkedHashMap<String, Object> request) throws Exception;

        /** 记录接口请求日志 */
        void log(LinkedHashMap<String, Object> request, Map<String, Object> result);

        /** 处理异常信息 */
        Map<String, Object> error(String msg, int code);
    }

    public static Map<String, Object> execute(RequestParameter requestParameter, ServiceHandlerProcess handler) {
        Map<String, Object> result = null;
        LinkedHashMap<String, Object> request = handler.parseParam(requestParameter.getData());
        handler.ip(requestParameter, request);

        try {
            handler.paramValidate(request);
            handler.userValidate(request, requestParameter.isInner());
            // IMPORTANT: assign the business result
            result = handler.cacheCheck(request);
            return result;

        } catch (ApiException e) {
            log.warn("ApiException: {}", e.getMessage(), e);
            return handler.error(e.getMessage(), e.getCode());

        } catch (ValidationException e) {
            log.info("Validation exception: {}", e.getMessage(), e);
            return handler.error(e.getMessage(), e.getCode());

        } catch (JSONException e) {
            log.error("JSON exception: {}", e.getMessage(), e);
            return handler.error(e.getMessage(), e.getCode());

        } catch (Exception e) {
            log.error("Internal exception: {}", e.getMessage(), e);
            return handler.error(e.getMessage(), 500);

        } finally {
            // even if result is null we still call log (your impl can handle it)
            try {
                handler.log(request, result);
            } catch (Exception logErr) {
                // never break the flow due to logging failure
                log.warn("Failed to log service execution: {}", logErr.toString());
            }
        }
    }
}
