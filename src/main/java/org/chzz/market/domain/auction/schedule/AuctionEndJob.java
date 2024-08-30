package org.chzz.market.domain.auction.schedule;

import org.chzz.market.domain.auction.service.AuctionService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuctionEndJob implements Job {
    @Autowired
    AuctionService auctionService;

    @Override
    public void execute(JobExecutionContext context) {
        Long auctionId = context.getJobDetail().getJobDataMap().getLong("auctionId");
        auctionService.completeAuction(auctionId);
    }
}
