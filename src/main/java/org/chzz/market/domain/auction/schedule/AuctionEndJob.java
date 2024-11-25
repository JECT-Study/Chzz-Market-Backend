package org.chzz.market.domain.auction.schedule;

import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auction.service.AuctionEndService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

/**
 * 경매 스케줄링 종료 작업
 */
@Component
@RequiredArgsConstructor
public class AuctionEndJob implements Job {
    private final AuctionEndService auctionEndService;

    @Override
    public void execute(JobExecutionContext context) {
        Long auctionId = context.getJobDetail().getJobDataMap().getLong("auctionId");
        auctionEndService.endAuction(auctionId);
    }
}
