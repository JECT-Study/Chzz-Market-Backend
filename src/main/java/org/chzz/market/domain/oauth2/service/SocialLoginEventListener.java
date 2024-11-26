package org.chzz.market.domain.oauth2.service;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.user.dto.UserDeletedEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SocialLoginEventListener {
    private final SocialLoginServiceFactory socialLoginServiceFactory;

    @Async("threadPoolTaskExecutor")
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void handleUserDeletedEvent(UserDeletedEvent event) {
        SocialLoginService socialLoginService = socialLoginServiceFactory.getService(event.type());
        socialLoginService.disconnect(event.type(), event.providerId());
    }
}
