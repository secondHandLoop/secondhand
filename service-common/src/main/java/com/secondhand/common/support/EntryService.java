package com.secondhand.common.support;

import com.alibaba.fastjson.JSON;
import com.secondhand.common.exception.ApiException;
import com.secondhand.common.exception.JSONException;
import com.secondhand.common.validate.exception.ValidationException;
import com.secondhand.common.param.Param;
import org.slf4j.Logger;               // ✅ add
import org.slf4j.LoggerFactory;        // ✅ add
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import java.lang.reflect.*;
import java.util.*;

@Service
@SuppressWarnings({"unchecked", "ConstantConditions"})
class EntryService extends FormatService {

    // ✅ add explicit logger
    private static final Logger log = LoggerFactory.getLogger(EntryService.class);

    private static final String LIST = "List";
    private static final String SET = "Set";
    private static final List<String> ORIGINAL_TYPE = Arrays.asList("Integer", "Double", "Float", "Int", "Long");
    private static final String method_name = "service";

    @SuppressWarnings("unchecked")
    Map<String, Object> handle(Object target, Map request) throws Exception {
        try {
            Method[] methods = target.getClass().getDeclaredMethods();
            Method serviceMethod = null;
            List<Object> values = new ArrayList<>();
            for (Method method : methods) {
                if (method_name.equals(method.getName())) {
                    serviceMethod = method;
                    Parameter[] parameters = method.getParameters();
                    for (Parameter parameter : parameters) {
                        if (parameter.isAnnotationPresent(Param.class)) {
                            Param param = parameter.getAnnotation(Param.class);
                            if (StringUtils.isEmpty(param.name())) {
                                addValueForParameter(this, request, values, parameter);
                            } else {
                                addValueForAnnotation(this, request, values, parameter, param);
                            }
                        } else {
                            addValueForParameter(this, request, values, parameter);
                        }
                    }
                    break;
                }
            }
            if (serviceMethod == null) {
                throw new NoSuchMethodException("未找到名为 " + method_name + " 的方法");
            }
            log.info("values:{}", JSON.toJSONString(values));
            return (Map<String, Object>) serviceMethod.invoke(target, values.toArray());
        } catch (SecurityException | InvocationTargetException | IllegalArgumentException e) {
            Throwable cause = e.getCause();
            if (cause instanceof ApiException) throw (ApiException) cause;
            if (cause instanceof ValidationException) throw (ValidationException) cause;
            if (cause instanceof JSONException) throw (JSONException) cause;
            throw e;
        }
    }

    private void addValueForAnnotation(FormatService formatService, Map param, List<Object> values, Parameter parameter, Param x)
            throws IllegalAccessException, InvocationTargetException {
        try {
            String simpleName = capitalize(parameter.getType().getSimpleName());
            Object o = param.get(x.name());
            if (ObjectUtils.isEmpty(o) && x.required()) {
                throw new ApiException(x.name() + " is required");
            }
            if (SET.equals(simpleName) || LIST.equals(simpleName)) {
                Method formatMethod = formatService.getClass().getMethod("format" + simpleName, Object.class, Class.class);
                ParameterizedType p = (ParameterizedType) parameter.getParameterizedType();
                Class c = (Class) p.getActualTypeArguments()[0];
                Object invoke = formatMethod.invoke(formatService, o, c);
                values.add(invoke);
            } else {
                Method formatMethod = formatService.getClass().getMethod("format" + simpleName, Object.class);
                Object invoke = formatMethod.invoke(formatService, o);
                values.add(ObjectUtils.isEmpty(invoke) ? formatMethod.invoke(formatService, x.value()) : invoke);
            }
        } catch (NoSuchMethodException e) {
            values.add(JSON.parseObject(formatString(param.get(x.name())), parameter.getType()));
        }
    }

    private void addValueForParameter(FormatService formatService, Map param, List<Object> values, Parameter parameter)
            throws IllegalAccessException, InvocationTargetException {
        try {
            String simpleName = capitalize(parameter.getType().getSimpleName());
            log.info("{}====={}", parameter.getName(), simpleName);
            if (SET.equals(simpleName) || LIST.equals(simpleName)) {
                Method formatMethod = formatService.getClass().getMethod("format" + simpleName, Object.class, Class.class);
                Object paramValue = param.get(parameter.getName());
                Type parameterizedType = parameter.getParameterizedType();
                if (parameterizedType instanceof ParameterizedType) {
                    ParameterizedType p = (ParameterizedType) parameterizedType;
                    Class c = (Class) p.getActualTypeArguments()[0];
                    Object invoke = formatMethod.invoke(formatService, paramValue, c);
                    log.info(" array value:{}", paramValue);
                    values.add(invoke);
                } else {
                    log.warn("parameter {} does not have a parameterized type.", parameter.getName());
                    values.add(null);
                }
            } else {
                Method formatMethod = formatService.getClass().getMethod("format" + simpleName, Object.class);
                Object paramValue = param.get(parameter.getName());
                log.info("single value:{}", paramValue);
                if (paramValue == null || "null".equals(String.valueOf(paramValue))) {
                    values.add(ORIGINAL_TYPE.contains(simpleName) ? 0 : null);
                } else {
                    Object invoke = formatMethod.invoke(formatService, paramValue);
                    values.add(invoke);
                }
            }
        } catch (NoSuchMethodException e) {
            values.add(JSON.parseObject(formatString(param.get(parameter.getName())), parameter.getType()));
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }
}
