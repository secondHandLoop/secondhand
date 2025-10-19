package com.secondhand.common.output;

import java.util.Map;

public interface IDataToMap<T> {
    void dataToMap(Map<Object, Object> map, T source);
}
