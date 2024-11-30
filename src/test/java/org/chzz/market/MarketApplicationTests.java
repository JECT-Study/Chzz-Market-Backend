package org.chzz.market;

import org.chzz.market.common.AWSConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(AWSConfig.class)
class MarketApplicationTests {

    @Test
    void contextLoads() {
    }

}
