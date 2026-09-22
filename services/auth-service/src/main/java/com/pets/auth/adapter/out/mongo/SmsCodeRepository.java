package com.pets.auth.adapter.out.mongo;

import com.pets.auth.domain.model.SmsCode;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SmsCodeRepository extends MongoRepository<SmsCode, String> {

    List<SmsCode> findByPhoneAndBizTypeOrderByCreateTimeDesc(String phone, String bizType);
}
