package com.github.gseobi.ops.scheduler.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@AllArgsConstructor
public class ReviewFetchResult {

    private final List<RawReview> reviews;

    @Getter
    @AllArgsConstructor
    public static class RawReview {
        private final String externalId;
        private final Integer rating;
        private final String title;
        private final String content;
        private final String author;
        private final Instant createdAt;
    }
}
