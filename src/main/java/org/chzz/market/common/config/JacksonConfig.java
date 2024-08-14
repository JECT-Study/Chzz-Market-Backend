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

        // JavaTimeModule 추가
        mapper.registerModule(new JavaTimeModule());

        // LocalDateTime을 ISO 8601 형식으로 직렬화하기 위한 설정
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // Page 직렬화 설정
        SimpleModule pageModule = new SimpleModule();
        pageModule.addSerializer(Page.class, pageResponseSerializer);
        mapper.registerModule(pageModule);

        return mapper;
    }
}