package org.chzz.market.domain.auctionv2.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.chzz.market.domain.auctionv2.error.AuctionErrorCode.AUCTION_ACCESS_FORBIDDEN;
import static org.chzz.market.domain.auctionv2.error.AuctionErrorCode.AUCTION_NOT_FOUND;
import static org.chzz.market.domain.auctionv2.error.AuctionErrorCode.OFFICIAL_AUCTION_DELETE_FORBIDDEN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.chzz.market.domain.auctionv2.entity.AuctionV2;
import org.chzz.market.domain.auctionv2.error.AuctionException;
import org.chzz.market.domain.auctionv2.repository.AuctionV2Repository;
import org.chzz.market.domain.imagev2.service.ImageDeleteService;
import org.chzz.market.domain.likev2.entity.LikeV2;
import org.chzz.market.domain.likev2.repository.LikeV2Repository;
import org.chzz.market.domain.notification.event.NotificationEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class AuctionDeleteServiceTest {
    private static final String ERROR_CODE = "errorCode";

    @Mock
    private ImageDeleteService imageDeleteService;
    @Mock
    private AuctionV2Repository auctionRepository;
    @Mock
    private LikeV2Repository likeRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AuctionDeleteService auctionDeleteService;

    @Test
    void 정상_삭제_테스트() {
        // given
        AuctionV2 auction = mock(AuctionV2.class);
        LikeV2 like = mock(LikeV2.class);

        when(auctionRepository.findById(any())).thenReturn(Optional.of(auction));
        doNothing().when(auction).validateOwner(any());
        when(auction.isOfficialAuction()).thenReturn(false);
        when(likeRepository.findByAuctionId(auction.getId())).thenReturn(List.of(like));

        // when
        auctionDeleteService.delete(1L, 1L);

        // then
        verify(imageDeleteService, times(1)).deleteImages(auction.getImages());
        verify(auctionRepository, times(1)).delete(auction);
        verify(eventPublisher, times(1)).publishEvent(any(NotificationEvent.class));
    }

    @Test
    void 정상_삭제_테스트_좋아요_누른사람이_없을때_알림이벤트발행_하지않는다() {
        // given
        AuctionV2 auction = mock(AuctionV2.class);

        when(auctionRepository.findById(any())).thenReturn(Optional.of(auction));
        doNothing().when(auction).validateOwner(any());
        when(auction.isOfficialAuction()).thenReturn(false);
        when(likeRepository.findByAuctionId(auction.getId())).thenReturn(List.of());

        // when
        auctionDeleteService.delete(1L, 1L);

        // then
        verify(imageDeleteService, times(1)).deleteImages(auction.getImages());
        verify(auctionRepository, times(1)).delete(auction);
        verify(eventPublisher, times(0)).publishEvent(any(NotificationEvent.class));
    }

    @Test
    void 해당_경매를_찾을수_없을때_예외가_발생한다() {
        // when
        when(auctionRepository.findById(any())).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> auctionDeleteService.delete(1L, 1L))
                .isInstanceOf(AuctionException.class)
                .extracting(ERROR_CODE)
                .isEqualTo(AUCTION_NOT_FOUND);
    }

    @Test
    void 해당_경매에_주인이_아닐때_삭제시도_할경우_예외가_발생한다() {
        // given
        AuctionV2 auction = mock(AuctionV2.class);

        // when
        when(auctionRepository.findById(any())).thenReturn(Optional.of(auction));
        doThrow(new AuctionException(AUCTION_ACCESS_FORBIDDEN)).when(auction).validateOwner(any());

        // then
        assertThatThrownBy(() -> auctionDeleteService.delete(1L, 1L))
                .isInstanceOf(AuctionException.class)
                .extracting(ERROR_CODE)
                .isEqualTo(AUCTION_ACCESS_FORBIDDEN);
    }

    @Test
    void 사전경매가_아닐때_삭제시도_할경우_예외가_발생한다() {
        // given
        AuctionV2 auction = mock(AuctionV2.class);

        // when
        when(auctionRepository.findById(any())).thenReturn(Optional.of(auction));
        doNothing().when(auction).validateOwner(any());
        when(auction.isOfficialAuction()).thenReturn(true);

        // then
        assertThatThrownBy(() -> auctionDeleteService.delete(1L, 1L))
                .isInstanceOf(AuctionException.class)
                .extracting(ERROR_CODE)
                .isEqualTo(OFFICIAL_AUCTION_DELETE_FORBIDDEN);
    }
}
