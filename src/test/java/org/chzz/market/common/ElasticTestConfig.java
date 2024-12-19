package org.chzz.market.common;

import org.chzz.market.domain.auction.repository.AuctionElasticRepository;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

@TestConfiguration
public class ElasticTestConfig {

    @Bean
    public AuctionElasticRepository auctionElasticRepository() {
        return Mockito.mock(AuctionElasticRepository.class);
    }

    @Bean
    public ElasticsearchOperations elasticsearchOperations() {
        return Mockito.mock(ElasticsearchOperations.class);
    }
}
