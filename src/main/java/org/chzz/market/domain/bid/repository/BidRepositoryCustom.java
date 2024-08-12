package org.chzz.market.domain.bid.repository;

import org.chzz.market.domain.bid.dto.query.BiddingRecord;
import org.chzz.market.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BidRepositoryCustom {
    Page<BiddingRecord> findUsersBidHistory(User user, Pageable pageable);
}
