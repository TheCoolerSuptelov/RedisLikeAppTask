package com.github.theCoolerSuptelov.redisLikeInMemory.redisLikeSpringApp.CacheUnit;

import java.util.Collection;

public class CachedValueDTO {
    private Long expiresAtMilliseconds;
    private Collection<Object> value;

    public CachedValueDTO() {
    }

    public CachedValueDTO(CacheUnit cacheUnit) {
        this.expiresAtMilliseconds = cacheUnit.getExpiresAtMilliseconds();
        this.value = (Collection<Object>) cacheUnit.getValue();
    }

    public static CachedValueDTO build(CacheUnit cacheUnit) {
        return new CachedValueDTO(cacheUnit);
    }

    public Iterable<Object> getValue() {
        return value;
    }

    public void setValue(Collection<Object> value) {
        this.value = value;
    }

    public Long getExpiresAfterMilliSeconds() {
        return expiresAtMilliseconds;
    }

    public void setExpiresAfterSeconds(Long expiresAfterSeconds) {
        this.expiresAtMilliseconds = expiresAfterSeconds;
    }


}
