package com.amine.billardbook.controller;

import com.amine.billardbook.model.User;
import com.amine.billardbook.service.jwt.UserOperationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.naming.NameNotFoundException;

/**
 * Aiguillage des requêtes liées aux opérations sur les utilisateurs.
 */
@RestController
@RequestMapping("/users")
public class UserOperationController {

    @Autowired
    private UserOperationService userOperationService;

    @PostMapping(value = "/login", consumes = {"application/json", "application/xml"})
    public ResponseEntity<Void> login(@RequestBody User user, HttpServletRequest request, HttpServletResponse response) {
        try {
            userOperationService.login(user, request, response);
            return ResponseEntity.noContent().build();
        } catch (MatchException e) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Erreur de correspondance des noms pour l'utilisateur " + user.getLogin() + "."
            );
        } catch (NameNotFoundException e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Utilisateur " + user.getLogin() + " inconnu."
            );

        } catch (Exception e){
             throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Utilisateur " + user.getLogin() + " inconnu."
            );
        }


    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        userOperationService.logout(request);
        return ResponseEntity.noContent().build();
    }
}
