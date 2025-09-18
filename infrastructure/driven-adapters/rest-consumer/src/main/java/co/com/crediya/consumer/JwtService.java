package co.com.crediya.consumer;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Slf4j
@Service
public class JwtService {

    private final Key key;
    private final String issuer;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.issuer}") String issuer
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.issuer = issuer;
    }

    public Claims validateAndExtractClaims(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .requireIssuer(issuer)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            log.debug("Token validated locally for user: {}", claims.getSubject());
            return claims;

        } catch (ExpiredJwtException ex) {
            log.warn("Token expired for user: {}", ex.getClaims().getSubject());
            throw new SecurityException("Token has expired");

        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token: {}", ex.getMessage());
            throw new SecurityException("Unsupported JWT token");

        } catch (MalformedJwtException ex) {
            log.error("Malformed JWT token: {}", ex.getMessage());
            throw new SecurityException("Malformed JWT token");

        } catch (io.jsonwebtoken.security.SignatureException ex) {
            log.error("Invalid JWT signature: {}", ex.getMessage());
            throw new SecurityException("Invalid JWT signature");

        } catch (IllegalArgumentException ex) {
            log.error("JWT token is invalid: {}", ex.getMessage());
            throw new SecurityException("Invalid JWT token");
        }
    }

    public String extractUserId(Claims claims) {
        String userId = claims.get("userId", String.class);
        return userId != null ? userId : claims.getSubject();
    }

    public String extractRole(Claims claims) {
        return claims.get("role", String.class);
    }

    public boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    public long getRemainingTimeMs(Claims claims) {
        long expirationTime = claims.getExpiration().getTime();
        long currentTime = System.currentTimeMillis();
        return Math.max(0, expirationTime - currentTime);
    }
}