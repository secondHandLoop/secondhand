package com.secondhand.common.tool;

import com.secondhand.common.exception.ApiException;
import org.apache.commons.lang3.StringUtils;

public class AssertUtil {

    public static void isTrue(boolean value, String message) {
        if (!value) {
            throw new ApiException(message);
        }
    }

    public static void isFalse(boolean value, String message) {
        if (value) {
            throw new ApiException(message);
        }
    }

    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new ApiException(message);
        }
    }

    public static void notNullOrBlank(Object object, String message) {
        if (object == null) {
            throw new ApiException(message);
        }
        if (object instanceof String && StringUtils.isBlank((String) object)) {
            throw new ApiException(message);
        }
    }

    public static void equal(Object object1, Object object2, String message) {
        if (object1 == null || object2 == null) {
            throw new ApiException(message);
        }
        if (!object1.equals(object2)) {
            throw new ApiException(message);
        }
    }
}

