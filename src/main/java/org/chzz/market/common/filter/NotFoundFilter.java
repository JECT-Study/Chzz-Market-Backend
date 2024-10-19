package org.chzz.market.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.chzz.market.common.error.GlobalErrorCode;
import org.chzz.market.common.error.GlobalException;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

/**
 *
 */
public class NotFoundFilter extends OncePerRequestFilter {

    private final List<HandlerMapping> handlerMappings;
    private final Map<String, HandlerExecutionChain> handlerCache = new ConcurrentHashMap<>();

    public NotFoundFilter(List<HandlerMapping> handlerMappings) {
        this.handlerMappings = handlerMappings;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String requestUri = request.getRequestURI();
        return requestUri.startsWith("/api-docs") || requestUri.startsWith("/swagger-ui");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        // 캐시에서 HandlerExecutionChain 검색 후 없으면 캐시 추가 (null 일 경우 map 추가 안함)
        HandlerExecutionChain handlerChain = handlerCache.computeIfAbsent(requestUri, uri -> findHandler(request));

        // 핸들러가 없으면 404 예외 처리
        if (handlerChain == null) {
            throw new GlobalException(GlobalErrorCode.RESOURCE_NOT_FOUND);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 핸들러 매핑에서 HandlerExecutionChain을 검색하는 스트림 메서드
     */
    private HandlerExecutionChain findHandler(HttpServletRequest request) {
        return handlerMappings.stream()
                .map(handlerMapping -> getHandlerExecutionChain(handlerMapping, request))
                .filter(chain -> chain != null
                        && !(chain.getHandler() instanceof ResourceHttpRequestHandler)) // 정적 리소스 핸들러 제외
                .findFirst()
                .orElse(null);
    }

    /**
     * 핸들러 매핑에서 HandlerExecutionChain을 가져옴
     */
    private HandlerExecutionChain getHandlerExecutionChain(HandlerMapping handlerMapping, HttpServletRequest request) {
        try {
            return handlerMapping.getHandler(request);
        } catch (Exception e) {
            return null;
        }
    }
}
