package com.amine.billardbook.controller;

import com.amine.billardbook.dto.UsersResponseDto;
import com.amine.billardbook.dto.UserDetailsDto;
import com.amine.billardbook.dto.LinkDto;
import com.amine.billardbook.dto.UserNameDto;
import com.amine.billardbook.dto.UserOwnedReservationsDto;
import com.amine.billardbook.dto.UserRegisteredReservationsDto;
import com.amine.billardbook.model.User;
import com.amine.billardbook.service.jwt.UserResourceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;

/**
 * Aiguillage des requêtes liées aux ressources des utilisateurs.
 */
@RestController
@RequestMapping("/users")
public class UserResourceController {

    @Autowired
    private UserResourceService userResourceService;

    @GetMapping(produces = {"application/json", "application/xml"})
    public ResponseEntity<UsersResponseDto> getAllUsers() {
        return ResponseEntity.ok(userResourceService.getAllUsersDto());
    }

    //----------------------------------------------------------------------------------------------------------
    @PostMapping(consumes = {"application/json", "application/xml"})
    public ResponseEntity<Void> createUser(@RequestBody User user) {
        try {
            return ResponseEntity.created(userResourceService.createUser(user)).build();
        } catch (NameAlreadyBoundException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Un utilisateur avec le login " + user.getLogin() + " existe déjà."
            );
        }
    }

    @PostMapping(consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<Void> createUserFromForm(@RequestParam MultiValueMap<String, String> formParams) {
        try {
            // Convertir les paramètres du formulaire en un objet User
            User user = new User(formParams.getFirst("login"), formParams.getFirst("name"));

            return ResponseEntity.created(userResourceService.createUser(user)).build();
        } catch (NameAlreadyBoundException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Un utilisateur avec le login " + formParams.getFirst("login") + " existe déjà."
            );
        }
    }
    //------------------------------------------------------------------------------------------------------------------


    @GetMapping(value = "/{userId}", produces = {"application/json", "application/xml"})
    public ResponseEntity<UserDetailsDto> getUser(@PathVariable String userId) {
        try {
            User user = userResourceService.getUser(userId);
            UserDetailsDto userDetailsDto = new UserDetailsDto(
                    user.getLogin(),
                    user.getName(),
                    userResourceService.getReservationsOwnedByUser(userId).getOwnedReservations().stream()
                            .map(LinkDto::getLink)
                            .toList()
            );
            return ResponseEntity.ok(userDetailsDto);
        } catch (NameNotFoundException e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Utilisateur " + userId + " inconnu."
            );
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    @PutMapping(value = "/{userId}", consumes = {"application/json", "application/xml"})
    public ResponseEntity<String> updateUser(@PathVariable String userId, @RequestBody User user, HttpServletRequest request, HttpServletResponse response) {
        try {
            User existingUser = userResourceService.getUser(userId);
            // ✅ CORRECTION : Toujours mettre à jour l'utilisateur existant
            User updatedUser = new User(
                user.getLogin() != null ? user.getLogin() : existingUser.getLogin(),  // Garde le login existant si non fourni
                user.getName() != null ? user.getName() : existingUser.getName()      // Garde le nom existant si non fourni
            );
            userResourceService.updateUser(userId, updatedUser, request, response);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("User updated");
        } catch (NameNotFoundException e) {
            // Si l'utilisateur n'existe pas, en créer un nouveau
            if (user.getLogin() != null && !user.getLogin().isEmpty()) {
                try {
                    userResourceService.createUser(user);
                    return ResponseEntity.status(HttpStatus.CREATED).body("User created");
                } catch (NameAlreadyBoundException ex) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("User with login " + user.getLogin() + " already exists.");
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request parameters");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request parameters");
        }
    }

    // TODO FIX UPDAT + CREATE
    @PutMapping(value = "/{userId}", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<String> updateUserFromForm(@PathVariable String userId,
                                                     @RequestParam MultiValueMap<String, String> formParams,
                                                     HttpServletRequest request, HttpServletResponse response) {

        User user = new User(formParams.getFirst("login"), formParams.getFirst("name"));

        try {
            User existingUser = userResourceService.getUser(userId);

            if (user.getLogin() == null) {
                // Mise à jour de l'utilisateur existant
                User updatedUser = new User(existingUser.getLogin(), user.getName());
                userResourceService.updateUser(userId, updatedUser, request, response);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Utilisateur modifié");
            } else {
                // Création d'un nouvel utilisateur
                try {
                    userResourceService.createUser(user);
                    return ResponseEntity.status(HttpStatus.CREATED).body("Utilisateur créé");
                } catch (NameAlreadyBoundException ex) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("Un utilisateur avec le login " + user.getLogin() + " existe déjà.");
                }
            }
        } catch (NameNotFoundException e) {
            if (user.getLogin() != null && !user.getLogin().isEmpty()) {
                // Création d'un nouvel utilisateur
                try {
                    userResourceService.createUser(user);
                    return ResponseEntity.status(HttpStatus.CREATED).body("Utilisateur créé");
                } catch (NameAlreadyBoundException ex) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("Un utilisateur avec le login " + user.getLogin() + " existe déjà.");
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Paramètres de la requête non acceptables");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Paramètres de la requête non acceptables");
        }
    }
    //------------------------------------------------------------------------------------------------------------------

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        try {
            userResourceService.deleteUser(userId);
            return ResponseEntity.noContent().build();
        } catch (NameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping(value = "/{userId}/name", produces = {"application/json", "application/xml"})
    public ResponseEntity<UserNameDto> getUserName(@PathVariable String userId) {
        try {
            UserNameDto userNameDto = userResourceService.getUserName(userId);
            return ResponseEntity.ok(userNameDto);
        } catch (NameNotFoundException e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Utilisateur " + userId + " inconnu."
            );
        }
    }

    @GetMapping(value = "/{userId}/ownedReservations", produces = "application/json")
    public ResponseEntity<UserOwnedReservationsDto> getUserOwnedReservations(@PathVariable String userId) {
        try {
            UserOwnedReservationsDto ownedReservationsDto = userResourceService.getReservationsOwnedByUser(userId);
            return ResponseEntity.ok(ownedReservationsDto);
        } catch (NameNotFoundException e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Utilisateur " + userId + " inconnu."
            );
        }
    }

    @GetMapping("/{userId}/ownedReservations/{reservationIndex}/**")
    public ResponseEntity<Void> getUserOwnedReservationsSubProperty(@PathVariable String userId,
                                                                    @PathVariable int reservationIndex,
                                                                    HttpServletRequest request) throws NameNotFoundException {
        try {
            return ResponseEntity
                    .status(HttpStatus.FOUND)
                    .header("Link", "reservations/" + userResourceService.getReservationOwnedByUserSubProperty(userId, reservationIndex, request))
                    .build();
        } catch (IndexOutOfBoundsException e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "L'utilisateur " + userId + " n'a actuellement pas de réservation avec le numéro " + reservationIndex + "."
            );
        } catch (NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping(value = "/{userId}/registeredReservations", produces = "application/json")
    public ResponseEntity<UserRegisteredReservationsDto> getUserRegisteredReservations(@PathVariable String userId) throws NameNotFoundException {
        try {
            UserRegisteredReservationsDto registeredReservationsDto = userResourceService.getReservationsPlayedByUser(userId);
            return ResponseEntity.ok(registeredReservationsDto);
        } catch (NameNotFoundException e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Utilisateur " + userId + " inconnu."
            );
        }
    }

    @GetMapping("/{userId}/registeredReservations/{reservationIndex}/**")
    public ResponseEntity<Void> getUserRegisteredReservationsSubProperty(@PathVariable String userId,
                                                                         @PathVariable int reservationIndex,
                                                                         HttpServletRequest request) {
        try {
            return ResponseEntity
                    .status(HttpStatus.FOUND)
                    .header("Link", "reservations/" + userResourceService.getReservationPlayedByUserSubProperty(userId, reservationIndex, request))
                    .build();
        } catch (IndexOutOfBoundsException e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "L'utilisateur " + userId + " n'a actuellement pas d'inscription à une réservation avec le numéro " + reservationIndex + "."
            );
        } catch (NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}

