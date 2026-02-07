package com.github.gseobi.ops.scheduler.controller;

import com.github.gseobi.ops.scheduler.model.ReviewRecord;
import com.github.gseobi.ops.scheduler.model.ReviewTarget;
import com.github.gseobi.ops.scheduler.repository.ReviewRepository;
import com.github.gseobi.ops.scheduler.service.ReviewsInquiryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ops/reviews")
public class ReviewsController {

    private final ReviewsInquiryService reviewsInquiryService;
    private final ReviewRepository reviewRepository;

    /**
     * (운영 확인용) 스케줄러 작업을 수동으로 1회 실행
     */
    @PostMapping("/fetch")
    public ApiResponse fetchOnce() {
        String logId = "MANUAL";
        reviewsInquiryService.fetchAll(logId);
        return ApiResponse.ok("FETCH_TRIGGERED");
    }

    /**
     * (운영 확인용) 저장된 리뷰 조회 (최근 n개)
     */
    @GetMapping
    public List<ReviewRecord> list(@RequestParam(defaultValue = "50") int limit) {
        return reviewRepository.findLatest(limit);
    }

    /**
     * (운영 확인용) 리뷰 단건 조회
     * 실제 스토어 단건 API 호출은 계약/키 이슈로 제외
     */
    @GetMapping("/inquiry")
    public ReviewRecord inquiryOne(
            @RequestParam String reviewId,
            @RequestParam String platform
    ) {
        // android/ios 문자열을 Store 로 매핑
        ReviewTarget.Store store = switch (platform.toLowerCase()) {
            case "android" -> ReviewTarget.Store.PLAY_STORE;
            case "ios" -> ReviewTarget.Store.APP_STORE;
            default -> throw new IllegalArgumentException("Invalid platform. use android|ios");
        };

        return reviewRepository.findByStoreAndExternalId(store, reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found. reviewId=" + reviewId));
    }

    public record ApiResponse(boolean ok, String message) {
        public static ApiResponse ok(String message) {
            return new ApiResponse(true, message);
        }
    }
}
