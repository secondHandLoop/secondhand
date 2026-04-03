package com.secondhand.common.jdbc;

import com.secondhand.common.entity.Base;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.apache.commons.lang3.StringUtils;

public class NameHelper {

    public static String table(Class<? extends Base> entityClass) {
        Entity entity = entityClass.getAnnotation(Entity.class);
        if (entity != null && StringUtils.isNotBlank(entity.name())) {
            return entity.name();
        }

        Table table = entityClass.getAnnotation(Table.class);
        if (table != null && StringUtils.isNotBlank(table.name())) {
            return table.name();
        }

        String simpleName = entityClass.getSimpleName();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < simpleName.length(); i++) {
            char ch = simpleName.charAt(i);
            if (Character.isUpperCase(ch) && i > 0) {
                builder.append('_');
            }
            builder.append(Character.toLowerCase(ch));
        }
        return builder.toString();
    }
}
