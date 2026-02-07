package com.github.gseobi.ops.scheduler.lock;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class InMemoryJobLock {

    private final ConcurrentHashMap<String, AtomicBoolean> locks = new ConcurrentHashMap<>();

    public boolean tryLock(String key) {
        AtomicBoolean flag = locks.computeIfAbsent(key, k -> new AtomicBoolean(false));
        return flag.compareAndSet(false, true);
    }

    public void unlock(String key) {
        AtomicBoolean flag = locks.get(key);
        if (flag != null) flag.set(false);
    }
}
