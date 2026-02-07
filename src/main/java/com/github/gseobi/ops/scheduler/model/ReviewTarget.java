package com.github.gseobi.ops.scheduler.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewTarget {

    public enum Store { PLAY_STORE, APP_STORE }

    private final Store store;
    private final String appCode;
    private final String storeAppId;

    public static ReviewTarget playStore(String appCode, String packageName) {
        return new ReviewTarget(Store.PLAY_STORE, appCode, packageName);
    }

    public static ReviewTarget appStore(String appCode, String appId) {
        return new ReviewTarget(Store.APP_STORE, appCode, appId);
    }
}
