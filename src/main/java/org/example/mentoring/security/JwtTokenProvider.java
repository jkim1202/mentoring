package org.example.mentoring.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;
import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtTokenProvider {
    private static final long ACCESS_VALIDITY_TIME = 1000 * 60 * 15;
    private static final long REFRESH_VALIDITY_TIME = 1000L * 60 * 60 * 24 * 7;
    @Value("${JWT_ACCESS_SECRET}")
    private String accessSecret;
    @Value("${JWT_REFRESH_SECRET}")
    private String refreshSecret;

    private SecretKey accessKey;
    private SecretKey refreshKey;
    @PostConstruct
    void init() {
        this.accessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret));
        this.refreshKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecret));
    }

    public String generateAccessToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + ACCESS_VALIDITY_TIME);
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(accessKey)
                .compact();
    }
    public String generateRefreshToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + REFRESH_VALIDITY_TIME);
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(refreshKey)
                .compact();
    }

    public String getEmailFromToken(String token) {
        validateToken(token,accessKey);
        return getClaimFromToken(token,accessKey,Claims::getSubject);
    }

    private String getEmailFromToken(String token, SecretKey secretKey) {
        validateToken(token,secretKey);
        return getClaimFromToken(token,secretKey,Claims::getSubject);
    }

    public boolean validateAccessToken(String token, UserDetails userDetails) {
        validateToken(token,accessKey);
        String tokenEmail = getEmailFromToken(token, accessKey);
        return tokenEmail != null && tokenEmail.equals(userDetails.getUsername());
    }

    public boolean validateRefreshToken(String token, UserDetails userDetails) {
        validateToken(token,refreshKey);
        String tokenEmail = getEmailFromToken(token, refreshKey);
        return tokenEmail != null && tokenEmail.equals(userDetails.getUsername());
    }

    public boolean isTokenExpired(String token) {
        Date expiration = getClaimFromToken(token,accessKey,Claims::getExpiration);
        return expiration.before(new Date());
    }

    private <T> T getClaimFromToken(String token, SecretKey key, Function<Claims, T> claimsResolver) {
        Claims claims = getAllClaimsFromToken(token, key);
        return claimsResolver.apply(claims);
    }

    public Claims getAllClaimsFromToken(String token, SecretKey key) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private void validateToken(String token, SecretKey key) {
        try{
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ErrorCode.AUTH_EXPIRED_TOKEN);
        } catch (UnsupportedJwtException e){
            throw new BusinessException(ErrorCode.AUTH_UNSUPPORTED_TOKEN);
        } catch (MalformedJwtException | SecurityException | IllegalArgumentException e){
            throw new BusinessException(ErrorCode.AUTH_INVALID_TOKEN);
        }
    }
}
