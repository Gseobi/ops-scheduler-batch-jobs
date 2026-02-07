package com.github.gseobi.ops.scheduler.job;

import com.github.gseobi.ops.scheduler.config.OpsSchedulerProperties;
import com.github.gseobi.ops.scheduler.lock.InMemoryJobLock;
import com.github.gseobi.ops.scheduler.service.ReviewsInquiryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewFetchScheduler {

    private static final String LOCK_KEY = "review-fetch";

    private final OpsSchedulerProperties props;
    private final ReviewsInquiryService reviewsInquiryService;
    private final InMemoryJobLock jobLock;

    private boolean isA() { return "A".equalsIgnoreCase(props.getServerGroup()); }
    private boolean isB() { return "B".equalsIgnoreCase(props.getServerGroup()); }

    // Server Group A: 0/6/12/18
    @Scheduled(cron = "${ops.scheduler.cron-group-a}", zone = "${ops.scheduler.zone:Asia/Seoul}")
    public void runGroupA() {
        if (!props.isEnabled() || !isA()) return;
        runWithLock("SCH-A");
    }

    // Server Group B: 3/9/15/21
    @Scheduled(cron = "${ops.scheduler.cron-group-b}", zone = "${ops.scheduler.zone:Asia/Seoul}")
    public void runGroupB() {
        if (!props.isEnabled() || !isB()) return;
        runWithLock("SCH-B");
    }

    private void runWithLock(String logId) {
        if (!jobLock.tryLock(LOCK_KEY)) {
            log.info("[{}] Skip (lock already held): {}", logId, LOCK_KEY);
            return;
        }

        long started = System.currentTimeMillis();
        try {
            log.info("[{}] Review fetch start", logId);
            reviewsInquiryService.fetchAll(logId);
            log.info("[{}] Review fetch end ({}ms)", logId, System.currentTimeMillis() - started);
        } finally {
            jobLock.unlock(LOCK_KEY);
        }
    }
}
