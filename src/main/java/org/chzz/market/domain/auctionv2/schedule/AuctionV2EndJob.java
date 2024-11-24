package org.chzz.market.domain.auctionv2.schedule;

import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auctionv2.service.AuctionEndService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

/**
 * 경매 스케줄링 종료 작업
 */
@Component
@RequiredArgsConstructor
public class AuctionV2EndJob implements Job {
    private final AuctionEndService auctionEndService;

    @Override
    public void execute(JobExecutionContext context) {
        Long auctionId = context.getJobDetail().getJobDataMap().getLong("auctionId");
        auctionEndService.endAuction(auctionId);
    }
}
