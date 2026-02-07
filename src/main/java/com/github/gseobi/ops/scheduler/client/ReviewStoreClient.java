package com.github.gseobi.ops.scheduler.client;

import com.github.gseobi.ops.scheduler.model.ReviewFetchResult;
import com.github.gseobi.ops.scheduler.model.ReviewTarget;

public interface ReviewStoreClient {

    boolean supports(ReviewTarget.Store store);

    ReviewFetchResult fetch(String logId, ReviewTarget target);
}
