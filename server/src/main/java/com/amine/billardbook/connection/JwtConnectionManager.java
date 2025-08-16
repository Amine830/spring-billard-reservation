package com.amine.billardbook.connection;

import com.amine.billardbook.model.User;
import com.amine.billardbook.util.BillardBookJwtTokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Gestion de la connexion par token JWT.
 */
@Service
public class JwtConnectionManager implements ConnectionManager {

    @Autowired
    private BillardBookJwtTokenProvider billardBookJwtTokenProvider;

    @Override
    @SuppressWarnings("unchecked")
    public void connect(HttpServletRequest request, HttpServletResponse response, User user) {
        String jwt;
        Map<String, Object> claims = (Map<String, Object>) request.getAttribute("props");
        if(claims == null || claims.isEmpty()) {

            jwt = billardBookJwtTokenProvider.generateToken(user);
        } else {
            claims.put("sub", user.getLogin());
            claims.put("name", user.getName());
            jwt = billardBookJwtTokenProvider.generateToken(claims);

        }
        response.setHeader("Authorization", "Bearer " + jwt);
    }

    /**
     * Ne fait rien.
     * En toute logique, il faudrait se souvenir des utilisateurs déconnectés
     * pour qu'ils ne puissent pas renvoyer un token valide après déconnexion et être authentifiés quand-même.
     * @param request Inutilisée en JWT
     */
    @Override
    public void disconnect(HttpServletRequest request) {
        // The logout logic is often managed client-side, by simply removing the token.
    }

    @Override
    public boolean isConnected(HttpServletRequest request) {
        String jwt = request.getHeader("Authorization");
        if(jwt != null && jwt.startsWith("Bearer ")) {
            return billardBookJwtTokenProvider.validateToken(jwt.substring(7));
        }
        return false;
    }

    @Override
    public String getUser(HttpServletRequest request) {
        String jwt = request.getHeader("Authorization");
        if(jwt != null && jwt.startsWith("Bearer ")) {
            Claims claims = billardBookJwtTokenProvider.getClaimsFromToken(jwt.substring(7));
            return claims.getSubject();
        }
        return null;
    }

    @Override
    public void updateUser(HttpServletRequest request, HttpServletResponse response, Map<String, Object> properties) {
        String jwt = request.getHeader("Authorization");
        if(jwt != null && jwt.startsWith("Bearer ")) {
            Claims claims = billardBookJwtTokenProvider.getClaimsFromToken(jwt.substring(7));
            // On crée une nouvelle map de propriétés, car les claims sont immutables et la map en paramètre peut l'être aussi.
            Map<String, Object> newProps = new HashMap<>(claims);
            properties.forEach(
                    (key, value) -> {
                        if(value != null) {
                            newProps.merge(key, value,
                                    (oldValue, newValue) -> newValue);
                        }
                    }
            );
            jwt = billardBookJwtTokenProvider.generateToken(newProps);
            response.setHeader("Authorization", "Bearer " + jwt);
        }
    }

    @Override
    public Object getUserProperty(HttpServletRequest request, String key) {
        String jwt = request.getHeader("Authorization");
        if(jwt != null && jwt.startsWith("Bearer ")) {
            Claims claims = billardBookJwtTokenProvider.getClaimsFromToken(jwt.substring(7));
            return claims.get(key);
        }
        return null;
    }
}
