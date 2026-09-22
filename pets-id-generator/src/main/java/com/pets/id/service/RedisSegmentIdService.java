package com.pets.id.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pets.id.entity.IdSegment;
import com.pets.id.properties.IdGeneratorProperties;
import com.pets.id.repo.SegmentRepository;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class RedisSegmentIdService implements IdService {

    private static final String KEY_PREFIX = "id:seg:";

    // 使用 RedisTemplate<Object, Object> 匹配 Spring Boot DataRedisAutoConfiguration 创建的默认 bean
    // Spring 7 泛型匹配更严格, RedisTemplate<String, Object> 无法匹配 RedisTemplate<Object, Object>
    private final RedisTemplate<Object, Object> redisTemplate;
    private final SegmentRepository segmentRepository;
    private final IdGeneratorProperties properties;

    private final ConcurrentHashMap<String, Segment> segments = new ConcurrentHashMap<>();

    public RedisSegmentIdService(RedisTemplate<Object, Object> redisTemplate,
                                 SegmentRepository segmentRepository,
                                 IdGeneratorProperties properties) {
        this.redisTemplate = redisTemplate;
        this.segmentRepository = segmentRepository;
        this.properties = properties;
    }

    @Override
    public long next(String bizType) {
        Segment segment = segments.computeIfAbsent(bizType, k -> new Segment());
        synchronized (segment) {
            long id = segment.current.incrementAndGet();
            long max = segment.max.get();
            if (id > max) {
                loadSegment(segment, bizType);
                id = segment.current.incrementAndGet();
            } else if (max - id <= prefetchBoundary()) {
                if (segment.loading.compareAndSet(false, true)) {
                    try {
                        prefetchSegment(segment, bizType);
                    } finally {
                        segment.loading.set(false);
                    }
                }
            }
            return id;
        }
    }

    @Override
    public List<Long> nextBatch(String bizType, int n) {
        List<Long> ids = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            ids.add(next(bizType));
        }
        return ids;
    }

    private long prefetchBoundary() {
        return (long) (properties.getStep() * properties.getPrefetchThreshold());
    }

    private void loadSegment(Segment segment, String bizType) {
        long newMax = allocate(bizType);
        segment.current.set(newMax - properties.getStep());
        segment.max.set(newMax);
    }

    private void prefetchSegment(Segment segment, String bizType) {
        String key = KEY_PREFIX + bizType;
        try {
            Long newMax = redisTemplate.opsForValue().increment(key, properties.getStep());
            if (newMax != null) {
                segment.max.set(newMax);
            }
        } catch (Exception ignored) {
            // Redis unavailable; skip prefetch, exhaustion path will allocate
        }
    }

    private long allocate(String bizType) {
        String key = KEY_PREFIX + bizType;
        try {
            Long val = redisTemplate.opsForValue().increment(key, properties.getStep());
            if (val == null) {
                return allocateFromDb(bizType);
            }
            return val;
        } catch (Exception e) {
            return allocateFromDb(bizType);
        }
    }

    private long allocateFromDb(String bizType) {
        int step = properties.getStep();
        for (int retry = 0; retry < 5; retry++) {
            IdSegment seg = segmentRepository.selectOne(
                    new QueryWrapper<IdSegment>().eq("biz_type", bizType));
            if (seg == null) {
                throw new IllegalStateException("id_segment row not found for bizType: " + bizType);
            }
            int affected = segmentRepository.updateMaxId(bizType, step, seg.getVersion());
            if (affected > 0) {
                return seg.getMaxId() + step;
            }
        }
        throw new IllegalStateException("Failed to allocate id from DB for bizType: " + bizType);
    }

    private static class Segment {
        private final AtomicLong current = new AtomicLong(0);
        private final AtomicLong max = new AtomicLong(0);
        private final AtomicBoolean loading = new AtomicBoolean(false);
    }
}
