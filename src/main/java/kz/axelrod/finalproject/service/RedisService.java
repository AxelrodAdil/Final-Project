package kz.axelrod.finalproject.service;

import kz.axelrod.finalproject.model.dto.ResultDto;

public interface RedisService {

    void saveData(String key, ResultDto data);

    ResultDto getData(String key);

    void deleteData(String key);

    Boolean hasKeyOfData(String key);
}
