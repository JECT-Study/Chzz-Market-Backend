package org.chzz.market.domain.payment.service;


import lombok.Getter;
import org.chzz.market.domain.payment.dto.request.ApprovalRequest;
import org.chzz.market.domain.payment.dto.response.TossPaymentResponse;
import org.chzz.market.domain.payment.error.TossPaymentErrorCode;
import org.chzz.market.domain.payment.error.TossPaymentException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;


@Component
public class PaymentClient {
    private final WebClient webClient;
    private final PaymentAuthorizationHeaderProvider provider;
    private final String authorizationHeader;

    public PaymentClient(WebClient webClient,
                         PaymentAuthorizationHeaderProvider provider) {
        this.webClient = webClient;
        this.provider = provider;
        this.authorizationHeader = provider.getAuthorizationHeader();
        System.out.println("authorizationHeader = " + authorizationHeader);
    }

    public TossPaymentResponse confirmPayment(ApprovalRequest request) {
        return paymentGatewayRequest(TossApiEndpoint.APPROVAL)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(TossPaymentResponse.class)
                .doOnError(throwable -> {
                    WebClientResponseException exception = (WebClientResponseException) throwable;
                    throw new TossPaymentException(TossPaymentErrorCode.from(exception));
                })
                .block();
    }

    private RequestBodySpec paymentGatewayRequest(TossApiEndpoint endpoint) {
        return webClient.method(endpoint.getHttpMethod())
                .uri(endpoint.getPath())
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .contentType(MediaType.APPLICATION_JSON);
    }

    @Getter
    private enum TossApiEndpoint {
        APPROVAL("payments/confirm", HttpMethod.POST);

        private static final String ROOT_PATH = "https://api.tosspayments.com/v1/";
        private final String path;
        private final HttpMethod httpMethod;

        TossApiEndpoint(String path, HttpMethod httpMethod) {
            this.path = ROOT_PATH.concat(path);
            this.httpMethod = httpMethod;
        }
    }
}