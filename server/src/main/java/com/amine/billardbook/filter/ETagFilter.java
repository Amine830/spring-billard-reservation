package com.amine.billardbook.filter;

import com.amine.billardbook.dao.UserDao;
import com.amine.billardbook.dao.ReservationDao;
import com.amine.billardbook.model.User;
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
import java.util.List;
import java.util.stream.Collectors;

/**
 * Filter qui ajoute un ETag aux réponses des requêtes GET sur les utilisateurs.
 */
@Component
@Order(4)
@WebFilter
public class ETagFilter extends HttpFilter {

    @Autowired
    private UserDao userDao;

    @Autowired
    private ReservationDao reservationDao;

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String url = request.getRequestURI();
        if (url.matches("/users/\\w+")) {
            handleUserRequest(request, response, chain, url);
        } else {
            chain.doFilter(request, response);
        }
    }

    private void handleUserRequest(HttpServletRequest request, HttpServletResponse response, FilterChain chain, String url)
            throws IOException, ServletException {
        String userId = url.substring(url.lastIndexOf('/') + 1);
        String ifNoneMatch = request.getHeader("If-None-Match");

        try {
            User user = userDao.findOne(userId);
            List<Integer> ownedReservations = reservationDao.findByOwner(userId);
            List<Integer> playedReservations = reservationDao.findByPlayer(userId);

            String eTag = getTag(user, ownedReservations, playedReservations);

            if (ifNoneMatch != null && ifNoneMatch.equals(eTag)) {
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            } else {
                response.setHeader("ETag", eTag);
                chain.doFilter(request, response);
            }
        } catch (Exception e) {
            chain.doFilter(request, response);
        }
    }

    private String getTag(User user, List<Integer> ownedReservations, List<Integer> playedReservations) {
        String data = user.getLogin() + user.getName() +
                ownedReservations.stream().map(Object::toString).collect(Collectors.joining()) +
                playedReservations.stream().map(Object::toString).collect(Collectors.joining());
        return Integer.toHexString(data.hashCode());
    }
}
