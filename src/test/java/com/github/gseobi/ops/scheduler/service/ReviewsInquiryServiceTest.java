package com.github.gseobi.ops.scheduler.service;

import com.github.gseobi.ops.scheduler.client.MockPlayStoreClient;
import com.github.gseobi.ops.scheduler.client.ReviewStoreClient;
import com.github.gseobi.ops.scheduler.model.ReviewRecord;
import com.github.gseobi.ops.scheduler.model.ReviewTarget;
import com.github.gseobi.ops.scheduler.repository.ReviewRepository;
import com.github.gseobi.ops.scheduler.support.RetryExecutor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewsInquiryServiceTest {

    @Test
    @DisplayName("지원하지 않는 store에 대한 client가 없으면 해당 대상은 저장되지 않는다")
    void fetchAllSkipsUnsupportedStoreWhenNoClientExists() {
        ReviewRepository repository = new ReviewRepository();
        ReviewsFormService formService = new ReviewsFormService();
        RetryExecutor retryExecutor = new RetryExecutor();

        ReviewStoreClient playOnlyClient = new MockPlayStoreClient();

        ReviewsInquiryService service = new ReviewsInquiryService(
                List.of(playOnlyClient),
                formService,
                repository,
                retryExecutor
        );

        service.fetchAll("TEST");

        List<ReviewRecord> saved = repository.findLatest(10);

        assertThat(saved).isNotEmpty();
        assertThat(saved)
                .allMatch(record -> record.getStore() == ReviewTarget.Store.PLAY_STORE);
    }
}