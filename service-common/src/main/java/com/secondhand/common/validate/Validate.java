package com.secondhand.common.validate;

import com.secondhand.common.validate.exception.ValidationException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * jerry on 2015/11/30.
 */
public class Validate {


    private final String name;
    private final String message;
    private final ValidateType type;
    private String regex;

    private int min;

    private int max;

    private Validate(String name, String message, ValidateType validateType) {
        this.name = name;
        this.message = message;
        this.type = validateType;
    }

    private Validate(String name, String message, String regex, ValidateType type) {
        this.name = name;
        this.message = message;
        this.regex = regex;
        this.type = type;
    }

    private Validate(String name, String message, int min, int max, ValidateType type) {
        this.name = name;
        this.message = message;
        this.min = min;
        this.max = max;
        this.type = type;
    }

    private static void regex(Map<String, Object> request, String[] name, String[] regex, String[] message) {
        if (name.length == regex.length && regex.length == message.length) {
            for (int i = 0; i < name.length; i++) {
                new Validate(name[i], message[i], regex[i], ValidateType.PATTERN).validate(request);
            }
        }
    }

    private static void length(Map<String, Object> request, String[] name, int[] min, int[] max) {
        if (name.length == min.length && min.length == max.length) {
            for (int i = 0; i < name.length; i++) {
                new Validate(name[i], name[i] + "must be " + min[i] + " to " + max[i], min[i], max[i], ValidateType.LENGTH).validate(request);
            }
        }
    }

    private static void notNull(Map<String, Object> request, String[] name) {
        for (String n : name) {
            new Validate(n, n + " is necessary", ValidateType.NOT_NULL).validate(request);
        }
    }

    private static void actualNotNull(Map<String, Object> request, String[] name) {
        for (String n : name) {
            if (request.get(n) == null)
                throw new ValidationException(n + " is null", ResponseCode.VALIDATE_NOT_NULL_CODE);
        }
    }

    public static void goValidate(Class<?> clazz, Map<String, Object> body, CodeValidator codeValidator) {
        if (clazz.isAnnotationPresent(Pattern.class)) {
            Pattern pattern = clazz.getAnnotation(Pattern.class);
            Validate.regex(body, pattern.name(), pattern.regex(), pattern.message());
        }
        if (clazz.isAnnotationPresent(Length.class)) {
            Length length = clazz.getAnnotation(Length.class);
            Validate.length(body, length.name(), length.min(), length.max());
        }
        if (clazz.isAnnotationPresent(NotNull.class)) {
            NotNull notNull = clazz.getAnnotation(NotNull.class);
            Validate.notNull(body, notNull.name());
        }
        if (clazz.isAnnotationPresent(ActualNotNull.class)) {
            ActualNotNull actualNotNull = clazz.getAnnotation(ActualNotNull.class);
            Validate.actualNotNull(body, actualNotNull.name());
        }
        if (clazz.isAnnotationPresent(CodeValidate.class)) {
            CodeValidate codeValidate = clazz.getAnnotation(CodeValidate.class);
            String code = codeValidate.code();
            String codeValue = String.valueOf(body.get(code));
            String keyValue = String.valueOf(body.get("phone"));
            codeValidator.validate(codeValue, keyValue);
        }
    }

    public void validate(Map<String, Object> request) {
        switch (type) {
            case NOT_NULL:
                Object o = request.get(name);
                if (o == null) {
                    throw new ValidationException(message, ResponseCode.VALIDATE_NOT_NULL_CODE);
                }
                if ((o instanceof String) && StringUtils.isEmpty(o)) {
                    throw new ValidationException(message, ResponseCode.VALIDATE_NOT_NULL_CODE);
                }
                if ((o instanceof Integer) && Integer.parseInt(String.valueOf(o)) == 0) {
                    throw new ValidationException(message, ResponseCode.VALIDATE_NOT_NULL_CODE);
                }
                if ((o instanceof Collection) && CollectionUtils.isEmpty((Collection<?>) o)) {
                    throw new ValidationException(message, ResponseCode.VALIDATE_NOT_NULL_CODE);
                }
                if ((o instanceof Map) && ((Map) o).isEmpty()) {
                    throw new ValidationException(message, ResponseCode.VALIDATE_NOT_NULL_CODE);
                }
                break;
            case PATTERN:
                try {
                    Object param = request.get(name);
                    if (StringUtils.isEmpty(param)) {
                        java.util.regex.Pattern p = java.util.regex.Pattern.compile(regex);
                        Matcher m = p.matcher(String.valueOf(param));
                        if (!m.matches()) {
                            throw new ValidationException(message, ResponseCode.VALIDATE_PATTERN_code);
                        }
                    }
                } catch (Exception e) {
                    throw new ValidationException(message, ResponseCode.VALIDATE_NOT_NULL_CODE);
                }
                break;
            case LENGTH:
                if (StringUtils.isEmpty(request.get(name))) {
                    throw new ValidationException(message, ResponseCode.VALIDATE_NOT_NULL_CODE);
                }
                try {
                    int length = String.valueOf(request.get(name)).getBytes("gbk").length;
                    if (length < min || length > max) {
                        throw new ValidationException(message, ResponseCode.VALIDATE_LENGTH_CODE);
                    }
                } catch (UnsupportedEncodingException ignored) {
                }
        }
    }

    public enum ValidateType {
        LENGTH, NOT_NULL, PATTERN
    }
}

