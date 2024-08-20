package org.chzz.market.common.config;

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
        mapper.registerModule(new JavaTimeModule()); // Java 8의 LocalDateTime을 직렬화하기 위한 설정
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                false); // LocalDateTime을 ISO 8601 형식으로 직렬화하기 위한 설정
        SimpleModule module = new SimpleModule();
        module.addSerializer(Page.class, pageResponseSerializer);
        mapper.registerModule(module);

        return mapper;
    }
}
