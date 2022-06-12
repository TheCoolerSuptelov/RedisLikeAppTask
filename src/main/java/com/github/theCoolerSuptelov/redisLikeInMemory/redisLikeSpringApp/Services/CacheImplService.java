package com.github.theCoolerSuptelov.redisLikeInMemory.redisLikeSpringApp.Services;

import com.github.theCoolerSuptelov.redisLikeInMemory.redisLikeSpringApp.CacheUnit.CacheUnit;
import com.github.theCoolerSuptelov.redisLikeInMemory.redisLikeSpringApp.CacheUnit.CachedValueDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

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

    public String getSavePath() {
      return System.getProperty("java.io.tmpdir") +"/RedisLikeApp_cacheHm";
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    @Value("${defaultSavePathCacheImpl}")
    private String savePath;
    private final long defaultTtlMillis = 60_000;

    private HashMap<String, CacheUnit> cacheHM = new HashMap<>();

    public Integer deleteKeys(List<String> arrKeysToDelete){
        Integer deletionCounter = 0;
        for (String keyToDelete:arrKeysToDelete){
            CacheUnit deletedValue = cacheHM.remove(keyToDelete);
            if (deletedValue != null){
                deletionCounter++;
            }
        }
        return deletionCounter;
    }
    @Secured("ROLE_ADMIN")
    public String putCachingValue(CachedValueDTO cachedValueDTO,
                                  String key,
                                  Long exSeconds,
                                  Long pxMilliseconds,
                                  Long unixTimeExpireSeconds,
                                  Long unixTimeExpireMilliseconds,
                                  Boolean setKeyIfNotExist,
                                  Boolean setKeyIfExist,
                                  Boolean keepCurrentTtl){

        CacheUnit cacheUnit = getCachingValue(key);

        if (setKeyIfExist && cacheUnit==null){
            return null;
        }
        if (setKeyIfNotExist && cacheUnit != null){
            return null;
        }
        if (keepCurrentTtl && cacheUnit != null){
            cacheUnit.setValue(cachedValueDTO.getValue());
            return OK;
        }

        CacheUnit freshCacheUnit = new CacheUnit(defineExpireTimeMilliseconds(exSeconds, pxMilliseconds,unixTimeExpireSeconds,unixTimeExpireMilliseconds)
                ,cachedValueDTO.getValue());

        cacheHM.put(key,freshCacheUnit);
        return OK;
    }
    private Long defineExpireTimeMilliseconds(Long exSeconds, Long pxMilliseconds, Long unixTimeExpireSeconds, Long unixTimeExpireMilliseconds){
        long currentTimeMillis = System.currentTimeMillis();
        if (exSeconds != null){
            return currentTimeMillis + (exSeconds * 1_000);
        }

        if (pxMilliseconds != null){
            return currentTimeMillis + (pxMilliseconds);
        }

        if (unixTimeExpireSeconds != null){
            return unixTimeExpireSeconds*1000;
        }
        if  (unixTimeExpireMilliseconds != null){
            return unixTimeExpireMilliseconds;
        }
        return currentTimeMillis + defaultTtlMillis;
    }
    public List<String> getAllKeys(){
        return cacheHM.keySet().stream().collect(Collectors.toList());
    }
    public List<String> getKeysByRegExpPattern(String regExpPattern){
        Pattern pattern = Pattern.compile(regExpPattern);
        return cacheHM.keySet().stream().filter(pattern.asPredicate()).collect(Collectors.toList());
    }
    public CacheUnit getCachingValue(String key){
        CacheUnit cachedValue = cacheHM.get(key);
        if (cachedValue==null){
            return null;
        }

        if (System.currentTimeMillis() > cachedValue.getExpiresAtMilliseconds()){
            cacheHM.remove(cachedValue);
            return null;
        }
        return cachedValue;
    }

    @PostConstruct
    public void loadFromDisk() {
        File dumpedToDiskCacheHm = new File(this.getSavePath());
        if (!dumpedToDiskCacheHm.exists()){
            return;
        }

        try {
            ObjectInputStream objectInputStream
                    = new ObjectInputStream(new FileInputStream(this.getSavePath()));
            cacheHM = (HashMap<String, CacheUnit>) objectInputStream.readObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public void saveToDisk(){
        File dumpedToDiskCacheHm = new File(this.getSavePath());
        if (!dumpedToDiskCacheHm.exists()){
            this.setSavePath(this.getSavePath());
            dumpedToDiskCacheHm = new File(this.getSavePath());
            try {
                dumpedToDiskCacheHm.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            ObjectOutputStream objectOutputStream
                    = new ObjectOutputStream(new FileOutputStream(this.getSavePath()));
            objectOutputStream.writeObject(cacheHM);
            objectOutputStream.flush();
            objectOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
