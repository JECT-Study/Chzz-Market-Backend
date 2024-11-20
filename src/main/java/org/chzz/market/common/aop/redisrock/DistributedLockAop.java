package org.chzz.market.common.aop.redisrock;

import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

/**
 * @DistributedLock 선언 시 수행되는 Aop class
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class DistributedLockAop {
    private static final String REDISSON_LOCK_PREFIX = "LOCK:";

    private final RedissonClient redissonClient;
    private final AopForTransaction aopForTransaction;

    @Around("@annotation(DistributedLock)")
    public Object lock(final ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

        String key = REDISSON_LOCK_PREFIX + CustomSpringELParser.getDynamicValue(signature.getParameterNames(),
                joinPoint.getArgs(), distributedLock.key());
        RLock rLock = redissonClient.getLock(key);  // (1) 락의 이름으로 RLock 인스턴스를 가져옴

        log.debug("Lock 획득 시도 중... [method: {}, key: {}]", method.getName(), key);

        try {
            boolean available = rLock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(),
                    distributedLock.timeUnit());  // (2) 정의된 waitTime까지 획득을 시도, 정의된 leaseTime이 지나면 잠금을 해제
            if (!available) {
                log.warn("Lock 획득 실패 [method: {}, key: {}]", method.getName(), key);
                return false;
            }
            log.debug("Lock 획득 성공 [method: {}, key: {}]", method.getName(), key);

            return aopForTransaction.proceed(joinPoint);  // (3) DistributedLock 어노테이션이 선언된 메서드를 별도의 트랜잭션으로 실행
        } catch (InterruptedException e) {
            log.error("Lock 획득 중 인터럽트가 발생 [method: {}, key: {}]", method.getName(), key, e);
            throw new InterruptedException();
        } finally {
            try {
                rLock.unlock();   // (4) 종료 시 무조건 락을 해제
                log.debug("Lock 해제 [method: {}, key: {}]", method.getName(), key);
            } catch (IllegalMonitorStateException e) {
                log.warn("이미 Lock 해제 [method: {}, key: {}]", method.getName(), key);
            }
        }
    }
}
