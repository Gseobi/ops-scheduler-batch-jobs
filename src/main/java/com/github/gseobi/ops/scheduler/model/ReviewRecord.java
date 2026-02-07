package com.github.gseobi.ops.scheduler.model;

import com.github.gseobi.ops.scheduler.model.ReviewTarget.Store;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class ReviewRecord {
    private Store store;
    private String appCode;
    private String externalId;
    private Integer rating;
    private String title;
    private String content;
    private String author;
    private Instant createdAt;
}
