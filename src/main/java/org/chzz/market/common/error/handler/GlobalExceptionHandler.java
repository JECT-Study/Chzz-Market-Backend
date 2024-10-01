package org.chzz.market.common.error.handler;


import static org.chzz.market.common.error.GlobalErrorCode.EXTERNAL_API_ERROR;

import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.common.error.ErrorCode;
import org.chzz.market.common.error.ErrorResponse;
import org.chzz.market.common.error.GlobalErrorCode;
import org.chzz.market.common.error.GlobalException;
import org.chzz.market.common.error.exception.BusinessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final String LOG_FORMAT = "\nException Class = {}\nResponse Code = {}\nMessage = {}";

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<?> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        GlobalErrorCode errorCode = GlobalErrorCode.UNSUPPORTED_PARAMETER_TYPE;
        logException(e, errorCode);
        return handleExceptionInternal(errorCode);
    }

    @Nullable
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            final MissingServletRequestParameterException e,
            final HttpHeaders headers,
            final HttpStatusCode status,
            final WebRequest request) {
        GlobalErrorCode errorCode = GlobalErrorCode.UNSUPPORTED_PARAMETER_NAME;
        logException(e, errorCode);
        return handleExceptionInternal(errorCode);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(final NoHandlerFoundException e,
                                                                   final HttpHeaders headers,
                                                                   final HttpStatusCode status,
                                                                   final WebRequest request) {
        final GlobalErrorCode errorCode = GlobalErrorCode.RESOURCE_NOT_FOUND;
        logException(e, errorCode);
        return handleExceptionInternal(errorCode);
    }

    @Nullable
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(MissingServletRequestPartException ex,
                                                                     HttpHeaders headers, HttpStatusCode status,
                                                                     WebRequest request) {
        final GlobalErrorCode errorCode = GlobalErrorCode.INVALID_REQUEST_PARAMETER;
        logException(ex, errorCode);
        return handleExceptionInternal(errorCode);
    }

    /**
     * @param e 비즈니스 로직상 발생한 커스텀 예외
     * @return 커스텀 예외 메세지와 상태를 담은 {@link ResponseEntity}
     */
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<?> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        logException(e, errorCode);
        return handleExceptionInternal(errorCode);
    }

    @ExceptionHandler(GlobalException.class)
    protected ResponseEntity<?> handleGlobalException(GlobalException e) {
        GlobalErrorCode errorCode = e.getErrorCode();
        logException(e, errorCode);
        return handleExceptionInternal(errorCode);
    }

    @ExceptionHandler(WebClientResponseException.class)
    protected ResponseEntity<?> handleWebClientResponseException(final WebClientResponseException exception) {
        logException(exception, EXTERNAL_API_ERROR, exception.getResponseBodyAsString());
        return handleExceptionInternal(EXTERNAL_API_ERROR);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        GlobalErrorCode errorCode = GlobalErrorCode.INVALID_REQUEST_PARAMETER;
        String detailedErrorMessage = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        logException(ex, errorCode, detailedErrorMessage);

        ErrorResponse errorResponse = ErrorResponse.of(errorCode, detailedErrorMessage);
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(errorResponse);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        GlobalErrorCode errorCode = GlobalErrorCode.INVALID_REQUEST_PARAMETER;
        logException(ex, errorCode, ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.from(errorCode);
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(errorResponse);
    }

    private ResponseEntity<Object> handleExceptionInternal(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ErrorResponse.from(errorCode));
    }

    private void logException(final Exception e, final ErrorCode errorCode) {
        log.error(LOG_FORMAT,
                e.getClass(),
                errorCode.getHttpStatus().value(),
                errorCode.getMessage());
    }

    private void logException(final Exception e, final ErrorCode errorCode, final String message) {
        log.error(LOG_FORMAT,
                e.getClass(),
                errorCode.getHttpStatus().value(),
                message);
    }
}
