package org.chzz.market.domain.auction.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.chzz.market.domain.auction.error.AuctionErrorCode.END_WITHIN_MINUTES_PARAM_ALLOWED_FOR_PROCEEDING_ONLY;

import org.chzz.market.common.AWSConfig;
import org.chzz.market.domain.auction.entity.AuctionStatus;
import org.chzz.market.domain.auction.entity.Category;
import org.chzz.market.domain.auction.error.AuctionException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

@SpringBootTest
@Import(AWSConfig.class)
class AuctionLookupServiceTest {
    @Autowired
    AuctionLookupService auctionLookupService;

    @Test
    void 남은시간_파라미터를_사전경매조회에_사용하면_예외가_발생한다() {
        // given when
        assertThatThrownBy(() -> auctionLookupService.getAuctionList(1L, Category.ELECTRONICS, AuctionStatus.PRE, 1,
                PageRequest.of(0, 10)))
                .isInstanceOf(AuctionException.class)
                .extracting("errorCode")
                .isEqualTo(END_WITHIN_MINUTES_PARAM_ALLOWED_FOR_PROCEEDING_ONLY);
    }

}
