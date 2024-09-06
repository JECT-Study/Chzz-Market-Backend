package org.chzz.market.domain.user.repository;

import java.util.Optional;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.entity.User.ProviderType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByProviderTypeAndProviderId(ProviderType providerType, String providerId);
    Optional<User> findByNickname(String nickname);
}
