package org.chzz.market.common.config;

import lombok.RequiredArgsConstructor;
import org.chzz.market.common.error.GlobalErrorCode;
import org.chzz.market.common.error.GlobalException;
import org.chzz.market.common.util.QuerydslOrderRegistry;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class CustomPageableHandlerMethodArgumentResolver extends PageableHandlerMethodArgumentResolver {
    private final QuerydslOrderRegistry querydslOrderRegistry;

    @Override
    public Pageable resolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer,
                                    NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) {
        Pageable pageable = super.resolveArgument(parameter, mavContainer, webRequest, binderFactory);

        pageable.getSort().stream().forEach(order -> {
            String property = order.getProperty();
            boolean isValid = querydslOrderRegistry.isValidOrderProperty(property);
            if (!isValid) {
                throw new GlobalException(GlobalErrorCode.UNSUPPORTED_SORT_TYPE);
            }
        });

        return pageable;
    }
}
