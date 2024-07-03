package org.chzz.market.domain;

import org.chzz.market.common.DatabaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@DatabaseTest
class TestRepositoryTest {
    @Autowired
    private TestRepository testRepository;
    @Test
    void test() {
        // given
        org.chzz.market.domain.Test saved = testRepository.save(new org.chzz.market.domain.Test());
        
        // when
        String val = testRepository.findVal(saved);
        
        // then
        assertThat(val).isEqualTo(saved.getValue());
    }
    
}