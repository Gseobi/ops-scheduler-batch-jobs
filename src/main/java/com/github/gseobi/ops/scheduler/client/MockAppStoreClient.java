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
public class MockAppStoreClient implements ReviewStoreClient {

    @Override
    public boolean supports(ReviewTarget.Store store) {
        return store == ReviewTarget.Store.APP_STORE;
    }

    @Override
    public ReviewFetchResult fetch(String logId, ReviewTarget target) {
        log.info("[{}] (MOCK) AppStore fetch. appId={}", logId, target.getStoreAppId());

        return new ReviewFetchResult(List.of(
                new ReviewFetchResult.RawReview(
                        "AS-" + UUID.randomUUID(),
                        4,
                        "Nice",
                        "Pretty useful. (mock)",
                        "userC",
                        Instant.now().minusSeconds(5400)
                )
        ));
    }
}
