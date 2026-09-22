package com.pets.auth.adapter.out.mongo;

import com.pets.auth.domain.model.LoginLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LoginLogRepository extends MongoRepository<LoginLog, String> {
}
