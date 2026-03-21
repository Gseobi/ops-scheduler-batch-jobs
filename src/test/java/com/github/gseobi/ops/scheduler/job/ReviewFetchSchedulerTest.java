package com.github.gseobi.ops.scheduler.job;

import com.github.gseobi.ops.scheduler.config.OpsSchedulerProperties;
import com.github.gseobi.ops.scheduler.lock.InMemoryJobLock;
import com.github.gseobi.ops.scheduler.service.ReviewsInquiryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class ReviewFetchSchedulerTest {

    @Test
    @DisplayName("서버 그룹 A이고 enabled=true면 Group A 스케줄이 실행된다")
    void runGroupAExecutesWhenEnabledAndGroupA() {
        OpsSchedulerProperties props = new OpsSchedulerProperties();
        props.setEnabled(true);
        props.setServerGroup("A");

        ReviewsInquiryService inquiryService = mock(ReviewsInquiryService.class);
        InMemoryJobLock lock = new InMemoryJobLock();

        ReviewFetchScheduler scheduler = new ReviewFetchScheduler(props, inquiryService, lock);

        scheduler.runGroupA();

        verify(inquiryService, times(1)).fetchAll("SCH-A");
    }

    @Test
    @DisplayName("락이 이미 잡혀 있으면 실행을 건너뛴다")
    void runWithLockSkipsWhenLockAlreadyHeld() {
        OpsSchedulerProperties props = new OpsSchedulerProperties();
        props.setEnabled(true);
        props.setServerGroup("A");

        ReviewsInquiryService inquiryService = mock(ReviewsInquiryService.class);
        InMemoryJobLock lock = new InMemoryJobLock();
        lock.tryLock("review-fetch");

        ReviewFetchScheduler scheduler = new ReviewFetchScheduler(props, inquiryService, lock);

        scheduler.runGroupA();

        verify(inquiryService, never()).fetchAll(anyString());
    }
}