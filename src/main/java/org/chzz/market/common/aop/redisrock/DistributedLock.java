package org.chzz.market.common.aop.redisrock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Redisson 분산 락을 적용하는 어노테이션
 * 사용 예시:
 * (1) 단일 파라미터를 사용하여 락의 이름을 지정하는 경우
 * @DistributedLock(key = "#lockName")
 * public void shipment(String lockName) { // ... }
 *
 * (2) 복합 키를 사용하여 락의 이름을 지정하는 경우
 * @DistributedLock(key = "#model.getName().concat('-').concat(#model.getShipmentOrderNumber())")
 * public void shipment(ShipmentModel model) { // ... }
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    /**
     * 락의 이름
     */
    String key();

    /**
     * 락의 시간 단위
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 락을 기다리는 시간 (default - 5s) 락 획득을 위해 waitTime 만큼 대기한다
     */
    long waitTime() default 5L;

    /**
     * 락 임대 시간 (default - 3s) 락을 획득한 이후 leaseTime 이 지나면 락을 해제한다
     */
    long leaseTime() default 3L;
}
