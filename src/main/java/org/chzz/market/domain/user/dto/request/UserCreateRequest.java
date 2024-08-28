package org.chzz.market.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.chzz.market.domain.bank_account.entity.BankAccount;
import org.chzz.market.domain.bank_account.entity.BankAccount.BankName;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UserCreateRequest {
    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Size(max = 15, message = "닉네임은 15자 이내여야 합니다.")
    private String nickname;

    @NotNull(message = "은행을 선택해주세요.")
    private BankName bankName;

    @NotBlank(message = "계좌번호는 필수 입력 항목입니다.")
    @Pattern(regexp = "\\d{10,14}", message = "계좌번호는 10자에서 14자리의 숫자로 구성되어야 합니다.")
    private String accountNumber;

    private String bio;

    @Pattern(regexp = "^(http|https)://.*$|^$", message = "링크는 유효한 URL 형식이어야 합니다.")
    private String link;

    public BankAccount toBankAccount() {
        return BankAccount.builder()
                .name(bankName)
                .number(accountNumber)
                .build();
    }
}
