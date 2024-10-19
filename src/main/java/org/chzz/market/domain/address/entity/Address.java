package org.chzz.market.domain.address.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.chzz.market.domain.address.dto.DeliveryRequest;
import org.chzz.market.domain.base.entity.BaseTimeEntity;
import org.chzz.market.domain.user.entity.User;

@Getter
@Entity
@Builder
@Table
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull(message = "도로명 주소는 필수 입력 사항입니다.")
    @Size(min = 5, max = 100, message = "도로명 주소는 최소 5자에서 최대 100자까지 가능합니다.")
    @Column(nullable = false)
    private String roadAddress;

    @NotNull(message = "지번 주소는 필수 입력 사항입니다.")
    @Size(min = 5, max = 100, message = "지번 주소는 최소 5자에서 최대 100자까지 가능합니다.")
    @Column(nullable = false)
    private String jibun;

    @NotNull(message = "우편번호는 필수 입력 사항입니다.")
    @Pattern(regexp = "\\d{5}", message = "우편번호는 5자리 숫자여야 합니다.")
    @Column(nullable = false)
    private String zipcode;

    @NotNull(message = "상세 주소는 필수 입력 사항입니다.")
    @Size(min = 1, max = 100, message = "상세 주소는 최대 100자까지 가능합니다.")
    @Column(nullable = false)
    private String detailAddress;

    @NotNull(message = "수취인 이름은 필수 입력 사항입니다.")
    @Size(min = 2, max = 50, message = "수취인 이름은 최소 2자에서 최대 50자까지 가능합니다.")
    @Column(nullable = false)
    private String recipientName;

    @NotNull(message = "전화번호는 필수 입력 사항입니다.")
    @Pattern(regexp = "^(01[016789]-?\\d{3,4}-?\\d{4})$", message = "전화번호 형식이 올바르지 않습니다.")
    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private boolean isDefault;

    public boolean isOwner(Long userId) {
        return this.user.getId().equals(userId);
    }

    public void update(DeliveryRequest dto) {
        this.roadAddress = dto.roadAddress();
        this.jibun = dto.jibun();
        this.zipcode = dto.zipcode();
        this.detailAddress = dto.detailAddress();
        this.recipientName = dto.recipientName();
        this.phoneNumber = dto.phoneNumber();
        this.isDefault = dto.isDefault();
    }
}
