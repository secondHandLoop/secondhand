package com.secondhand.common.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;


@SuppressWarnings("unchecked")
@Component
public class SpringApplicationContext implements ApplicationContextAware {

    private static ApplicationContext applicationContext = null;

    public SpringApplicationContext() {
    }

    public static <T> T getBean(String name) {
        Object object = null;
        if (applicationContext != null) {
            object = applicationContext.getBean(name);
        }
        return (T) object;
    }

    public static <T> T getBeanByClass(Class<T> clazz) {
        Object object = null;
        if (applicationContext != null) {
            object = applicationContext.getBean(clazz);
        }
        return (T) object;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringApplicationContext.applicationContext = applicationContext;
    }
}
