package org.chzz.market.common.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.chzz.market.common.validation.annotation.ThousandMultiple;

public class ThousandMultipleValidator implements ConstraintValidator<ThousandMultiple, Number> {

    @Override
    public boolean isValid(Number value, ConstraintValidatorContext context) {
        return value != null && value.longValue() > 0 && value.longValue() % 1000 == 0;
    }
}
