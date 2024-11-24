package org.chzz.market.common.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Collection;
import org.chzz.market.common.validation.annotation.NotEmptyMultipartList;
import org.springframework.web.multipart.MultipartFile;

public class NotEmptyMultipartListValidator implements
        ConstraintValidator<NotEmptyMultipartList, Collection<MultipartFile>> {

    @Override
    public boolean isValid(final Collection<MultipartFile> multipartFiles,
                           final ConstraintValidatorContext context) {
        for (MultipartFile file : multipartFiles) {
            if (file.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
