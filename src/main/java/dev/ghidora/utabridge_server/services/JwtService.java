package dev.ghidora.utabridge_server.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private static final int VALIDITY_IN_MILLIS = 1000 * 60 * 60;
    private final String secretKey;

    public JwtService(@Value("${jwt.secret}") String secretKey) {
        this.secretKey = secretKey;
    }

    private SecretKey getSigningKey() {
        var keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String subject) {
        return Jwts.builder()
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + VALIDITY_IN_MILLIS))
                .signWith(getSigningKey())
                .compact();
    }

    public <T> T extractClaim(String token, Function<Claims, T> extractor) {
        var claims = extractAllClaims(token);
        return extractor.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isValidToken(String token, String subject) {
        final String extractedSubject = extractClaim(token, Claims::getSubject);
        final Date expiry = extractClaim(token, Claims::getExpiration);
        final Date issuedAt = extractClaim(token, Claims::getIssuedAt);
        final Date now = new Date();
        return extractedSubject.equals(subject) && expiry.after(now) && issuedAt.before(now);
    }
}
