package org.chzz.market.domain.user.entity;

import static org.chzz.market.domain.user.entity.User.UserRole.TEMP_USER;
import static org.chzz.market.domain.user.entity.User.UserRole.USER;
import static org.chzz.market.domain.user.error.UserErrorCode.USER_ALREADY_REGISTERED;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.bank_account.entity.BankAccount;
import org.chzz.market.domain.base.entity.BaseTimeEntity;
import org.chzz.market.domain.bid.entity.Bid;
import org.chzz.market.domain.like.entity.Like;
import org.chzz.market.domain.payment.entity.Payment;
import org.chzz.market.domain.product.entity.Product;
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
    @Email(message = "invalid type of email")
    private String email;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String link;

    // 구현 방식에 따라 권한 설정이 달라질 수 있어 임의로 열거체 선언 하였습니다
    @Column(columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    @Column(columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    private ProviderType providerType;

    @Column(columnDefinition = "binary(16)", unique = true, nullable = false)
    private UUID customerKey;

    @Builder.Default
    @OneToMany(mappedBy = "bidder", fetch = FetchType.LAZY)
    private List<Bid> bids = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Product> products = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Like> likes = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "payer", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Payment> payments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<BankAccount> bankAccounts = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (customerKey == null) {
            customerKey = UUID.randomUUID();//TODO 2024 09 11 22:33:46 : 분산 시스템에서 사용 가능한 UUID 생성 고려
        }
    }

    public void addBankAccount(BankAccount bankAccount) {
        this.bankAccounts.add(bankAccount);
        bankAccount.specifyUser(this);
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
        if (!StringUtils.isBlank(userCreateRequest.getLink())) {
            this.link = userCreateRequest.getLink();
        }
    }

    public void updateProfile(String nickname, String bio, String link) {
        this.nickname = nickname;
        this.bio = bio;
        this.link = link;
    }

    private Stream<Auction> getAuctionsFromBids() {
        return bids.stream().map(Bid::getAuction);
    }

    public long getOngoingAuctionCount() {
        return getAuctionsFromBids().filter(Auction::isProceeding).count();
    }

    public long getSuccessfulBidCount() {
        return getAuctionsFromBids().filter(auction -> auction.isSuccessfulBidFor(this.id)).count();
    }

    public long getFailedBidCount() {
        return getAuctionsFromBids().filter(auction -> auction.isFailedBidFor(this.id)).count();
    }

    @Getter
    @AllArgsConstructor
    public enum UserRole {
        TEMP_USER("ROLE_TEMP_USER"),
        USER("ROLE_USER"),
        ADMIN("ROLE_ADMIN");

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
