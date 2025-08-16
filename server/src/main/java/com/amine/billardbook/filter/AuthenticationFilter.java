package com.amine.billardbook.filter;

import com.amine.billardbook.connection.JwtConnectionManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filtre d'authentification.
 * Vérifie la présence et la validité d'un token JWT dans l'en-tête Authorization.
 * Laisse passer les requêtes valides et celles exemptées de l'authentification (comme /users/login).
 */
@Component
@Order(1)
@WebFilter
public class AuthenticationFilter extends HttpFilter {
    @Autowired
    private JwtConnectionManager connectionManager;

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        // Permet de retrouver la fin de l'URL (après l'URL du contexte) -> indépendant de l'URL de déploiement
        String url = request.getRequestURI().replace(request.getContextPath(), "");

        // Laisse passer les URLs ne nécessitant pas d'authentification et les requêtes par des utilisateurs authentifiés
        if(
                (request.getMethod().equals("OPTIONS")) ||
                        (!url.startsWith("/users/") && !url.startsWith("/reservations")) || // /users/ avec un slash à la fin pour laisser passer /users
                        (url.startsWith("/users/") && request.getMethod().equals("DELETE")) ||
                        (url.equals("/users/login") && request.getMethod().equals("POST")) ||
                        (url.equals("/reservations") && request.getMethod().equals("GET")) ||
                        url.startsWith("/users/hello") ||
                        (!url.equals("/users/login") && connectionManager.isConnected(request))
        ) {
            chain.doFilter(request, response);
            return;
        }

        // Bloque les autres requêtes
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Vous devez vous connecter pour accéder au site.");
    }
}
