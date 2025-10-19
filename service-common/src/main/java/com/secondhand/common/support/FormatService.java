package com.secondhand.common.support;

import com.alibaba.fastjson.JSON;
import com.secondhand.common.tool.FormatUtils;

import java.math.BigDecimal;
import java.util.*;

@SuppressWarnings("all")
public class FormatService {

    public List<String> formatList(Object obj) {
        return JSON.parseArray(formatString(obj), String.class);
    }

    public <T> List<T> formatList(Object obj, Class<T> a) {
        return JSON.parseArray(formatString(obj), a);
    }

    public List<Map> formatListMap(Object obj) {
        return JSON.parseArray(formatString(obj), Map.class);
    }

    public List<Integer> formatIntegerList(Object obj) {
        return JSON.parseArray(formatString(obj), Integer.class);
    }

    public <T> Set<T> formatSet(Object obj, Class<T> c) {
        try {
            List<T> ts = formatList(obj, c);
            return new HashSet<T>(ts);
        } catch (Exception e) {
            return null;
        }
    }

    public Set<String> formatSet(Object obj) {
        try {
            List<String> list = formatList(obj);
            return new HashSet<>(list);
        } catch (Exception e) {
            return Collections.emptySet();
        }
    }

    public <T extends Enum<T>> T formatEnum(Object obj, Class<T> clz) {
        if (Objects.isNull(obj)) {
            return null;
        }
        return Enum.valueOf(clz, formatString(obj));
    }

    public Set<Integer> formatIntegerSet(Object obj) {
        try {
            List<Integer> list = formatIntegerList(obj);
            return new HashSet<>(list);
        } catch (Exception e) {
            return null;
        }
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

    public Integer formatInt(Object obj) {
        return formatInteger(obj);
    }

    public BigDecimal formatBigDecimal(Object obj) {
        return BigDecimal.valueOf(formatDouble(obj));
    }

    public Integer formatInteger(Object obj, Integer d) {
        return FormatUtils.getInstance().formatInteger(obj, d);
    }

    public Long formatLong(Object obj) {
        return FormatUtils.getInstance().formatLong(obj);
    }

    public Long formatLong(Object obj, long l) {
        return FormatUtils.getInstance().formatLong(obj, l);
    }

    public Double formatDouble(Object obj) {
        return FormatUtils.getInstance().formatDouble(obj);
    }

    public Double formatDouble(Object obj, double d) {
        return FormatUtils.getInstance().formatDouble(obj, d);
    }

    public Float formatFloat(Object obj) {
        return FormatUtils.getInstance().formatFloat(obj);
    }

    public String formatString(Object obj) {
        return FormatUtils.getInstance().formatString(obj);
    }

    public Boolean formatBoolean(Object obj) {
        return FormatUtils.getInstance().formatBoolean(obj);
    }

    public String formatString(Object obj, String d) {
        return FormatUtils.getInstance().formatString(obj, d);
    }

    public Float formatFloat(Object obj, float f) {
        return FormatUtils.getInstance().formatFloat(obj, f);
    }

}
