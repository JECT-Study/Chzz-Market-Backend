package org.chzz.market.domain.bank_account.entity;

import static org.chzz.market.common.error.GlobalErrorCode.INVALID_REQUEST_PARAMETER;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Arrays;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.chzz.market.common.error.GlobalException;
import org.chzz.market.domain.base.entity.BaseTimeEntity;
import org.chzz.market.domain.user.entity.User;

@Getter
@Entity
@Table
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BankAccount extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bank_account_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    //TODO 2024 07 17 21:45:15 : validation
    @Column(nullable = false)
    private String number;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(255)")
    private BankName name;

    public void specifyUser(User user) {
        this.user = user;
    }

    @Getter
    @RequiredArgsConstructor
    public enum BankName {
        NH("농협은행"),
        KB("국민은행"),
        KAKAO("카카오뱅크"),
        SHINHAN("신한은행"),
        WOORI("우리은행"),
        IBK("기업은행"),
        HANA("하나은행"),
        SAEMAUL("새마을금고"),
        CITI("씨티은행"),
        KBANK("케이뱅크");

        private final String displayName;

        public static BankName from(String bankName) {
            return Arrays.stream(BankName.values())
                    .filter(name -> name.name().equalsIgnoreCase(bankName))
                    .findFirst()
                    .orElseThrow(() -> new GlobalException(INVALID_REQUEST_PARAMETER));
        }
    }
}
