package org.chzz.market.util;

import org.chzz.market.domain.auction.dto.request.BaseRegisterRequest;
import org.chzz.market.domain.product.entity.Product;
import org.chzz.market.domain.user.entity.User;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;

public class ProductTestFactory {
    public static Product createProduct(BaseRegisterRequest request, User user) {
        try {
            Constructor<Product> constructor = Product.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            Product product = constructor.newInstance();

            ReflectionTestUtils.setField(product, "user", user);
            ReflectionTestUtils.setField(product, "name", request.getProductName());
            ReflectionTestUtils.setField(product, "description", request.getDescription());
            ReflectionTestUtils.setField(product, "category", request.getCategory());

            return product;
        } catch (Exception e) {
            throw new RuntimeException("테스트를 위한 상품 인스턴스 생성에 실패했습니다.", e);
        }
    }
}
