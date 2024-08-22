package org.chzz.market.common;

import org.chzz.market.util.AuctionTestFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestConfig {
    @Bean
    public AuctionTestFactory auctionTestFactory() {
        return new AuctionTestFactory();
    }
}