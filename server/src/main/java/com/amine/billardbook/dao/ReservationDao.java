package com.amine.billardbook.dao;

import com.amine.billardbook.model.Reservation;
import com.amine.billardbook.exception.DeletedReservationException;

import javax.naming.InvalidNameException;
import javax.naming.NameNotFoundException;
import java.io.Serializable;
import java.util.List;

/**
 * DAO responsable de la gestion des réservations.
 */
public class ReservationDao extends AbstractListDao<Reservation> {

    /**
     * Rajoute à la méthode l'envoi d'une exception pour les réservations précédemment supprimées.
     * @param id La clé de l'élément cherché
     * @return une instance non nulle de la classe <code>Reservation</code>
     * @throws InvalidNameException <i>cf.</i> doc superclasse
     * @throws NameNotFoundException <i>cf.</i> doc superclasse
     * @throws DeletedReservationException <i>cf.</i> doc superclasse
     */
    @Override
    public Reservation findOne(Serializable id) throws InvalidNameException, NameNotFoundException, DeletedReservationException {
        Reservation r = super.findOne(id);
        if(r == null) {
            throw new DeletedReservationException(id.toString());
        }
        return r;
    }

    public List<Integer> findByOwner(String ownerId) {
        List<Reservation> allReservations = (List<Reservation>) this.findAll();
        List<Integer> indices = new java.util.ArrayList<>();
        
        for (int i = 0; i < allReservations.size(); i++) {
            Reservation reservation = allReservations.get(i);
            if (reservation != null && reservation.getOwnerId().equals(ownerId)) {
                indices.add(i);
            }
        }
        
        return indices;
    }

    public List<Integer> findByPlayer(String playerId) {
        List<Reservation> allReservations = (List<Reservation>) this.findAll();
        List<Integer> indices = new java.util.ArrayList<>();
        
        for (int i = 0; i < allReservations.size(); i++) {
            Reservation reservation = allReservations.get(i);
            if (reservation != null && reservation.hasPlayer(playerId)) {
                indices.add(i);
            }
        }
        
        return indices;
    }
}
