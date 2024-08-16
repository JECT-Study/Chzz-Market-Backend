package org.chzz.market.common.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;

@Configuration
@RequiredArgsConstructor
public class JacksonConfig {
    private final PageResponseSerializer pageResponseSerializer;

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Page 직렬화 설정
        SimpleModule pageModule = new SimpleModule();
        pageModule.addSerializer(Page.class, pageResponseSerializer);
        mapper.registerModule(pageModule);

        // JavaTimeModule 추가
        mapper.registerModule(new JavaTimeModule());

        return mapper;
    }
}
