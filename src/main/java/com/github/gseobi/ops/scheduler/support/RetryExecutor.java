package com.github.gseobi.ops.scheduler.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;

@Slf4j
@Component
public class RetryExecutor {

    public <T> T execute(String logId, String actionName, int maxAttempts, long initialBackoffMs, double multiplier, Callable<T> action) {
        long backoff = initialBackoffMs;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return action.call();
            } catch (Exception e) {
                boolean last = (attempt == maxAttempts);
                log.warn("[{}] {} fail (attempt {}/{}). {}", logId, actionName, attempt, maxAttempts, e.toString());

                if (last) {
                    throw new RuntimeException(actionName + " failed after retries", e);
                }

                try {
                    Thread.sleep(backoff);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", ie);
                }

                backoff = (long) Math.max(backoff, backoff * multiplier);
            }
        }

        throw new IllegalStateException("Unreachable");
    }
}
