//package com.amine.billardbook.service;
//
//import com.amine.billardbook.dto.UserDetailsDto;
//import com.amine.billardbook.model.User;
//import com.amine.billardbook.dao.ReservationDao;
//import com.amine.billardbook.dao.UserDao;
//import com.amine.billardbook.dto.UsersResponseDto;
//import com.amine.billardbook.dto.LinkDto;
//import com.amine.billardbook.util.UrlDecomposer;
//import jakarta.servlet.http.HttpServletRequest;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import javax.naming.NameAlreadyBoundException;
//import javax.naming.NameNotFoundException;
//import java.net.URI;
//import java.util.List;
//import java.util.stream.Collectors;
//
///**
// * Méthodes de service du contrôleur de ressources sur les utilisateurs.
//  */
//@Service
//public class UserResourceService {
//
//    @Autowired
//    // DAO pour les utilisateurs
//    private UserDao userDao;
//
//    @Autowired
//    // DAO pour les réservations
//    private ReservationDao reservationDao;
//
//    // Récupère tous les utilisateurs
//    public List<User> getAllUsers() {
//        return userDao.findAll().stream().toList();
//    }
//
//    // Récupère tous les utilisateurs sous forme de DTO
//    public UsersResponseDto getAllUsersDto() {
//        return new UsersResponseDto(userDao
//                .findAll().stream()
//                .map(User::getLogin)
//                .map(s -> "users/" + s)
//                .map(LinkDto::new)
//                .toList()
//        );
//    }
//
//    // Crée un utilisateur
//    public URI createUser(User user) throws NameAlreadyBoundException {
//        userDao.add(user);
//        return URI.create("users/" + user.getLogin());
//    }
//
//    public List<Integer> getReservationsOwnedByUser(String login) throws NameNotFoundException {
//        User user = userDao.findOne(login);
//        if (user == null) {
//            throw new NameNotFoundException("Utilisateur non trouvé");
//        }
//        return reservationDao.findByOwner(login);
//    }
//
//    public UserDetailsDto getUser(String login) throws NameNotFoundException {
//        //TODO récupérer les infos manquantes dans la session
//        User user = userDao.findOne(login);
//        List<String> ownedReservations = reservationDao.findByOwner(login)
//                .stream()
//                .map(id -> "reservations/" + id)
//                .collect(Collectors.toList());
//        return new UserDetailsDto(user.getLogin(), user.getName(), ownedReservations);
//    }
//
//    public void updateUser(String login, User user) throws NameNotFoundException {
//        User existingUser = userDao.findOne(login);
//        if (existingUser == null) {
//            throw new NameNotFoundException("Utilisateur non trouvé");
//        }
//        existingUser.setName(user.getName());
//        userDao.update(login, existingUser);
//    }
//
//    public void deleteUser(String login) throws NameNotFoundException {
//        userDao.deleteById(login);
//    }
//
//    public String getUserName(String login) throws NameNotFoundException {
//        return userDao.findOne(login).getName();
//    }
//
//    public String getReservationOwnedByUserSubProperty(String userId, int reservationIndex, HttpServletRequest
//    request) throws IndexOutOfBoundsException, NameNotFoundException {
//        List<Integer> reservations = this.getReservationsOwnedByUser(userId);
//        String urlEnd = UrlDecomposer.getUrlEnd(request, 4);
//        return reservations.get(reservationIndex) + urlEnd;
//    }
//
//    public List<Integer> getReservationsPlayedByUser(String login) throws NameNotFoundException {
//        User user = userDao.findOne(login);
//        if (user == null) {
//            throw new NameNotFoundException("Utilisateur non trouvé");
//        }
//        return reservationDao.findByPlayer(login);
//    }
//
//    public String getReservationPlayedByUserSubProperty(String userId, int reservationIndex, HttpServletRequest
//    request) throws IndexOutOfBoundsException, NameNotFoundException {
//        List<Integer> reservations = this.getReservationsPlayedByUser(userId);
//        String urlEnd = UrlDecomposer.getUrlEnd(request, 4);
//        return reservations.get(reservationIndex) + urlEnd;
//    }
//}
