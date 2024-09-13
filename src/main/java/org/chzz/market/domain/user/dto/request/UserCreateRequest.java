package org.chzz.market.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.chzz.market.common.validation.annotation.EnumValue;
import org.chzz.market.domain.bank_account.entity.BankAccount;
import org.chzz.market.domain.bank_account.entity.BankAccount.BankName;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UserCreateRequest {
    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Size(max = 15, message = "닉네임은 15자 이내여야 합니다.")
    private String nickname;

    @NotBlank(message = "은행 이름은 필수 입력 항목입니다.")
    @EnumValue(enumClass = BankName.class, message = "유효하지 않은 은행 이름입니다.")
    private String bankName;

    @NotBlank(message = "계좌번호는 필수 입력 항목입니다.")
    @Pattern(regexp = "\\d{10,14}", message = "계좌번호는 10자에서 14자리의 숫자로 구성되어야 합니다.")
    private String accountNumber;

    @Size(max = 500, message = "자기소개는 500자 이내여야 합니다.")
    private String bio;

    @Pattern(regexp = "^(http|https)://.*$|^$", message = "링크는 유효한 URL 형식이어야 합니다.")
    private String link;

    public BankAccount toBankAccount() {
        return BankAccount.builder()
                .name(BankName.from(bankName))
                .number(accountNumber)
                .build();
    }
}
