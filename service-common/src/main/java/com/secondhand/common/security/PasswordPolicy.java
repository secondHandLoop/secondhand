package com.secondhand.common.security;

import com.secondhand.common.exception.ApiException;

/**
 * 统一的账号密码强度策略，注册与重置密码共用，避免出现多处不一致的弱口令规则。
 *
 * <p>规则：长度至少 {@link #MIN_LENGTH} 位，且同时包含大写字母、小写字母和数字。</p>
 */
public final class PasswordPolicy {

    /** 密码最小长度 */
    public static final int MIN_LENGTH = 12;

    /** 校验失败时统一返回的提示信息（注册 / 重置共用） */
    public static final String MESSAGE = "密码必须至少12位，且包含大写字母、小写字母和数字";

    private PasswordPolicy() {
    }

    /**
     * 判断密码是否满足强度要求。
     *
     * @param password 待校验的明文密码，可为 null
     * @return 满足规则返回 true，否则返回 false
     */
    public static boolean isValid(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            return false;
        }
        boolean hasLower = false;
        boolean hasUpper = false;
        boolean hasDigit = false;
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (Character.isLowerCase(c)) {
                hasLower = true;
            } else if (Character.isUpperCase(c)) {
                hasUpper = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        return hasLower && hasUpper && hasDigit;
    }

    /**
     * 校验密码强度，不满足时抛出 {@link ApiException}（HTTP 400），
     * 交由全局异常处理器统一返回 {@link #MESSAGE}。
     *
     * @param password 待校验的明文密码
     */
    public static void validate(String password) {
        if (!isValid(password)) {
            throw new ApiException(MESSAGE, 400);
        }
    }
}
