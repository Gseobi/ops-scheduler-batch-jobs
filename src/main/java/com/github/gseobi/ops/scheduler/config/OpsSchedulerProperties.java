package com.github.gseobi.ops.scheduler.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ops.scheduler")
public class OpsSchedulerProperties {

    /**
     * 운영 서버 이중화 환경에서 Schedule 작업 중복 실행 방지를 위해 Group Divide 실행
     * Server A: 0/6/12/18
     * Server B: 3/9/15/21
     */
    private String serverGroup = "A";

    // Cron Isolation (changeable in application.yml if desired)
    private String cronGroupA = "0 0 0,6,12,18 * * *";
    private String cronGroupB = "0 0 3,9,15,21 * * *";

    // Work on/off
    private boolean enabled = true;

    // Server Timezone Fixed
    private String zone = "Asia/Seoul";
}
