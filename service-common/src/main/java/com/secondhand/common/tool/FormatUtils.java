package com.secondhand.common.tool;


import com.alibaba.fastjson.JSON;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * jerry on 2016/3/16.
 */
public class FormatUtils {

    private static final FormatUtils ourInstance = new FormatUtils();

    private FormatUtils() {
    }

    public static FormatUtils getInstance() {
        return ourInstance;
    }

    public List<String> formatList(Object obj) {
        return JSON.parseArray(formatString(obj), String.class);
    }


    public List<Integer> formatList2Int(Object obj) {
        return JSON.parseArray(formatString(obj), Integer.class);
    }

    public Integer formatInteger(Object obj) {
        try {
            return Integer.valueOf(formatString(obj));
        } catch (Exception e) {
            return null;
        }
    }

    public Integer formatInteger(Object obj, Integer d) {
        try {
            String o = formatString(obj);
            if (StringUtils.isEmpty(o)) {
                return d;
            }
            return Integer.valueOf(o);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Long formatLong(Object obj) {
        try {
            return Long.valueOf(formatString(obj));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Double formatDouble(Object obj) {
        try {
            return Double.valueOf(formatString(obj));
        } catch (Exception ex) {
            return null;
        }
    }

    public Double formatDouble(Object obj, double f) {
        try {
            if (StringUtils.isEmpty(obj)) {
                return f;
            }
            return formatDouble(obj);
        } catch (Exception ex) {
            return null;
        }
    }

    public Long formatLong(Object obj, long f) {
        try {
            if (StringUtils.isEmpty(obj)) {
                return f;
            }
            return formatLong(obj);
        } catch (Exception ex) {
            return null;
        }
    }

    public Float formatFloat(Object obj) {
        try {
            return Float.valueOf(formatString(obj));
        } catch (Exception ex) {
            return null;
        }
    }

    public Float formatFloat(Object obj, float f) {
        try {
            if (StringUtils.isEmpty(obj)) {
                return f;
            }
            return formatFloat(obj);
        } catch (Exception ex) {
            return null;
        }
    }

    public String formatString(Object obj) {
        try {
            if (obj == null) {
                return null;
            }
            return String.valueOf(obj);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean formatBoolean(Object obj) {
        try {
            if (obj == null) {
                return false;
            }
            return Boolean.valueOf(String.valueOf(obj));
        } catch (Exception e) {
            return false;
        }
    }

    public String formatString(Object obj, String d) {
        try {
            if (obj == null || StringUtils.isEmpty(String.valueOf(obj))) {
                return d;
            }
            return String.valueOf(obj);
        } catch (Exception e) {
            return null;
        }
    }

    public BigDecimal formatBigcimal(Object obj) {
        try {
            return new BigDecimal(formatString(obj));
        } catch (Exception ex) {
            return null;
        }
    }


}

