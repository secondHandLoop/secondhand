package com.secondhand.common.cashe;


public interface CacheProvider {

    boolean set(String key, int time, Object o);

    boolean delete(String key);

    <T> T get(String key);

    void convertAndSend(String channel, Object o);

}
