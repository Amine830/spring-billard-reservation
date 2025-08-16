package com.amine.billardbook.exception;

/**
 * À utiliser lors d'une requête d'inscription d'un utilisateur à une réservation qui est déjà complète.
 */
public class CompleteReservationException extends RuntimeException {
    public CompleteReservationException(String message) {
        super(message);
    }
}
