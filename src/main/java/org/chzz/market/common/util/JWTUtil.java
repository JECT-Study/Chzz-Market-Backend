package org.chzz.market.common.util;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.chzz.market.domain.token.entity.TokenType;
import org.chzz.market.domain.token.error.TokenErrorCode;
import org.chzz.market.domain.token.error.exception.TokenException;
import org.chzz.market.domain.user.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JWTUtil {
    private static final String ID = "id";
    private static final String ROLE = "role";
    private static final String TYPE = "type";

    private SecretKey secretKey;

    public JWTUtil(@Value("${spring.jwt.secret}") String secret) {
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    public Long getId(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get(ID, Long.class);
    }

    public String getRole(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload()
                .get(ROLE, String.class);
    }

    public String getType(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload()
                .get(TYPE, String.class);
    }

    public void validateToken(String token, TokenType tokenType) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
        } catch (ExpiredJwtException e) {
            throw new TokenException(TokenErrorCode.EXPIRED_TOKEN);
        } catch (UnsupportedJwtException e) {
            throw new TokenException(TokenErrorCode.UNSUPPORTED_TOKEN);
        } catch (MalformedJwtException e) {
            throw new TokenException(TokenErrorCode.MALFORMED_TOKEN);
        } catch (SignatureException e) {
            throw new TokenException(TokenErrorCode.SIGNATURE_EXCEPTION);
        } catch (JwtException e) {
            throw new TokenException(TokenErrorCode.INVALID_TOKEN);
        } catch (Exception e) {
            throw new TokenException(TokenErrorCode.MALFORMED_TOKEN);
        }
        validateTokenType(token, tokenType);
    }

    public String createToken(User user, TokenType tokenType) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expirationTime = now.plusSeconds(tokenType.getExpirationTime());
        return Jwts.builder()
                .claim(TYPE, tokenType.name())
                .claim(ID, user.getId())
                .claim(ROLE, user.getUserRole())
                .issuedAt(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()))
                .expiration(Date.from(expirationTime.atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(secretKey)
                .compact();
    }

    private void validateTokenType(String token, TokenType tokenType) {
        String type = getType(token);
        if (!type.equals(tokenType.name())) {
            throw new TokenException(TokenErrorCode.INVALID_TOKEN_TYPE);
        }
    }

}
