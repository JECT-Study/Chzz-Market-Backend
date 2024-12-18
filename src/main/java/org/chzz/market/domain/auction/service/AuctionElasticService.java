package org.chzz.market.domain.auction.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.auction.dto.event.AuctionDocumentDeleteEvent;
import org.chzz.market.domain.auction.dto.event.AuctionDocumentModifyEvent;
import org.chzz.market.domain.auction.dto.event.AuctionDocumentSaveEvent;
import org.chzz.market.domain.auction.dto.event.AuctionEndEvent;
import org.chzz.market.domain.auction.dto.event.AuctionStartEvent;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.entity.AuctionDocument;
import org.chzz.market.domain.auction.repository.AuctionElasticQueryRepository;
import org.chzz.market.domain.auction.repository.AuctionElasticRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionElasticService {
    private final AuctionElasticRepository auctionElasticRepository;
    private final AuctionElasticQueryRepository auctionElasticQueryRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void saveAuctionDocument(AuctionDocumentSaveEvent event) {
        auctionElasticRepository.save(AuctionDocument.from(event.auction()));
        log.info("경매 문서를 Elasticsearch에 저장 완료: auctionId={}", event.auction().getId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void modifyAuctionDocument(AuctionDocumentModifyEvent event) {
        auctionElasticQueryRepository.update(AuctionDocument.from(event.auction()));
        log.info("경매 문서를 Elasticsearch에 수정 완료: auctionId={}", event.auction().getId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void updateStartAuctionStatus(AuctionStartEvent event) {
        Auction auction = event.auction();
        auctionElasticQueryRepository.updateAuctionToStartedStatus(AuctionDocument.from(event.auction()));
        log.info("Elasticsearch에서 경매 상태를 '시작됨(STARTED)'으로 업데이트 완료: auctionId={}", auction.getId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void updateEndAuctionStatus(AuctionEndEvent event) {
        Auction auction = event.auction();
        auctionElasticQueryRepository.updateAuctionToEndedStatus(AuctionDocument.from(event.auction()));
        log.info("Elasticsearch에서 경매 상태를 '종료됨(ENDED)'으로 업데이트 완료: auctionId={}", auction.getId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void deleteDocument(AuctionDocumentDeleteEvent event) {
        auctionElasticRepository.deleteById(event.auction().getId());
        log.info("Elasticsearch에서 경매 문서 삭제 완료: auctionId={}", event.auction().getId());
    }
}
