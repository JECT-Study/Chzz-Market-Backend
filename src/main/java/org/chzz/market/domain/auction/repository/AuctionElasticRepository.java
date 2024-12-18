package org.chzz.market.domain.auction.repository;

import org.chzz.market.domain.auction.entity.AuctionDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface AuctionElasticRepository extends ElasticsearchRepository<AuctionDocument, Long> {
}
