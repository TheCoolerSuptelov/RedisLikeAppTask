package com.github.theCoolerSuptelov.redisLikeInMemory.redisLikeSpringApp.Services;

import com.github.theCoolerSuptelov.redisLikeInMemory.redisLikeSpringApp.CacheUnit.CacheUnit;
import com.github.theCoolerSuptelov.redisLikeInMemory.redisLikeSpringApp.CacheUnit.CachedValueDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import static java.lang.Boolean.TRUE;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class CacheImplService {
    public static final String OK = "OK";
    private final long DEFAULTTTLMILLIS = 60_000;
    @Value("${defaultSavePathCacheImpl}")
    private String savePath;
    private HashMap<String, CacheUnit> cache = new HashMap<>();

    public String getSavePath() {
        return System.getProperty("java.io.tmpdir") + "/RedisLikeApp_cache";
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public Integer deleteKeys(List<String> arrKeysToDelete) {
        Integer deletionCounter = 0;
        for (String keyToDelete : arrKeysToDelete) {
            CacheUnit deletedValue = cache.remove(keyToDelete);
            if (deletedValue != null) {
                deletionCounter++;
            }
        }
        return deletionCounter;
    }

    public String putCachingValue(CachedValueDTO cachedValueDTO,
                                  String key,
                                  Long exSeconds,
                                  Long pxMilliseconds,
                                  Long unixTimeExpireSeconds,
                                  Long unixTimeExpireMilliseconds,
                                  Boolean setKeyIfNotExist,
                                  Boolean setKeyIfExist,
                                  Boolean keepCurrentTtl) {

        CacheUnit cacheUnit = getCachingValue(key);

        if (Boolean.TRUE.equals(setKeyIfExist) && cacheUnit == null) {
            return null;
        }
        if (Boolean.TRUE.equals(setKeyIfNotExist) && cacheUnit != null) {
            return null;
        }
        if (Boolean.TRUE.equals(keepCurrentTtl) && cacheUnit != null) {
            cacheUnit.setValue(cachedValueDTO.getValue());
            return OK;
        }

        CacheUnit freshCacheUnit = new CacheUnit(defineExpireTimeMilliseconds(exSeconds, pxMilliseconds, unixTimeExpireSeconds, unixTimeExpireMilliseconds)
                , cachedValueDTO.getValue());

        cache.put(key, freshCacheUnit);
        return OK;
    }

    private Long defineExpireTimeMilliseconds(Long exSeconds, Long pxMilliseconds, Long unixTimeExpireSeconds, Long unixTimeExpireMilliseconds) {
        long currentTimeMillis = System.currentTimeMillis();
        if (exSeconds != null) {
            return currentTimeMillis + (exSeconds * 1_000);
        }

        if (pxMilliseconds != null) {
            return currentTimeMillis + (pxMilliseconds);
        }

        if (unixTimeExpireSeconds != null) {
            return unixTimeExpireSeconds * 1000;
        }
        if (unixTimeExpireMilliseconds != null) {
            return unixTimeExpireMilliseconds;
        }
        return currentTimeMillis + DEFAULTTTLMILLIS;
    }

    public List<String> getAllKeys() {
        return cache.keySet().stream().collect(Collectors.toList());
    }

    public List<String> getKeysByRegExpPattern(String regExpPattern) {
        Pattern pattern = Pattern.compile(regExpPattern);
        return cache.keySet().stream().filter(pattern.asPredicate()).collect(Collectors.toList());
    }

    public CacheUnit getCachingValue(String key) {
        CacheUnit cachedValue = cache.get(key);
        if (cachedValue == null) {
            return null;
        }

        if (System.currentTimeMillis() > cachedValue.getExpiresAtMilliseconds()) {
            cache.remove(cachedValue);
            return null;
        }
        return cachedValue;
    }

    @PostConstruct
    public void loadFromDisk() {
        File dumpedToDiskCache = new File(this.getSavePath());
        if (!dumpedToDiskCache.exists()) {
            return;
        }

        try {
            ObjectInputStream objectInputStream
                    = new ObjectInputStream(new FileInputStream(this.getSavePath()));
            cache = (HashMap<String, CacheUnit>) objectInputStream.readObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public void saveToDisk() {
        File dumpedToDiskCache = new File(this.getSavePath());
        if (!dumpedToDiskCache.exists()) {
            this.setSavePath(this.getSavePath());
            dumpedToDiskCache = new File(this.getSavePath());
            try {
                dumpedToDiskCache.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            ObjectOutputStream objectOutputStream
                    = new ObjectOutputStream(new FileOutputStream(this.getSavePath()));
            objectOutputStream.writeObject(cache);
            objectOutputStream.flush();
            objectOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
