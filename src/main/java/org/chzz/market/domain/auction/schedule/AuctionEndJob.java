package org.chzz.market.domain.auction.schedule;

import org.chzz.market.domain.auction.service.AuctionEndService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 경매 스케줄링 종료 작업
 */
@Component
public class AuctionEndJob implements Job {
    @Autowired
    private AuctionEndService auctionEndService;

    @Override
    public void execute(JobExecutionContext context) {
        Long auctionId = context.getJobDetail().getJobDataMap().getLong("auctionId");
        auctionEndService.endAuction(auctionId);
    }
}
