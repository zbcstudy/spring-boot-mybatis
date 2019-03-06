package com.wondertek.mybatis.lock.cache;

import com.wondertek.mybatis.lock.annotation.VersionLocker;
import com.wondertek.mybatis.lock.uitl.Constent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author zbc
 * @Date 20:44-2019/2/19
 */
public class LocalVersionLockerCache implements VersionLockerCache {

    private static final Logger log = LoggerFactory.getLogger(LocalVersionLockerCache.class);

    //map用于保存缓存
    private ConcurrentHashMap<String, ConcurrentHashMap<VersionLockerCache.MethodSignature, VersionLocker>> caches = new ConcurrentHashMap<>();


    @Override
    public boolean containMethodSignature(MethodSignature vm) {
        String nameSpace = getNameSpace(vm);
        ConcurrentHashMap<MethodSignature, VersionLocker> cache = caches.get(nameSpace);
        if (cache == null || cache.isEmpty()) {
            return false;
        }
        boolean containsMethodSignature = cache.containsKey(vm);
        if (containsMethodSignature && log.isDebugEnabled()) {
            log.debug("{}The method {} {} is hit in cache", Constent.LOG_PREFIX, nameSpace, vm.getId());
        }
        return containsMethodSignature;
    }

    @Override
    public void cacheMethod(MethodSignature vm, VersionLocker versionLocker) {
        String nameSpace = getNameSpace(vm);
        ConcurrentHashMap<MethodSignature, VersionLocker> cache = caches.get(nameSpace);
        if (cache == null || cache.isEmpty()) {
            cache = new ConcurrentHashMap<>();
            cache.put(vm, versionLocker);
            caches.put(nameSpace, cache);
            if (log.isDebugEnabled()) {
                log.debug(Constent.LOG_PREFIX + nameSpace + ": " + vm.getId() + " is cached.");
            }
        } else {
            cache.put(vm, versionLocker);
        }
    }

    @Override
    public VersionLocker getVersionLocker(MethodSignature vm) {
        String nameSpace = getNameSpace(vm);
        ConcurrentHashMap<MethodSignature, VersionLocker> cache = caches.get(nameSpace);
        if (cache == null || cache.isEmpty()) {
            return null;
        }
        return cache.get(vm);
    }

    private String getNameSpace(VersionLockerCache.MethodSignature vm) {
        String id = vm.getId();
        int index = id.lastIndexOf(".");
        return id.substring(0, index);
    }
}
