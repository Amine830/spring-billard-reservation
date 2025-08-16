package com.amine.billardbook.exception;

/**
 * À utiliser lors d'une requête d'inscription d'un utilisateur à une réservation à laquelle il est déjà inscrit.
 */
public class PlayerAlreadyRegisteredException extends RuntimeException {
    public PlayerAlreadyRegisteredException(String message) {
        super(message);
    }
}
