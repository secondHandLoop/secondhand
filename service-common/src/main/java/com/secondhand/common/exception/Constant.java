package com.secondhand.common.exception;

@SuppressWarnings("unused")
public class Constant {

    public final static String BODY_KEY = "data";
    public final static String SALT_KEY = "salt";
    public static final String PAGE_KEY = "total";
    public final static String TIMEOUT_DEFAULT_MESSAGE = "timeout";
    public final static String FORBIDDEN_DEFAULT_MESSAGE = "token_forbidden";
    public final static String STATUS_KEY = "code";
    public final static String MESSAGE_KEY = "message";
    public final static String SUCCESS_DEFAULT_MESSAGE = "success";
    public final static String FAIL_DEFAULT_MESSAGE = "fail";
    public final static String LOG_YEAR_MONTH_FORMAT = "YYYY_MM";
    public final static String PMC_LOG_TABLE_PREFIX = "log_pmc_";
    public final static String UMC_LOG_TABLE_PREFIX = "log_umc_";
    public final static String PRODUCT_LOG_TABLE_PREFIX = "log_product_";
    public final static String LONG_MAX_VALUE = "9999999999";
    public static final int FAIL_CODE = 500;
    public static final int NO_DATA_CODE = 404;
    public static final Integer SUCCESS_CODE = 200;
    public static final int WECHAT_CALLBACK_ERROR = -301;
    public static final int UNSUPPORTED_PAY = -302;
    public static final int MESSAGE_PUSH_ERROR = -303;
    public static final int CODE_ERROR = -305;
    public static final int INPUT_ERROR = -306;
}
