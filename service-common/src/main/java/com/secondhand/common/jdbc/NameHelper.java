package com.secondhand.common.jdbc;

import com.secondhand.common.entity.Base;

import jakarta.persistence.Entity;

public class NameHelper {

    public static String table(Class<? extends Base> entityClass) {
        Entity entity = entityClass.getAnnotation(Entity.class);
        return entity.name();
    }
}
