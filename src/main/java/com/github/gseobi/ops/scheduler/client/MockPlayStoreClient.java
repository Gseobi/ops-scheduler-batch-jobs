package com.github.gseobi.ops.scheduler.client;

import com.github.gseobi.ops.scheduler.model.ReviewFetchResult;
import com.github.gseobi.ops.scheduler.model.ReviewTarget;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class MockPlayStoreClient implements ReviewStoreClient {

    @Override
    public boolean supports(ReviewTarget.Store store) {
        return store == ReviewTarget.Store.PLAY_STORE;
    }

    @Override
    public ReviewFetchResult fetch(String logId, ReviewTarget target) {
        log.info("[{}] (MOCK) PlayStore fetch. package={}", logId, target.getStoreAppId());

        return new ReviewFetchResult(List.of(
                new ReviewFetchResult.RawReview(
                        "GP-" + UUID.randomUUID(),
                        5,
                        "Great app",
                        "Works smoothly. (mock)",
                        "userA",
                        Instant.now().minusSeconds(3600)
                ),
                new ReviewFetchResult.RawReview(
                        "GP-" + UUID.randomUUID(),
                        3,
                        "Not bad",
                        "Could be improved. (mock)",
                        "userB",
                        Instant.now().minusSeconds(7200)
                )
        ));
    }
}
