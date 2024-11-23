package org.chzz.market.domain.auctionv2.service;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.auctionv2.dto.ImageUploadEvent;
import org.chzz.market.domain.auctionv2.dto.request.RegisterRequest;
import org.chzz.market.domain.auctionv2.entity.AuctionStatus;
import org.chzz.market.domain.auctionv2.entity.AuctionV2;
import org.chzz.market.domain.auctionv2.repository.AuctionV2Repository;
import org.chzz.market.domain.auctionv2.schedule.AuctionV2EndJob;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.error.UserErrorCode;
import org.chzz.market.domain.user.error.exception.UserException;
import org.chzz.market.domain.user.repository.UserRepository;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionRegistrationService implements RegistrationService {
    private final AuctionV2Repository auctionRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final Scheduler scheduler;

    @Override
    @Transactional
    public void register(final Long userId, RegisterRequest request, final List<MultipartFile> images) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        AuctionV2 auction = createAuction(request, user);

        auctionRepository.save(auction);

        eventPublisher.publishEvent(new ImageUploadEvent(auction, images));
        registerSchedule(auction);
    }


    private AuctionV2 createAuction(final RegisterRequest request, final User user) {
        return AuctionV2.builder()
                .name(request.productName())
                .minPrice(request.minPrice())
                .description(request.description())
                .category(request.category())
                .seller(user)
                .status(AuctionStatus.PROCEEDING)
                .endDateTime(LocalDateTime.now().plusDays(1))
                .build();
    }

    private void registerSchedule(AuctionV2 auction) {
        // Job과 Trigger를 스케줄러에 등록
        try {
            // JobDetail 생성
            JobDetail jobDetail = JobBuilder.newJob(AuctionV2EndJob.class)
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
