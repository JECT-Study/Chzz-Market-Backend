package org.chzz.market.common.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.chzz.market.domain.token.entity.TokenType;

public class CookieUtil {
    public static Cookie createTokenCookie(String token, TokenType tokenType) {
        Cookie cookie = new Cookie(tokenType.name(), token);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(tokenType.getExpirationTime());
        cookie.setPath("/");
        return cookie;
    }

    public static Cookie getCookieByName(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie;
                }
            }
        }
        return null;
    }

    public static Cookie expireCookie(String cookieName) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        return cookie;
    }
}
