package org.chzz.market.domain.user.entity;

import static org.chzz.market.domain.user.entity.User.UserRole.TEMP_USER;
import static org.chzz.market.domain.user.entity.User.UserRole.USER;
import static org.chzz.market.domain.user.error.UserErrorCode.USER_ALREADY_REGISTERED;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.chzz.market.domain.base.entity.BaseTimeEntity;
import org.chzz.market.domain.user.dto.request.UpdateUserProfileRequest;
import org.chzz.market.domain.user.dto.request.UserCreateRequest;
import org.chzz.market.domain.user.error.exception.UserException;
import org.hibernate.annotations.DynamicUpdate;

@Getter
@Entity
@Builder
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
@AllArgsConstructor
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false)
    private String providerId;

    @Column(length = 25)
    private String nickname;

    @Column(nullable = false)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String profileImageUrl;

    @Column(columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    @Column(columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    private ProviderType providerType;

    @Column(columnDefinition = "binary(16)", unique = true)
    private UUID customerKey;

    @PrePersist
    public void prePersist() {
        if (customerKey == null) {
            customerKey = UUID.randomUUID();//TODO 2024 09 11 22:33:46 : 분산 시스템에서 사용 가능한 UUID 생성 고려
        }
    }

    public boolean isTempUser() {
        return userRole == TEMP_USER;
    }

    public void createUser(UserCreateRequest userCreateRequest) {
        if (this.userRole.equals(USER)) {
            throw new UserException(USER_ALREADY_REGISTERED);
        }
        this.nickname = userCreateRequest.getNickname();
        this.userRole = USER;
        if (!StringUtils.isBlank(userCreateRequest.getBio())) {
            this.bio = userCreateRequest.getBio();
        }
    }

    public void updateProfile(UpdateUserProfileRequest request, String profileImageUrl) {
        this.nickname = request.getNickname();
        this.bio = request.getBio();
        this.profileImageUrl = profileImageUrl;
    }

    public void anonymize() {
        this.userRole = UserRole.DELETED_USER;
        this.email = null;
        this.nickname = "탈퇴한 사용자";
        this.bio = null;
        this.profileImageUrl = null;
        this.providerId = null;
        this.providerType = null;
        this.customerKey = null;
    }

    @Getter
    @AllArgsConstructor
    public enum UserRole {
        TEMP_USER("ROLE_TEMP_USER"),
        USER("ROLE_USER"),
        ADMIN("ROLE_ADMIN"),
        DELETED_USER("ROLE_DELETED_USER");

        private final String value;
    }

    @Getter
    @AllArgsConstructor
    public enum ProviderType {
        NAVER("naver"),
        KAKAO("kakao");

        private final String name;
    }
}
