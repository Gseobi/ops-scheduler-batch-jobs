package com.github.gseobi.ops.scheduler.service;

import com.github.gseobi.ops.scheduler.client.ReviewStoreClient;
import com.github.gseobi.ops.scheduler.model.ReviewFetchResult;
import com.github.gseobi.ops.scheduler.model.ReviewRecord;
import com.github.gseobi.ops.scheduler.model.ReviewTarget;
import com.github.gseobi.ops.scheduler.repository.ReviewRepository;
import com.github.gseobi.ops.scheduler.support.RetryExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewsInquiryService {

    private final List<ReviewStoreClient> storeClients;
    private final ReviewsFormService formService;
    private final ReviewRepository repository;
    private final RetryExecutor retryExecutor;

    /**
     * 운영 서버에서 일정 주기로 스토어 리뷰를 조회/정규화/저장하는 파이프라인
     */
    public void fetchAll(String logId) {
        // 포트폴리오용: 대상은 하드코딩(실무에선 DB/설정으로 관리)
        List<ReviewTarget> targets = List.of(
                ReviewTarget.playStore("APP_ALPHA", "com.example.app.alpha"),
                ReviewTarget.appStore("APP_BETA", "1234567890")
        );

        int success = 0;
        int fail = 0;

        for (ReviewTarget target : targets) {
            try {
                ReviewStoreClient client = resolveClient(target);

                ReviewFetchResult fetched = retryExecutor.execute(
                        logId,
                        "StoreFetch(" + target.getStore() + "," + target.getAppCode() + ")",
                        3,          // maxAttempts
                        200,        // initialBackoffMs
                        2.0,        // multiplier
                        () -> client.fetch(logId, target)
                );

                List<ReviewRecord> normalized = formService.normalize(logId, target, fetched);
                repository.saveAll(logId, normalized);

                success++;
                log.info("[{}] target={} saved={}", logId, target.getAppCode(), normalized.size());
            } catch (Exception e) {
                fail++;
                log.warn("[{}] fetch fail. target={}", logId, target.getAppCode(), e);
            }
        }

        log.info("[{}] Summary: targets={} success={} fail={}", logId, targets.size(), success, fail);
    }

    private ReviewStoreClient resolveClient(ReviewTarget target) {
        return storeClients.stream()
                .filter(c -> c.supports(target.getStore()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No client for store=" + target.getStore()));
    }
}