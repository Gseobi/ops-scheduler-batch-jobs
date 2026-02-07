package com.github.gseobi.ops.scheduler.repository;

import com.github.gseobi.ops.scheduler.model.ReviewRecord;
import com.github.gseobi.ops.scheduler.model.ReviewTarget;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class ReviewRepository {

    private final List<ReviewRecord> storage = Collections.synchronizedList(new ArrayList<>());

    public void saveAll(String logId, List<ReviewRecord> records) {
        storage.addAll(records);
        log.info("[{}] repository saved. total={}", logId, storage.size());
    }

    public List<ReviewRecord> findLatest(int limit) {
        int safeLimit = Math.max(1, limit);
        int size = storage.size();
        int from = Math.max(0, size - safeLimit);
        return new ArrayList<>(storage.subList(from, size));
    }

    public Optional<ReviewRecord> findByStoreAndExternalId(ReviewTarget.Store store, String externalId) {
        return storage.stream()
                .filter(r -> r.getStore() == store && externalId.equals(r.getExternalId()))
                .findFirst();
    }
}
