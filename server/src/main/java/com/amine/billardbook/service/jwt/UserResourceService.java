package com.amine.billardbook.service.jwt;

import com.amine.billardbook.connection.ConnectionManager;
import com.amine.billardbook.dto.UserNameDto;
import com.amine.billardbook.dto.UserOwnedReservationsDto;
import com.amine.billardbook.dto.UserRegisteredReservationsDto;
import com.amine.billardbook.dto.UserResponseDto;
import com.amine.billardbook.dto.UsersResponseDto;
import com.amine.billardbook.dto.LinkDto;
import com.amine.billardbook.model.User;
import com.amine.billardbook.model.Reservation;
import com.amine.billardbook.dao.ReservationDao;
import com.amine.billardbook.dao.UserDao;
import com.amine.billardbook.util.UrlDecomposer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Méthodes de service du contrôleur de ressources sur les utilisateurs.
  */
@Service("jwtUserResourceService")
public class UserResourceService {

    @Autowired
    private UserDao userDao;
    @Autowired
    private ReservationDao reservationDao;
    @Autowired
    private ConnectionManager connectionManager;

    public List<User> getAllUsers() {
        return userDao.findAll().stream().toList();
    }

    public UsersResponseDto getAllUsersDto() {
        return new UsersResponseDto(userDao
                .findAll().stream()
                .map(User::getLogin)
                .map(LinkDto::new)
                .toList()
        );
    }

    public URI createUser(User user) throws NameAlreadyBoundException {
        userDao.add(user);
        return URI.create("users/" + user.getLogin());
    }

    public User getUser(String login) throws NameNotFoundException {
        return userDao.findOne(login);
    }

    public UserResponseDto getUserDto(String login) throws NameNotFoundException {
        User user = userDao.findOne(login);
        
        // Correction pour les réservations possédées
        List<Integer> ownedIndices = reservationDao.findByOwner(login);
        List<LinkDto> ownedReservations = new ArrayList<>();
        for (Integer index : ownedIndices) {
            try {
                Reservation reservation = reservationDao.findOne(index);
                if (reservation != null) {
                    ownedReservations.add(new LinkDto("reservations/" + reservation.getId()));
                }
            } catch (Exception e) {
                // Ignorer les réservations supprimées ou inaccessibles
            }
        }
        
        // Correction pour les réservations auxquelles l'utilisateur participe
        List<Integer> playedIndices = reservationDao.findByPlayer(login);
        List<LinkDto> playedReservations = new ArrayList<>();
        for (Integer index : playedIndices) {
            try {
                Reservation reservation = reservationDao.findOne(index);
                if (reservation != null) {
                    playedReservations.add(new LinkDto("reservations/" + reservation.getId()));
                }
            } catch (Exception e) {
                // Ignorer les réservations supprimées ou inaccessibles
            }
        }
        
        return new UserResponseDto(user.getLogin(), user.getName(), ownedReservations, playedReservations);
    }

    public void updateUser(String login, User user, HttpServletRequest request, HttpServletResponse response) {
        userDao.update(login, user);

        Map<String, Object> props = new HashMap<>();
        props.put("sub", user.getLogin());  // ✅ Ajouter le login (subject)
        props.put("name", user.getName());  // ✅ Mettre à jour le name
        connectionManager.updateUser(request, response, props); //HERE
    }

    public void deleteUser(String login) throws NameNotFoundException {
        userDao.deleteById(login);
    }

    public UserNameDto getUserName(String login) throws NameNotFoundException {
        return new UserNameDto(userDao.findOne(login).getName());
    }

    public UserOwnedReservationsDto getReservationsOwnedByUser(String userId) throws NameNotFoundException  {
        List<Integer> reservationIndices = reservationDao.findByOwner(userId);
        List<String> reservationIds = new ArrayList<>();
        
        for (Integer index : reservationIndices) {
            try {
                Reservation reservation = reservationDao.findOne(index);
                if (reservation != null) {
                    reservationIds.add(reservation.getId());
                }
            } catch (Exception e) {
                // Ignorer les réservations supprimées ou inaccessibles
            }
        }
        
        return new UserOwnedReservationsDto(reservationIds.stream()
                .map(id -> "reservations/" + id)
                .map(LinkDto::new)
                .toList()
        );
    }

    public String getReservationOwnedByUserSubProperty(String userId, int reservationIndex, HttpServletRequest request)
            throws IndexOutOfBoundsException, NameNotFoundException {
        List<Integer> reservationIndices = reservationDao.findByOwner(userId);
        if (reservationIndices.isEmpty()) {
            throw new NameNotFoundException("Utilisateur " + userId + " inconnu.");
        }
        
        // Obtenir l'ID réel de la réservation au lieu de l'indice
        Integer actualIndex = reservationIndices.get(reservationIndex);
        try {
            Reservation reservation = reservationDao.findOne(actualIndex);
            if (reservation == null) {
                throw new NameNotFoundException("Réservation non trouvée.");
            }
            String urlEnd = UrlDecomposer.getUrlEnd(request, 4);
            return reservation.getId() + urlEnd;  // Sans "reservations/" car le contrôleur l'ajoute
        } catch (Exception e) {
            throw new NameNotFoundException("Réservation non accessible.");
        }
    }

    public UserRegisteredReservationsDto getReservationsPlayedByUser(String userId) throws NameNotFoundException {
        List<Integer> reservationIndices = reservationDao.findByPlayer(userId);
        List<String> reservationIds = new ArrayList<>();
        
        for (Integer index : reservationIndices) {
            try {
                Reservation reservation = reservationDao.findOne(index);
                if (reservation != null) {
                    reservationIds.add(reservation.getId());
                }
            } catch (Exception e) {
                // Ignorer les réservations supprimées ou inaccessibles
            }
        }
        
        return new UserRegisteredReservationsDto(reservationIds.stream()
                .map(id -> new LinkDto("reservations/" + id))
                .toList()
        );
    }
    public String getReservationPlayedByUserSubProperty(String userId, int reservationIndex, HttpServletRequest request)
            throws IndexOutOfBoundsException, NameNotFoundException {
        List<Integer> reservationIndices = reservationDao.findByPlayer(userId);
        if (reservationIndices.isEmpty()) {
            throw new NameNotFoundException("Utilisateur " + userId + " inconnu.");
        }
        
        // Obtenir l'ID réel de la réservation au lieu de l'indice
        Integer actualIndex = reservationIndices.get(reservationIndex);
        try {
            Reservation reservation = reservationDao.findOne(actualIndex);
            if (reservation == null) {
                throw new NameNotFoundException("Réservation non trouvée.");
            }
            String urlEnd = UrlDecomposer.getUrlEnd(request, 4);
            return reservation.getId() + urlEnd;
        } catch (Exception e) {
            throw new NameNotFoundException("Réservation non accessible.");
        }
    }

}
