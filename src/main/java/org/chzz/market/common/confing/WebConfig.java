package org.chzz.market.common.confing;

import lombok.RequiredArgsConstructor;
import org.chzz.market.common.util.StringToEnumConverterFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final StringToEnumConverterFactory stringToEnumConverterFactory;

    @Override
    public void addFormatters(final FormatterRegistry registry) {
        registry.addConverterFactory(stringToEnumConverterFactory);
    }

}
