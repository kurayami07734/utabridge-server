package dev.ghidora.utabridgeserver.testutils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;

public class JwtTestUtils {

  private static final Key testKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

  public static String generateValidTestToken(Long userId) {
    Date now = new Date();
    Date expiration = new Date(now.getTime() + 3600000); // 1 hour from now

    return Jwts.builder()
        .setSubject(userId.toString())
        .setIssuedAt(now)
        .setExpiration(expiration)
        .signWith(testKey)
        .compact();
  }

  public static String generateExpiredTestToken(Long userId) {
    Date now = new Date();
    Date expiration = new Date(now.getTime() - 1000); // 1 second ago (expired)

    return Jwts.builder()
        .setSubject(userId.toString())
        .setIssuedAt(now)
        .setExpiration(expiration)
        .signWith(testKey)
        .compact();
  }

  public static String generateMalformedToken() {
    return "this.is.not.a.valid.jwt.token";
  }

  public static Key getTestKey() {
    return testKey;
  }
}
