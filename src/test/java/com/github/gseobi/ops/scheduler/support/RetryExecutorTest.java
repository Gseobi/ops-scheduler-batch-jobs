package com.github.gseobi.ops.scheduler.support;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RetryExecutorTest {

    private final RetryExecutor retryExecutor = new RetryExecutor();

    @Test
    @DisplayName("실패 후 재시도 끝에 성공하면 결과를 반환한다")
    void executeReturnsResultAfterRetries() {
        AtomicInteger attempts = new AtomicInteger(0);

        String result = retryExecutor.execute(
                "TEST",
                "retry-success",
                3,
                1,
                2.0,
                () -> {
                    if (attempts.incrementAndGet() < 3) {
                        throw new RuntimeException("temporary fail");
                    }
                    return "OK";
                }
        );

        assertThat(result).isEqualTo("OK");
        assertThat(attempts.get()).isEqualTo(3);
    }

    @Test
    @DisplayName("최대 재시도 이후에도 실패하면 예외를 던진다")
    void executeThrowsAfterMaxRetries() {
        AtomicInteger attempts = new AtomicInteger(0);

        assertThatThrownBy(() -> retryExecutor.execute(
                "TEST",
                "retry-fail",
                3,
                1,
                2.0,
                () -> {
                    attempts.incrementAndGet();
                    throw new RuntimeException("always fail");
                }
        )).isInstanceOf(RuntimeException.class);

        assertThat(attempts.get()).isEqualTo(3);
    }
}