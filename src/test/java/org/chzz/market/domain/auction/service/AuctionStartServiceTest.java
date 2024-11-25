package org.chzz.market.domain.auction.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.chzz.market.domain.like.entity.Like;
import org.chzz.market.domain.like.repository.LikeRepository;
import org.chzz.market.domain.notification.event.NotificationEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class AuctionStartServiceTest {
    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AuctionStartService auctionStartService;

    @Test
    public void 사전경매에서_정식경매로_전환_성공() {
        // given
        Auction auction = mock(Auction.class);
        Like like = mock(Like.class);

        when(auctionRepository.findById(any())).thenReturn(Optional.of(auction));
        doNothing().when(auction).validateOwner(any());
        doNothing().when(auction).startOfficialAuction();
        when(likeRepository.findByAuctionId(auction.getId())).thenReturn(List.of(like));

        // when
        auctionStartService.start(1L, 1L);

        // then
        verify(eventPublisher, times(1)).publishEvent(any(NotificationEvent.class));
    }

    @Test
    public void 사전경매에서_정식경매로_전환할때_좋아요가_없을시_알림이벤트발행을_하지않는다() {
        // given
        Auction auction = mock(Auction.class);

        when(auctionRepository.findById(any())).thenReturn(Optional.of(auction));
        doNothing().when(auction).validateOwner(any());
        doNothing().when(auction).startOfficialAuction();
        when(likeRepository.findByAuctionId(auction.getId())).thenReturn(List.of());

        // when
        auctionStartService.start(1L, 1L);

        // then
        verify(eventPublisher, times(0)).publishEvent(any(NotificationEvent.class));
    }
}
