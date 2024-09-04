package org.chzz.market.domain.auction.entity.listener;

import jakarta.persistence.PostPersist;
import java.sql.Date;
import java.time.ZoneId;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.schedule.AuctionEndJob;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuctionEntityListener {
    @Autowired
    private Scheduler scheduler;

    @PostPersist
    public void postPersist(Auction auction) {
        // Job과 Trigger를 스케줄러에 등록
        try {
            // JobDetail 생성
            JobDetail jobDetail = JobBuilder.newJob(AuctionEndJob.class)
                    .withIdentity("auctionEndJob_" + auction.getId(), "auctionJobs")
                    .usingJobData("auctionId", String.valueOf(auction.getId()))  // auctionId를 문자열로 변환하여 저장
                    .build();

            // Trigger 생성
            SimpleTrigger trigger = (SimpleTrigger) TriggerBuilder.newTrigger()
                    .withIdentity("auctionEndTrigger_" + auction.getId(), "auctionTriggers")
                    .startAt(Date.from(auction.getEndDateTime().atZone(ZoneId.systemDefault()).toInstant()))
                    .build();
            scheduler.scheduleJob(jobDetail, trigger);
            log.info("Scheduled job with ID: {} and Trigger: {} at {}", jobDetail.getKey(), trigger.getKey(),
                    auction.getEndDateTime());
        } catch (SchedulerException e) {
            log.error("SchedulerException occurred while scheduling job", e);
        }
    }
}
