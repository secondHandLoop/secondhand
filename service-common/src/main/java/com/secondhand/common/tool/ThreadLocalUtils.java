package com.secondhand.common.tool;


import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * ThreadLocal工具类
 *
 * @author xiaohua
 * @version 1.0
 */
@Slf4j
public class ThreadLocalUtils {
    private static final ThreadLocal<Map<String, Object>> auth = new ThreadLocal<>();


    public static void add(Map<String, Object> param) {
        if (param != null)
            auth.set(param);
    }

    public static Map<String, Object> get() {
        return auth.get();
    }

    public static void removeAuth() {
        auth.remove();
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        return (T) auth.get().get(key);
    }
}
