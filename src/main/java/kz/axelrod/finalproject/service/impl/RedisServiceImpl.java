package kz.axelrod.finalproject.service.impl;

import kz.axelrod.finalproject.model.dto.ResultDto;
import kz.axelrod.finalproject.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RedisServiceImpl implements RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveData(String key, ResultDto data) {
        log.info("REDIS --- save data '{}' with key '{}'", data, key);
        redisTemplate.opsForValue().set(key, data);
    }

    public ResultDto getData(String key) {
        log.info("REDIS --- retrieve data with key '{}'", key);
        return (ResultDto) redisTemplate.opsForValue().get(key);
    }

    public void deleteData(String key) {
        log.info("REDIS --- delete data with key '{}'", key);
        redisTemplate.delete(key);
    }

    public Boolean hasKeyOfData(String key) {
        log.info("REDIS --- has key of data '{}'", key);
        return redisTemplate.hasKey(key);
    }
}
