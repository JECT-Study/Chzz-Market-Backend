package org.chzz.market.common.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.chzz.market.common.validation.annotation.NotEmptyMultipartList.List;
import org.chzz.market.common.validation.validator.NotEmptyMultipartListValidator;

@Documented
@Constraint(
        validatedBy = {NotEmptyMultipartListValidator.class}
)
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(List.class)
public @interface NotEmptyMultipartList {
    String message() default "파일은 최소 하나 이상 필요합니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface List {
        NotEmptyMultipartList[] value();
    }
}
