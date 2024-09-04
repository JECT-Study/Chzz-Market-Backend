package org.chzz.market.common;

import org.mockito.Mockito;
import org.quartz.Scheduler;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class QuartzConfig {

    @Bean
    public Scheduler scheduler() {
        // Scheduler를 Mockito로 모킹하여 반환
        return Mockito.mock(Scheduler.class);
    }
}
