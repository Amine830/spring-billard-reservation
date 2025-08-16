//package com.amine.billardbook.connection;
//
//import com.amine.billardbook.model.User;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import jakarta.servlet.http.HttpSession;
//import org.springframework.stereotype.Service;
//
//import java.util.Map;
//
///**
// * Gestion de la connexion par session HTTP classique.
// */
//@Service
//public class HttpSessionConnectionManager implements ConnectionManager {
//
//    @Override
//    public void connect(HttpServletRequest request, HttpServletResponse response, User user) {
//
//        HttpSession session = request.getSession(true); // Crée une session si elle n'existe pas
//        session.setAttribute("login", user.getLogin()); // Ajoute l'attribut "login" à la session
//        session.setAttribute("user", user); // Ajoute l'attribut "user" à la session
//    }
//
//    @Override
//    public void disconnect(HttpServletRequest request) {
//        HttpSession session = request.getSession(false);
//        if(session != null && session.getAttribute("user") != null && session.getAttribute("user") instanceof User) {
//            session.invalidate();
//        }
//    }
//
//    @Override
//    public boolean isConnected(HttpServletRequest request) {
//        // Note :
//        //   le paramètre false dans request.getSession(false) permet de récupérer null si la session n'est pas déjà créée.
//        //   Sinon, l'appel de la méthode getSession() la crée automatiquement.
//        return request.getSession(false) != null;
//    }
//
//    @Override
//    public String getUser(HttpServletRequest request) {
//        HttpSession session = request.getSession(false);
//        return ((User) session.getAttribute("user")).getLogin();
//    }
//
//    @Override
//    public void updateUser(HttpServletRequest request, HttpServletResponse response, Map<String, Object> properties) {
//        HttpSession session = request.getSession(false);
//        for(String key : properties.keySet()) {
//            if(key.equals("name")) {
//                User user = (User) session.getAttribute("user");
//                user.setName((String) properties.get(key));
//                session.setAttribute("user", user);
//            } else if(properties.get(key) != null) {
//                session.setAttribute(key, properties.get(key));
//            } else {
//                session.removeAttribute(key);
//            }
//        }
//    }
//
//    @Override
//    public Object getUserProperty(HttpServletRequest request, String key) {
//        HttpSession session = request.getSession(false);
//        return session.getAttribute(key);
//    }
//}
