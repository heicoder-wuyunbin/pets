package com.pets.id.service;

import java.util.List;

public interface IdService {

    long next(String bizType);

    List<Long> nextBatch(String bizType, int n);
}
