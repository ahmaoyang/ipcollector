package com.yz.spider.collector.utils;

import com.alibaba.fastjson.JSONObject;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.net.URL;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *  
 *
 * @author maoyang
 * 2019/1/22
 * * 本地缓存工具，基于ehcache，磁盘存储  
 **/
public class LocalCacheUtil {
    private static Logger LOG = LoggerFactory.getLogger(LocalCacheUtil.class);

    // 缓存map
    private static Map<String, Object> cacheMap = new HashMap();
    // 缓存有效期map
    private static Map<String, Long> expireTimeMap = new HashMap();


    /**
     * 获取指定的value,如果key不存在或者已过期,则返回null
     * @param key
     * @return
     */
    public static Object get(String key) {
        if (!cacheMap.containsKey(key)) {
            return null;
        }
        if (expireTimeMap.containsKey(key)) {
            if (expireTimeMap.get(key) < System.currentTimeMillis()) { // 缓存失效,已过期
                return null;
            }
        }
        return cacheMap.get(key);
    }

    /**
     * @param key
     * @param <T>
     * @return
     */
    public static <T> T getT(String key) {
        Object obj = get(key);
        return obj == null ? null : (T) obj;
    }

    /**
     * 设置value(不过期)
     * @param key
     * @param value
     */
    public static void set(String key, Object value) {
        cacheMap.put(key, value);
    }

    /**
     * 设置value
     * @param key
     * @param value
     * @param millSeconds 过期时间(毫秒)
     */
    public static void set(final String key, Object value, int millSeconds) {
        final long expireTime = System.currentTimeMillis() + millSeconds;
        cacheMap.put(key, value);
        expireTimeMap.put(key, expireTime);
        if (cacheMap.size() > 2) { // 清除过期数据
            new Thread(new Runnable() {
                public void run() {
                    // 此处若使用foreach进行循环遍历,删除过期数据,会抛出java.util.ConcurrentModificationException异常
                    Iterator<Map.Entry<String, Object>> iterator = cacheMap.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, Object> entry = iterator.next();
                        if (expireTimeMap.containsKey(entry.getKey())) {
                            long expireTime = expireTimeMap.get(key);
                            if (System.currentTimeMillis() > expireTime) {
                                iterator.remove();
                                expireTimeMap.remove(entry.getKey());
                            }
                        }
                    }
                }
            }).start();
        }
    }

    /**
     * key是否存在
     * @param key
     * @return
     */
    public static boolean isExist(String key) {
        return cacheMap.containsKey(key);
    }


    public static void main(String[] args) {
        LocalCacheUtil.set("testKey_1", "testValue_1");
        LocalCacheUtil.set("testKey_2", "testValue_2", 1);
        LocalCacheUtil.set("testKey_3", "testValue_3");
        LocalCacheUtil.set("testKey_4", "testValue_4", 1);
        Object testKey_2 =LocalCacheUtil.get("testKey_2");
        System.out.println(testKey_2);
    }
}
