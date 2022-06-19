package com.github.theCoolerSuptelov.redisLikeInMemory.redisLikeSpringApp.CacheUnit;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * Класс обертка для кэшируемого значения.
 * expiresAtMilliseconds - дата, после которой значение не действительно
 * value - любое значение, проверка на указанные типы в задании не выполняется.
 * */
public class CacheUnit implements Serializable {
    private Long expiresAtMilliseconds;
    private Object value;


    public CacheUnit(Long expiresAtMilliseconds, Object value) {
        this.expiresAtMilliseconds = expiresAtMilliseconds;
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CacheUnit cacheUnit = (CacheUnit) o;

        if (!expiresAtMilliseconds.equals(cacheUnit.expiresAtMilliseconds)) return false;
        return value.equals(cacheUnit.value);
    }

    @Override
    public int hashCode() {
        int result = expiresAtMilliseconds.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }

    public Long getExpiresAtMilliseconds() {
        return expiresAtMilliseconds;
    }

    public void setExpiresAtMilliseconds(Long expiresAtMilliseconds) {
        this.expiresAtMilliseconds = expiresAtMilliseconds;
    }
}
