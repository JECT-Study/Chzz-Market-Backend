package org.chzz.market.domain.auction.service;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.auction.dto.event.AuctionRegistrationEvent;
import org.chzz.market.domain.auction.schedule.AuctionEndJob;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionSchedulingService {
    private final Scheduler scheduler;

    @EventListener
    public void registerSchedule(AuctionRegistrationEvent event) {
        Long auctionId = event.auctionId();
        LocalDateTime endDateTime = event.endDateTime();
        // Job과 Trigger를 스케줄러에 등록
        try {
            // JobDetail 생성
            JobDetail jobDetail = JobBuilder.newJob(AuctionEndJob.class)
                    .withIdentity("auctionEndJob_" + auctionId, "auctionJobs")
                    .usingJobData("auctionId", String.valueOf(auctionId))  // auctionId를 문자열로 변환하여 저장
                    .build();

            // Trigger 생성
            SimpleTrigger trigger = (SimpleTrigger) TriggerBuilder.newTrigger()
                    .withIdentity("auctionEndTrigger_" + auctionId, "auctionTriggers")
                    .startAt(Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant()))
                    .build();
            scheduler.scheduleJob(jobDetail, trigger);
            log.info("Scheduled job with ID: {} and Trigger: {} at {}", jobDetail.getKey(), trigger.getKey(),
                    endDateTime);
        } catch (SchedulerException e) {
            log.error("SchedulerException occurred while scheduling job", e);
        }
    }
}
