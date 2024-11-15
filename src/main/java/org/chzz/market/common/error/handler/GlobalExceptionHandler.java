package org.chzz.market.common.error.handler;


import static org.chzz.market.common.error.GlobalErrorCode.EXTERNAL_API_ERROR;

import java.io.IOException;
import java.util.List;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.common.error.ErrorCode;
import org.chzz.market.common.error.ErrorResponse;
import org.chzz.market.common.error.GlobalErrorCode;
import org.chzz.market.common.error.GlobalException;
import org.chzz.market.common.error.exception.BusinessException;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final String LOG_FORMAT = "\nException Class = {}\nResponse Code = {}\nMessage = {}";

    // 1. 커스텀 예외 핸들러

    /**
     * 비즈니스 로직에서 정의한 예외가 발생할 때 처리
     */
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<?> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        logException(e, errorCode);
        return handleExceptionInternal(errorCode);
    }

    /**
     * 시스템 전역에서 발생하는 예외를 처리
     */
    @ExceptionHandler(GlobalException.class)
    protected ResponseEntity<?> handleGlobalException(GlobalException e) {
        GlobalErrorCode errorCode = e.getErrorCode();
        logException(e, errorCode);
        return handleExceptionInternal(errorCode);
    }

    /**
     * 외부 API 호출 시 발생하는 WebClient 관련 예외 처리
     */
    @ExceptionHandler(WebClientResponseException.class)
    protected ResponseEntity<?> handleWebClientResponseException(final WebClientResponseException exception) {
        logException(exception, EXTERNAL_API_ERROR, exception.getResponseBodyAsString());
        return handleExceptionInternal(EXTERNAL_API_ERROR);
    }

    /**
     * 요청 매개변수의 타입이 잘못된 경우 발생
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<?> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        GlobalErrorCode errorCode = GlobalErrorCode.UNSUPPORTED_PARAMETER_TYPE;
        logException(e, errorCode);
        return handleExceptionInternal(errorCode);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Object> handleIOException(IOException e) {
        if (e.getMessage().contains("Broken pipe")) {
            return null; // 연결이 끊어진 경우 응답을 하지 않음
        } else {
            GlobalErrorCode errorCode = GlobalErrorCode.INTERNAL_SERVER_ERROR;
            logException(e, errorCode);
            return handleExceptionInternal(errorCode);
        }
    }

    // 2. ResponseEntityExceptionHandler에서 오버라이드된 핸들러

    /**
     * 필수 요청 매개변수가 누락된 경우 발생
     */
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            @NonNull final MissingServletRequestParameterException e,
            @NonNull final HttpHeaders headers,
            @NonNull final HttpStatusCode status,
            @NonNull final WebRequest request) {
        GlobalErrorCode errorCode = GlobalErrorCode.UNSUPPORTED_PARAMETER_NAME;
        logException(e, errorCode);
        return handleExceptionInternal(errorCode);
    }

    /**
     * 존재하지 않는 URL 요청 시 발생
     */
    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(@NonNull final NoHandlerFoundException e,
                                                                   @NonNull final HttpHeaders headers,
                                                                   @NonNull final HttpStatusCode status,
                                                                   @NonNull final WebRequest request) {
        final GlobalErrorCode errorCode = GlobalErrorCode.RESOURCE_NOT_FOUND;
        logException(e, errorCode);
        return handleExceptionInternal(errorCode);
    }

    /**
     * 멀티파트 요청의 필수 파트가 누락된 경우 발생
     */
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(@NonNull MissingServletRequestPartException ex,
                                                                     @NonNull HttpHeaders headers,
                                                                     @NonNull HttpStatusCode status,
                                                                     @NonNull WebRequest request) {
        final GlobalErrorCode errorCode = GlobalErrorCode.INVALID_REQUEST_PARAMETER;
        logException(ex, errorCode);
        return handleExceptionInternal(errorCode);
    }

    /**
     * 요청의 유효성 검사 실패 시 발생
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(@NonNull MethodArgumentNotValidException ex,
                                                                  @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatusCode status,
                                                                  @NonNull WebRequest request) {

        GlobalErrorCode errorCode = GlobalErrorCode.VALIDATION_FAILED;
        String[] detailedErrorMessages = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toArray(String[]::new);
        logException(ex, errorCode, detailedErrorMessages);

        ErrorResponse errorResponse = ErrorResponse.of(errorCode, detailedErrorMessages);
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(errorResponse);
    }

    /**
     * @ReuqestParam, @PathVariable 에서 발생하는 유효성 검사 에러 핸들러
     */
    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            @NonNull HandlerMethodValidationException ex,
            @NonNull HttpHeaders headers, @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        GlobalErrorCode errorCode = GlobalErrorCode.VALIDATION_FAILED;

        // 모든 오류를 추출
        List<? extends MessageSourceResolvable> allErrors = ex.getAllErrors();
        log.info("allErrors = {}", allErrors);

        // 필드명과 기본 메시지를 조합하여 "필드명: 메시지" 형식으로 변환
        String[] errorMessages = allErrors.stream()
                .map(error -> {
                    String[] codes = error.getCodes();
                    String fieldName = (codes != null && codes.length > 0)
                            ? codes[0].substring(codes[0].lastIndexOf('.') + 1) // 필드명 추출
                            : "Unknown field";
                    return fieldName + ": " + error.getDefaultMessage(); // 필드명과 기본 메시지 결합
                })
                .toArray(String[]::new);

        logException(ex, errorCode, errorMessages);

        ErrorResponse errorResponse = ErrorResponse.of(errorCode, errorMessages);
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(errorResponse);
    }

    /**
     * 읽을 수 없는 HTTP 메시지가 수신된 경우 발생
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(@NonNull HttpMessageNotReadableException ex,
                                                                  @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatusCode status,
                                                                  @NonNull WebRequest request) {

        GlobalErrorCode errorCode = GlobalErrorCode.INVALID_REQUEST_PARAMETER;
        logException(ex, errorCode, ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.from(errorCode);
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(errorResponse);
    }

    /**
     * 예외 처리 응답 생성 메서드
     */
    private ResponseEntity<Object> handleExceptionInternal(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ErrorResponse.from(errorCode));
    }

    // 3. 예외 로깅 메서드들

    /**
     * 예외 정보를 로깅 (ErrorCode 메시지 사용)
     */
    private void logException(final Exception e, final ErrorCode errorCode) {
        log.error(LOG_FORMAT,
                e.getClass(),
                errorCode.getHttpStatus().value(),
                errorCode.getMessage());
    }

    /**
     * 예외 정보를 로깅 (단일 메시지 사용)
     */
    private void logException(final Exception e, final ErrorCode errorCode, final String message) {
        log.error(LOG_FORMAT,
                e.getClass(),
                errorCode.getHttpStatus().value(),
                message);
    }

    /**
     * 예외 정보를 로깅 (다중 메시지 사용)
     */
    private void logException(final Exception e, final ErrorCode errorCode, final String[] message) {
        log.error(LOG_FORMAT,
                e.getClass(),
                errorCode.getHttpStatus().value(),
                String.join("; ", message));
    }
}
