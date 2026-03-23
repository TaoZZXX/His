package com.his.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 角色权限 URL 在 Redis 中的读写与版本号维护（网关与 System 共用）。
 */
@Component
public class PermissionUrlRedisCache {

    private static final Logger log = LoggerFactory.getLogger(PermissionUrlRedisCache.class);

    public static final String PERM_VERSION_KEY = "his:permission:cache:ver";
    public static final String PERM_ROLE_KEY_PREFIX = "his:permission:role:";
    public static final long PERM_URL_CACHE_TTL_MS = 10_000L;

    private final StringRedisTemplate stringRedisTemplate;

    public PermissionUrlRedisCache(ObjectProvider<StringRedisTemplate> stringRedisTemplateProvider) {
        this.stringRedisTemplate = stringRedisTemplateProvider.getIfAvailable();
    }

    public void bumpPermissionCacheVersion() {
        if (stringRedisTemplate == null) {
            return;
        }
        try {
            stringRedisTemplate.opsForValue().increment(PERM_VERSION_KEY);
        } catch (Exception e) {
            log.debug("redis increment permission version failed", e);
        }
    }

    public long getPermissionCacheVersion() {
        if (stringRedisTemplate == null) {
            return 0L;
        }
        try {
            String v = stringRedisTemplate.opsForValue().get(PERM_VERSION_KEY);
            if (v == null || v.trim().isEmpty()) {
                return 0L;
            }
            return Long.parseLong(v.trim());
        } catch (Exception e) {
            return 0L;
        }
    }

    public String getCachedPermissionUrlsJson(long roleId, long version) {
        if (stringRedisTemplate == null) {
            return null;
        }
        String cacheKey = roleCacheKey(roleId, version);
        try {
            return stringRedisTemplate.opsForValue().get(cacheKey);
        } catch (Exception e) {
            log.warn("redis get permissionUrls failed, roleId={}", roleId, e);
            return null;
        }
    }

    public void cachePermissionUrlsJson(long roleId, long version, String json) {
        if (stringRedisTemplate == null || json == null) {
            return;
        }
        String cacheKey = roleCacheKey(roleId, version);
        try {
            stringRedisTemplate.opsForValue().set(cacheKey, json, PERM_URL_CACHE_TTL_MS, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.warn("redis set permissionUrls failed, roleId={}", roleId, e);
        }
    }

    private static String roleCacheKey(long roleId, long version) {
        return PERM_ROLE_KEY_PREFIX + roleId + ":ver:" + version;
    }
}
