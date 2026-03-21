package com.github.gseobi.ops.scheduler.controller;

import com.github.gseobi.ops.scheduler.model.ReviewRecord;
import com.github.gseobi.ops.scheduler.model.ReviewTarget;
import com.github.gseobi.ops.scheduler.repository.ReviewRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ReviewsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReviewRepository reviewRepository;

    @Test
    @DisplayName("수동 실행 API가 정상 응답한다")
    void fetchOnceReturnsOk() throws Exception {
        mockMvc.perform(post("/ops/reviews/fetch")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.message").value("FETCH_TRIGGERED"));
    }

    @Test
    @DisplayName("최근 리뷰 조회 API가 정상 응답한다")
    void listReturnsLatestReviews() throws Exception {
        String uniqueId = "GP-LIST-" + System.currentTimeMillis();

        reviewRepository.saveAll("TEST", List.of(
                ReviewRecord.builder()
                        .store(ReviewTarget.Store.PLAY_STORE)
                        .appCode("APP_ALPHA")
                        .externalId(uniqueId)
                        .rating(5)
                        .title("Great")
                        .content("Good app")
                        .author("userA")
                        .createdAt(Instant.now())
                        .build()
        ));

        mockMvc.perform(get("/ops/reviews").param("limit", "50"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(uniqueId)));
    }

    @Test
    @DisplayName("단건 리뷰 조회 API가 정상 응답한다")
    void inquiryOneReturnsReview() throws Exception {
        reviewRepository.saveAll("TEST", List.of(
                ReviewRecord.builder()
                        .store(ReviewTarget.Store.PLAY_STORE)
                        .appCode("APP_ALPHA")
                        .externalId("GP-777")
                        .rating(4)
                        .title("Nice")
                        .content("Useful")
                        .author("userB")
                        .createdAt(Instant.now())
                        .build()
        ));

        mockMvc.perform(get("/ops/reviews/inquiry")
                        .param("reviewId", "GP-777")
                        .param("platform", "android"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalId").value("GP-777"));
    }

    @Test
    @DisplayName("잘못된 platform 요청 시 예외가 발생한다")
    void inquiryOneWithInvalidPlatformFails() {
        assertThatThrownBy(() ->
                mockMvc.perform(get("/ops/reviews/inquiry")
                        .param("reviewId", "GP-777")
                        .param("platform", "windows"))
        )
                .isInstanceOf(Exception.class)
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }
}