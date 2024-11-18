package org.chzz.market.domain.paymentv2.respository;

import org.chzz.market.domain.paymentv2.entity.PaymentV2;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentV2Repository extends JpaRepository<PaymentV2, Long> {
}
