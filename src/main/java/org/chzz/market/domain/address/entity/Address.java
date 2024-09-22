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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.chzz.market.domain.address.dto.request.AddressDto;
import org.chzz.market.domain.base.entity.BaseTimeEntity;
import org.chzz.market.domain.user.entity.User;

@Getter
@Entity
@Table
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column
    private String roadAddress;

    @Column
    private String jibun;

    @Column
    private String zipcode;

    @Column
    private String detailAddress;

    public static Address toEntity(User user, AddressDto dto) {
        return new Address(user, dto.roadAddress(), dto.jibun(), dto.zipcode(), dto.detailAddress());
    }

    private Address(User user, String roadAddress, String jibun, String zipcode, String detailAddress) {
        this.user = user;
        this.roadAddress = roadAddress;
        this.jibun = jibun;
        this.zipcode = zipcode;
        this.detailAddress = detailAddress;
    }
}
