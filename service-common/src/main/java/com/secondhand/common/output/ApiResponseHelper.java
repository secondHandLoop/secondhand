package com.secondhand.common.output;

import com.secondhand.common.exception.Constant;
import com.secondhand.common.exception.JSONException;
import com.secondhand.common.exception.PassException;
import org.apache.commons.lang3.RandomStringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;

/**
 * jerry on 2015/12/1.
 */
@SuppressWarnings("all")
@Slf4j
public class ApiResponseHelper {

    public Map<String, Object> success(Map data) {
        Map<String, Object> result = new HashMap<>();
        result.put(Constant.STATUS_KEY, 200);
        result.put(Constant.MESSAGE_KEY, "success");
        result.put(Constant.BODY_KEY, data);
        return result;
    }


    private void wrapper(Map map, String key) {
        Object body = map.get(Constant.BODY_KEY);
        if (body != null) {
            String _salt = RandomStringUtils.randomNumeric(6);
            map.put(Constant.BODY_KEY, body);
            map.put(Constant.SALT_KEY, _salt);
        }
    }

    public List dataToMaps(Collection source, IDataToMap dataToMap) {
        if (source == null || source.isEmpty()) {
            return Collections.EMPTY_LIST;
        } else {
            List list = new ArrayList();
            for (Object obj : source) {
                Map map = new HashMap();
                try {
                    dataToMap.dataToMap(map, obj);
                    list.add(map);
                } catch (PassException ignored) {
                }
            }
            return list;
        }
    }

    private Map response(Integer status, String message, Collection sources, IDataToMap dataToMap) {
        return response(status, message, dataToMaps(sources, dataToMap));
    }


    private Map<String, Object> response(Integer status, String message, Object source, IDataToMap dataToMap) {
        if (source == null) {
            throw new JSONException("the result is empty", Constant.NO_DATA_CODE);
        }
        Map map = new HashMap();
        dataToMap.dataToMap(map, source);
        return response(status, message, map);
    }

    private Map response(Integer status, String message, Object source, IDataToMap dataToMap, Object page) {
        if (source == null) {
            throw new JSONException("the result is empty", Constant.NO_DATA_CODE);
        }
        Map map = new HashMap();
        dataToMap.dataToMap(map, source);
        return response(status, message, map, page);
    }

    private Map response(Integer status, String message) {
        return response(status, message, null);
    }

    private Map<String, Object> response(Integer status, String message, Object body) {
        Map<String, Object> map = new HashMap();
        map.put(Constant.STATUS_KEY, status);
        map.put(Constant.MESSAGE_KEY, message);
        // Always include body when present — empty collections must still be returned
        // so the frontend receives [] instead of a missing field (which causes crashes).
        if (body != null) {
            map.put(Constant.BODY_KEY, body);
        }
        return map;
    }

    private Map response(Integer status, String message, Object body, Object total) {
        Map map = new HashMap();
        map.put(Constant.STATUS_KEY, status);
        map.put(Constant.MESSAGE_KEY, message);
        map.put(Constant.BODY_KEY, body != null ? body : Collections.EMPTY_LIST);
        if (total != null) {
            map.put(Constant.PAGE_KEY, total);
        }
        return map;
    }

    public Map<String, Object> success() {
        return response(Constant.SUCCESS_CODE, Constant.SUCCESS_DEFAULT_MESSAGE);
    }

    public Map<String, Object> success(String message) {
        return response(Constant.SUCCESS_CODE, message);
    }

    public Map success(String message, Object source, IDataToMap dataToMap) {
        return response(Constant.SUCCESS_CODE, message, source, dataToMap);
    }

    public Map<String, Object> success(Object source, IDataToMap dataToMap) {
        return response(Constant.SUCCESS_CODE, Constant.SUCCESS_DEFAULT_MESSAGE, source, dataToMap);
    }

    public Map success(Object source, IDataToMap dataToMap, Object page) {
        return response(Constant.SUCCESS_CODE, Constant.SUCCESS_DEFAULT_MESSAGE, source, dataToMap, page);
    }

    public Map<String, Object> success(Object object) {
        return response(Constant.SUCCESS_CODE, Constant.SUCCESS_DEFAULT_MESSAGE, object);
    }

    public Map<String, Object> export(Object object) {
        return response(Constant.SUCCESS_CODE, Constant.SUCCESS_DEFAULT_MESSAGE, object);
    }

    public Map success(Object object, Object page) {
        return response(Constant.SUCCESS_CODE, Constant.SUCCESS_DEFAULT_MESSAGE, object, page);
    }

    public Map success(Object object, String message) {
        return response(Constant.SUCCESS_CODE, message, object);
    }


    public Map<String, Object> success(Collection collection) {
        return response(Constant.SUCCESS_CODE, Constant.SUCCESS_DEFAULT_MESSAGE, collection);
    }

    public Map<String, Object> success(Collection collection, Object page) {
        return response(Constant.SUCCESS_CODE, Constant.SUCCESS_DEFAULT_MESSAGE, collection, page);
    }

    public Map success(String message, Collection source, IDataToMap dataToMap) {
        return response(Constant.SUCCESS_CODE, message, source, dataToMap);
    }

    public Map<String, Object> success(Collection source, IDataToMap dataToMap) {
        return response(Constant.SUCCESS_CODE, Constant.SUCCESS_DEFAULT_MESSAGE, source, dataToMap);
    }

    public Map<String, Object> success(Collection source, IDataToMap dataToMap, Object total) {
        return response(Constant.SUCCESS_CODE, Constant.SUCCESS_DEFAULT_MESSAGE, dataToMaps(source, dataToMap), total);
    }

    public Map<String, Object> fail() {
        return response(Constant.FAIL_CODE, Constant.FAIL_DEFAULT_MESSAGE);
    }

    public Map invalidFail(int code, String message) {
        return response(code, message);
    }

    public Map<String, Object> fail(String message) {
        return response(Constant.FAIL_CODE, message);
    }

    public Map<String, Object> fail(String message, int code) {
        return response(code, message);
    }

    public Map<String, Object> fail(Object data, String me) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put(Constant.MESSAGE_KEY, me);
        if (data != null) {
            resultMap.put(Constant.BODY_KEY, data);
        }
        resultMap.put(Constant.STATUS_KEY, 500);
        return resultMap;
    }

    public Map<String, Object> empty() {
        return response(Constant.NO_DATA_CODE, "is empty");
    }

    public Map empty(String message) {
        return response(Constant.NO_DATA_CODE, message);
    }

    public Map empty(Object object) {
        return response(Constant.NO_DATA_CODE, "is empty", object);
    }
}


