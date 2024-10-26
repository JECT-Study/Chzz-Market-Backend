package org.chzz.market.common.dto;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;

public record ApiEndpoint(String url, HttpMethod method) {
    public boolean matches(HttpServletRequest request) {
        return request.getRequestURI().equals(url) && request.getMethod().equals(method.name());
    }
}
