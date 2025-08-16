package com.amine.billardbook.util;

import com.amine.billardbook.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

/**
 * Classe utilitaire qui fournit le token JWT pour l'application Billard-Book, le décode, et le modifie.
 */
@Component
public class BillardBookJwtTokenProvider {
    private final String issuerName = "billard-book-api";
    private final SecretKey key = Jwts.SIG.HS512.key().build();

    @Value("${jwt.expirationMs}")
    private int jwtExpirationMs;

    public BillardBookJwtTokenProvider() {
    }

    /**
     * Génère un token pour un utilisateur "simple".
     * @param user L'utilisateur (login et name seront dans le token).
     * @return Le token généré.
     */
    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(user.getLogin())
                .issuer(issuerName)
                .issuedAt(now)
                .expiration(expiryDate)
                .claim("name", user.getName())
                .signWith(key)
                .compact();
    }

    /**
     * Génère un token avec les propriétés passées en paramètres.
     * @param claims Une <code>Map&lt;String, Object&gt;</code> contenant les propriétés à ajouter comme claims.
     * @return Le token généré.
     */
    public String generateToken(Map<String, Object> claims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
        return Jwts.builder()
                .claims(claims)
                .issuer(issuerName)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * Renvoie l'ensemble des claims contenus dans le token.
     * @param token Le JWT à analyser
     * @return Un ensemble de <code>Claims</code> (hérite de <code>Map&lt;String, Object&gt;</code>)
     */
    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Vérifie que le token est valide par rapport à la clé.
     * @param token Une <code>String</code> contenant un JWT.
     * @return True si le paramètre est bien un token JWT et s'il a pu être correctement vérifié, false sinon.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parse(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
