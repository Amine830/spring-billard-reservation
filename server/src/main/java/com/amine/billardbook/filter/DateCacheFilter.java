package com.amine.billardbook.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Filter qui cache les réponses des requêtes GET en fonction de la date de dernière modification.
 */
@Component
@Order(3)
@WebFilter

public class DateCacheFilter extends HttpFilter {

    private final Map<String, Date> lastModifiedMap = new HashMap<>();

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String url = request.getRequestURI();
        String method = request.getMethod();

        // Appliquer le cache pour les utilisateurs et les réservations
        if ("GET".equalsIgnoreCase(method) && (url.startsWith("/users/") || url.startsWith("/reservations/"))) {
            handleGetRequest(request, response, chain, url);
        } else if (("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) 
                   && (url.startsWith("/users/") || url.startsWith("/reservations"))) {
            handleModificationRequest(request, response, chain, url);
        } else {
            chain.doFilter(request, response);
        }
    }

    private void handleGetRequest(HttpServletRequest request, HttpServletResponse response, FilterChain chain, String url)
            throws IOException, ServletException {
        long ifModifiedSince = request.getDateHeader("If-Modified-Since");
        Date lastModified = lastModifiedMap.get(url);

        // Si la ressource n'a jamais été modifiée, on l'initialise avec une date ancienne pour forcer le premier chargement
        if (lastModified == null) {
            lastModified = new Date(System.currentTimeMillis() - 86400000); // 24h dans le passé
            lastModifiedMap.put(url, lastModified);
        }

        // Si If-Modified-Since est présent et que la ressource n'a pas été modifiée depuis
        if (ifModifiedSince != -1 && ifModifiedSince >= lastModified.getTime()) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            response.setDateHeader("Last-Modified", lastModified.getTime());
            return;
        }

        // Sinon, on continue avec la requête normale SANS mettre à jour la date (elle ne change que lors des modifications)
        response.setDateHeader("Last-Modified", lastModified.getTime());
        chain.doFilter(request, response);
    }

    private void handleModificationRequest(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                           String url) throws IOException, ServletException {
        chain.doFilter(request, response);
        Date now = new Date();
        lastModifiedMap.put(url, now);
        
        // Si on modifie une sous-ressource (comme /reservations/{id}/comment), 
        // on doit aussi mettre à jour la ressource parent (/reservations/{id})
        if (url.matches("/reservations/[^/]+/.*")) {
            String parentUrl = url.replaceFirst("(/reservations/[^/]+)/.*", "$1");
            lastModifiedMap.put(parentUrl, now);
        } else if (url.matches("/users/[^/]+/.*")) {
            String parentUrl = url.replaceFirst("(/users/[^/]+)/.*", "$1");
            lastModifiedMap.put(parentUrl, now);
        }
    }

}
