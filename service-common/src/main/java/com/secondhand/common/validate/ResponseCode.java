package com.secondhand.common.validate;

/**
 * jerry on 2015/11/30.
 */
@SuppressWarnings("unused")
public class ResponseCode {

    public final static String BODY_KEY = "body";

    public final static String SALT_KEY = "salt";

    public final static String TIMEOUT_DEFAULT_MESSAGE = "timeout";

    public final static String FORBIDDEN_DEFAULT_MESSAGE = "token_forbidden";
    public final static String DEST_DEFAULT_MESSAGE = "DEST FAIL";

    public final static String STATUS_KEY = "status";

    public final static String MESSAGE_KEY = "message";

    public final static String SUCCESS_DEFAULT_MESSAGE = "success";

    public final static String FAIL_DEFAULT_MESSAGE = "fail";

    public final static Integer UPLOAD_SUCCESS_CODE = 201;

    public static final Integer VALIDATE_LENGTH_CODE = -101;

    public static final Integer VALIDATE_NOT_NULL_CODE = -102;

    public static final Integer VALIDATE_PATTERN_code = -103;

    public static final Integer VALIDATE_USER_ERROR_CODE = -105;

    public final static Integer SUCCESS_CODE = 200;

    public final static Integer NO_DATA_CODE = 404;

    public final static Integer FAIL_CODE = 500;

    public static final Integer TIMEOUT_CODE = -600;

    public static final Integer TOKEN_ERROR_CODE = -700;
    public static final Integer DEST_ERROR_CODE = -701;

    public static final Integer USER_EXIST = -104;

    public static final Integer MESSAGE_ERROR = -105;

    public static final int PASSWORD_ERROR = -106;

    public static final int COUPON_EXPIRED = -107;

    public static final int USER_EXIST_CODE = -108;

    public static final int HAS_COLLECTED = -109;

    public static final int CREDIT_LOW = -110;

    public static final int ILLEGAL_SERVICE = -111;

    public static final int UPLOAD_ERROR = -112;

    public static final int ORDER_NOT_FOUND = -113;
    public static final int INVALID_BIND_CODE = -114;
    public static final int ACCOUNT_INCOME_ERROR = -115;
}

