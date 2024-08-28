package org.chzz.market.util;

import org.chzz.market.domain.auction.dto.request.RegisterAuctionRequest;
import org.chzz.market.domain.auction.dto.request.BaseRegisterRequest;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.product.entity.Product;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.time.LocalDateTime;

import static org.chzz.market.domain.auction.entity.Auction.*;

public class AuctionTestFactory {
    public static Auction createAuction(Product product, BaseRegisterRequest request, AuctionStatus status) {
        try {
            // 리플렉션을 사용하여 protected 생성자에 접근
            Constructor<Auction> constructor = Auction.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            Auction auction = constructor.newInstance();

            // 리플렉션을 사용하여 필드 값 설정
            ReflectionTestUtils.setField(auction, "product", product);
            ReflectionTestUtils.setField(auction, "status", status);
            ReflectionTestUtils.setField(auction, "endDateTime", LocalDateTime.now().plusHours(24));

            return auction;
        } catch (Exception e) {
            throw new RuntimeException("테스트를 위한 경매 인스턴스 생성에 실패했습니다.", e);
        }
    }
}
