package org.chzz.market.domain.bid.service;

import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.bid.dto.query.BiddingRecord;
import org.chzz.market.domain.bid.repository.BidRepository;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BidService {
    private final BidRepository bidRepository;
    private final UserRepository userRepository;//TODO 2024 08 04 14:54:26 : to be removed


    public Page<BiddingRecord> inquireBidHistory(Pageable pageable) {
        User user=userRepository.findById(1L).orElseThrow();//TODO 2024 08 04 15:29:29 : to be removed
        return bidRepository.findUsersBidHistory(user,pageable);
    }
}
