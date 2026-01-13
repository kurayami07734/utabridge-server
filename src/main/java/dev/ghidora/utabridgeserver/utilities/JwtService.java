package dev.ghidora.utabridgeserver.utilities;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Service for handling JWT operations. */
@Service
public class JwtService {
  private final long validityDurationMs;
  private final String secretKey;

  public JwtService(
      @Value("${jwt.secret}") String secretKey,
      @Value("${jwt.validity-duration-ms}") long validityDurationMs) {
    this.secretKey = secretKey;
    this.validityDurationMs = validityDurationMs;
  }

  private SecretKey getSigningKey() {
    var keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  /**
   * Generates a token for the given subject.
   *
   * @param subject The subject of the token.
   * @return The generated token.
   */
  public String generateToken(String subject) {
    return Jwts.builder()
        .subject(subject)
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + validityDurationMs))
        .signWith(getSigningKey())
        .compact();
  }

  public <T> T extractClaim(String token, Function<Claims, T> extractor) {
    var claims = extractAllClaims(token);
    return extractor.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
  }

  /**
   * Validates if the token matches the subject and is not expired.
   *
   * @param token The token to validate.
   * @param subject The expected subject.
   * @return True if valid, false otherwise.
   */
  public boolean isValidToken(String token, String subject) {
    final String extractedSubject = extractClaim(token, Claims::getSubject);
    final Date expiry = extractClaim(token, Claims::getExpiration);
    final Date issuedAt = extractClaim(token, Claims::getIssuedAt);
    final Date now = new Date();
    return extractedSubject.equals(subject) && expiry.after(now) && issuedAt.before(now);
  }
}
