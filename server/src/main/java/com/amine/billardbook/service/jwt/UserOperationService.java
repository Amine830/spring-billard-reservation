package com.amine.billardbook.service.jwt;

import com.amine.billardbook.dao.ReservationDao;
import com.amine.billardbook.model.User;
import com.amine.billardbook.dao.UserDao;
import com.amine.billardbook.connection.ConnectionManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.naming.NameNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Méthodes de service du contrôleur d'opérations sur les utilisateurs.
  */
@Service("jwtUserOperationService")
public class UserOperationService {

    @Autowired
    private ConnectionManager connectionManager;
    @Autowired
    private UserDao userDao;
    @Autowired
    private ReservationDao reservationDao;

    /**
     * Méthode réalisant le login : valide le contenu de la requête et place les informations sur l'utilisateur dans une Map en attribut de requête.
     * @param user L'utilisateur trouvé dans le DAO
     * @param request La requête à laquelle la méthode va rajouter un attribut "properties" dans lequel seront les listes owned et played reservations.
     * @param response La réponse (nécessaire pour le JwtConnectionManager qui y rajoute le header "Authorization" avec le token JWT).
     * @throws NameNotFoundException Si le login de l'utilisateur ne correspond pas à un utilisateur existant
     * @throws MatchException Si la vérification des credentials de l'utilisateur a échoué.
     */
    public void login(User user, HttpServletRequest request, HttpServletResponse response) throws NameNotFoundException, MatchException {
        User userFromDao = userDao.findOne(user.getLogin());
        if (userFromDao == null) {
            throw new NameNotFoundException("Utilisateur non trouvé");
        }

        // levée d'exception si le nom ou le login ne correspondent pas à ceux du DAO
        if(!user.getName().equals(userFromDao.getName()) || !user.getLogin().equals(userFromDao.getLogin())) {
            throw new MatchException("Le nom doit correspondre à celui dans le DAO.", null);
        }

        // Récupération des propriétés de l'utilisateur
        List<Integer> ownedReservations = reservationDao.findByOwner(user.getLogin());
        List<Integer> playedReservations = reservationDao.findByPlayer(user.getLogin());

        // Ajout des réservations possédées et auxquelles il participe pour les retrouver plus facilement
        Map<String, Object> props = new HashMap<>();
        if(!ownedReservations.isEmpty()) {
            props.put("own", ownedReservations);
        }
        if(!playedReservations.isEmpty()) {
            props.put("ply", playedReservations);
        }

        // Passage des propriétés au ConnectionManager par un attribut de requête
        request.setAttribute("props", props);
        connectionManager.connect(request, response, userFromDao);
    }

    /**
     * Méthode mettant à jour les réservations possédées et auxquelles l'utilisateur participe dans le token JWT.
     * @param user L'utilisateur dont les réservations sont à mettre à jour
     * @param request La requête (inutilisée en JWT)
     * @param response La réponse (nécessaire pour le JwtConnectionManager qui y rajoute le header "Authorization" avec le token JWT).
     * @throws NameNotFoundException Si le login de l'utilisateur ne correspond pas à un utilisateur existant
     */
    public void updateUserReservations(User user, HttpServletRequest request, HttpServletResponse response) throws NameNotFoundException {
        User userFromDao = userDao.findOne(user.getLogin());
        List<Integer> ownedReservations = reservationDao.findByOwner(user.getLogin());
        List<Integer> playedReservations = reservationDao.findByPlayer(user.getLogin());

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", userFromDao.getLogin());
        claims.put("own", ownedReservations);
        claims.put("ply", playedReservations);

        connectionManager.updateUser(request, response, claims); //HERE
    }

    /**
     * Méthode appelant la déconnexion dans le <code>ConnectionManager</code> .
     * @param request Inutilisée en JWT
     */
    public void logout(HttpServletRequest request) {
        connectionManager.disconnect(request);
    }
}
