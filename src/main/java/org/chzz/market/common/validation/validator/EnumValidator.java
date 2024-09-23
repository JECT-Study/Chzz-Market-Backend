package org.chzz.market.common.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import org.chzz.market.common.validation.annotation.EnumValue;

public class EnumValidator implements ConstraintValidator<EnumValue, String> {
    private EnumValue enumValue;

    @Override
    public void initialize(final EnumValue constraintAnnotation) {
        this.enumValue = constraintAnnotation;
    }

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext context) {
        final Enum<?>[] enumConstants = this.enumValue.enumClass().getEnumConstants();
        if (enumConstants == null) {
            return false;
        }

        // Enum의 모든 상수들과 대소문자 구분 없이 비교
        return Arrays.stream(enumValue.enumClass().getEnumConstants())
                .anyMatch(enumConstant -> value.trim().equalsIgnoreCase(enumConstant.name()));
    }
}
