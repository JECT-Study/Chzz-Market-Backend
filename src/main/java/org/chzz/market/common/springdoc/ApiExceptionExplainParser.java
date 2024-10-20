package org.chzz.market.common.springdoc;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Builder;
import org.chzz.market.common.error.ErrorCode;
import org.chzz.market.common.error.ErrorResponse;
import org.flywaydb.core.internal.util.StringUtils;
import org.springframework.web.method.HandlerMethod;

public final class ApiExceptionExplainParser {

    public static void parse(Operation operation, HandlerMethod handlerMethod) {
        ApiResponseExplanations annotation = handlerMethod.getMethodAnnotation(ApiResponseExplanations.class);

        if (annotation != null) {
            generateExceptionResponseDocs(operation, annotation.errors());
        }
    }

    private static void generateExceptionResponseDocs(Operation operation, ApiExceptionExplanation[] exceptions) {
        ApiResponses responses = operation.getResponses();

        Map<Integer, List<ExampleHolder>> holders = Arrays.stream(exceptions)
                .map(ExampleHolder::from)
                .collect(Collectors.groupingBy(ExampleHolder::httpStatus));

        addExamplesToResponses(responses, holders);
    }

    private static void addExamplesToResponses(ApiResponses responses, Map<Integer, List<ExampleHolder>> holders) {
        holders.forEach((httpStatus, exampleHolders) -> {
            Content content = new Content();
            MediaType mediaType = new MediaType();
            ApiResponse response = new ApiResponse();

            exampleHolders.forEach(holder -> mediaType.addExamples(holder.name(), holder.holder()));
            content.addMediaType("application/json", mediaType);
            response.setContent(content);

            responses.addApiResponse(String.valueOf(httpStatus), response);
        });
    }

    @Builder(access = AccessLevel.PRIVATE)
    private record ExampleHolder(int httpStatus, String name, String mediaType, String description, Example holder) {

        static ExampleHolder from(ApiExceptionExplanation annotation) {
            ErrorCode errorCode = getErrorCode(annotation);

            return ExampleHolder.builder()
                    .httpStatus(errorCode.getHttpStatus().value())
                    .name(StringUtils.hasText(annotation.name()) ? annotation.name() : errorCode.name())
                    .mediaType("application/json")
                    .description(annotation.description())
                    .holder(createExample(errorCode, annotation.description()))
                    .build();
        }

        @SuppressWarnings("unchecked")
        public static <E extends Enum<E> & ErrorCode> E getErrorCode(ApiExceptionExplanation annotation) {
            Class<E> enumClass = (Class<E>) annotation.value();
            return Enum.valueOf(enumClass, annotation.constant());
        }

        private static Example createExample(ErrorCode errorCode, String description) {
            ErrorResponse response = ErrorResponse.of(errorCode, new String[]{errorCode.getMessage()});

            Example example = new Example();
            example.setValue(response);
            example.setDescription(description);

            return example;
        }
    }
}
