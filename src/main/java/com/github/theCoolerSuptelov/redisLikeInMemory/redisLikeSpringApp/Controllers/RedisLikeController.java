package com.github.theCoolerSuptelov.redisLikeInMemory.redisLikeSpringApp.Controllers;

import com.github.theCoolerSuptelov.redisLikeInMemory.redisLikeSpringApp.CacheUnit.CacheUnit;
import com.github.theCoolerSuptelov.redisLikeInMemory.redisLikeSpringApp.CacheUnit.CachedValueDTO;
import com.github.theCoolerSuptelov.redisLikeInMemory.redisLikeSpringApp.Services.CacheImplService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;


@RestController
@RequestMapping("/api")
public class RedisLikeController {
    private final CacheImplService cacheImplService;
    private final String defaultAnswer = "OK";

    public RedisLikeController(@Autowired CacheImplService cacheImplService) {
        this.cacheImplService = cacheImplService;
    }

    @GetMapping
    public CachedValueDTO getValue(@RequestParam("key") String key) {
        CacheUnit currentCacheUnit = cacheImplService. getCachingValue(key);
        if (currentCacheUnit == null) {
            return null;
        }
        return new CachedValueDTO(currentCacheUnit);
    }

    @DeleteMapping
    @Secured("ROLE_ADMIN")
    public Integer deleteValue(@RequestParam("key") List<String> keyList){
        return cacheImplService.deleteKeys(keyList);
    }

    /** Returns all keys matching pattern
     * @param pattern - the given regexp pattern
     * */
    @GetMapping("/keys")
    public List<String> getKeys(@RequestParam(value = "pattern", required = false) String pattern) {
        if (pattern == null){
            return cacheImplService.getAllKeys();
        }
        return cacheImplService.getKeysByRegExpPattern(pattern);
    }
    /**
     * @param key - Set key to hold the string value. If key already holds a value, it is overwritten, regardless of its type.
     * @param exSeconds   - EX Set the specified expire time, in seconds.
     * @param pxMilliseconds PX milliseconds -- Set the specified expire time, in milliseconds.
     * @param unixTimeExpireSeconds EXAT timestamp-seconds -- Set the specified Unix time at which the key will expire, in seconds.
     * @param unixTimeExpireMilliseconds timestamp-milliseconds -- Set the specified Unix time at which the key will expire, in milliseconds.
     * @param setKeyIfNotExist -- Only set the key if it does not already exist.
     * @param setKeyIfExist -- Only set the key if it already exist.
     * @param keepCurrentTtl -- Retain the time to live associated with the key.
     * */
    @PostMapping
    @Secured("ROLE_ADMIN")
    public String putValue(@RequestBody @NotNull CachedValueDTO cachedValueDTO,
                         @RequestParam("key") String key,
                         @RequestParam(value = "EX",required = false) Long exSeconds,
                         @RequestParam(value ="PX",required = false) Long pxMilliseconds,
                         @RequestParam(value ="EXAT",required = false) Long unixTimeExpireSeconds,
                         @RequestParam(value ="PXAT",required = false) Long unixTimeExpireMilliseconds,
                         @RequestParam(value = "NX",required = false ,defaultValue = "false") Boolean setKeyIfNotExist,
                         @RequestParam(value = "XX",required = false, defaultValue = "false") Boolean setKeyIfExist,
                         @RequestParam(value = "KEEPTTL",required = false, defaultValue = "false") Boolean keepCurrentTtl
    ){
        var putAnswer = cacheImplService.putCachingValue(cachedValueDTO,
                key,
                exSeconds,
                pxMilliseconds,
                unixTimeExpireSeconds,
                unixTimeExpireMilliseconds,
                setKeyIfNotExist,
                setKeyIfExist,
                keepCurrentTtl
        );
        if (putAnswer == null){
            return null;
        }
        if (putAnswer.equals("")){
            return defaultAnswer;
        }
        return putAnswer;
    }
}
