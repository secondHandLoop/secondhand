package com.secondhand.common.support;

import com.secondhand.common.input.RequestParameter;

import java.util.Map;

public interface IApiService<K extends Map<String, Object>> {
    K handle(RequestParameter requestParameter);
}
