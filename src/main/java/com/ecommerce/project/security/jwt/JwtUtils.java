package com.ecommerce.project.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${spring.app.jwtSecret}")
    private String jwtSecret;

    @Value("${spring.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    @Value("${spring.ecom.epp.jwtCookieName}")
    private String jwtCookieName;

    // Helper method to extract JWT from the Authorization header
//    public String getJwtFromHeader(HttpServletRequest request) {
//        String bearerToken = request.getHeader("Authorization");
//        logger.debug("Authorization Header: {}", bearerToken);
//        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
//            return bearerToken.substring(7); // Remove Bearer prefix
//        }
//        return null;
//    }

    public String getJwtFromCookies(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, jwtCookieName);
        if (cookie != null) {
            System.out.println("JWT Cookie: " + cookie.getValue());
            return cookie.getValue();
        } else
            return null;
    }
    // First : eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyMSIsImlhdCI6MTc1NzIwNDY2OSwiZXhwIjoxNzU3MjA3NjY5fQ.7zZXxnfL0hlPvwcXDqELOun-Tc4H6MLPasQEyYo_WNs
    // Second: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyMSIsImlhdCI6MTc1NzIwNDgxMiwiZXhwIjoxNzU3MjA3ODEyfQ.yhbVJeNOfth5BoRWrnmDj5OOZaytFCT4UPzkCon0STE

    public ResponseCookie generateJwtCookie(UserDetails userPrincipal) {
        String jwtToken = generateTokenFromUsername(userPrincipal.getUsername());
        //inbuilt class in spring to create cookies with jwt token wrapped inside
        ResponseCookie cookie = ResponseCookie.from(jwtCookieName, jwtToken)
                .path("/api").maxAge(24 * 60 * 60).httpOnly(false).build();
                // limit cookie to API path only, valid for 1 day, accessible via JavaScript
        return cookie;
    }

    public ResponseCookie getCleanJwtCookie() {

        //inbuilt class in spring to create cookies with jwt token wrapped inside
        ResponseCookie cookie = ResponseCookie.from(jwtCookieName, null)
                .path("/api")
                .build();
        // limit cookie to API path only, valid for 1 day, accessible via JavaScript
        return cookie;
    }

    // Helper method to generate JWT token from UserDetails
    public String generateTokenFromUsername(String username) {
        // String username = userDetails.getUsername();
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key())
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser()
                        .verifyWith((SecretKey) key())
                .build().parseSignedClaims(token)
                .getPayload().getSubject();
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public boolean validateJwtToken(String authToken) {
        try {
            System.out.println("Validate");
            Jwts.parser().verifyWith((SecretKey) key()).build().parseSignedClaims(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}
