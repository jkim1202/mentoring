package org.example.mentoring.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {
    private String secret;
    private String refreshSecret;
    private long accessExpMinutes;
    private long refreshExpDays;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getRefreshSecret() {
        return refreshSecret;
    }

    public void setRefreshSecret(String refreshSecret) {
        this.refreshSecret = refreshSecret;
    }

    public long getAccessExpMinutes() {
        return accessExpMinutes;
    }

    public void setAccessExpMinutes(long accessExpMinutes) {
        this.accessExpMinutes = accessExpMinutes;
    }

    public long getRefreshExpDays() {
        return refreshExpDays;
    }

    public void setRefreshExpDays(long refreshExpDays) {
        this.refreshExpDays = refreshExpDays;
    }

    public long getAccessValidityMillis() {
        return Duration.ofMinutes(accessExpMinutes).toMillis();
    }

    public long getRefreshValidityMillis() {
        return Duration.ofDays(refreshExpDays).toMillis();
    }
}
