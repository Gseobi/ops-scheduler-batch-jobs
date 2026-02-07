package com.github.gseobi.ops.scheduler.service;

import com.github.gseobi.ops.scheduler.model.ReviewFetchResult;
import com.github.gseobi.ops.scheduler.model.ReviewRecord;
import com.github.gseobi.ops.scheduler.model.ReviewTarget;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ReviewsFormService {

    /**
     * 스토어별 원천 응답을 표준 ReviewRecord로 정규화
     * - 실무: 번역/클린징/중복 제거/카테고리 분류 등을 포함할 수 있음
     * - 포트폴리오: 표준화 파이프라인 구조를 보여주는 목적
     */
    public List<ReviewRecord> normalize(String logId, ReviewTarget target, ReviewFetchResult fetched) {
        List<ReviewRecord> out = new ArrayList<>();
        for (ReviewFetchResult.RawReview r : fetched.getReviews()) {
            out.add(ReviewRecord.builder()
                    .store(target.getStore())
                    .appCode(target.getAppCode())
                    .externalId(r.getExternalId())
                    .rating(r.getRating())
                    .title(r.getTitle())
                    .content(r.getContent())
                    .author(r.getAuthor())
                    .createdAt(r.getCreatedAt() != null ? r.getCreatedAt() : Instant.now())
                    .build());
        }
        log.info("[{}] normalized: target={} count={}", logId, target.getAppCode(), out.size());
        return out;
    }
}
