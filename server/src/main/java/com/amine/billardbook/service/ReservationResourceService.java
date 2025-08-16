package com.amine.billardbook.service;

import com.amine.billardbook.dto.LinkDto;
import com.amine.billardbook.dto.PlayersResponseDto;
import com.amine.billardbook.dto.ReservationListDto;
import com.amine.billardbook.dao.ReservationDao;
import com.amine.billardbook.exception.DeletedReservationException;
import com.amine.billardbook.model.Reservation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.naming.InvalidNameException;
import javax.naming.NameNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour les opérations sur les ressources de réservation .
 */
@Service
public class ReservationResourceService {

    @Autowired
    private ReservationDao reservationDao;

    public ReservationListDto getAllReservationsDto() {
        return new ReservationListDto(
                reservationDao.findAll()
                        .stream()
                        .filter(reservation -> reservation != null)  // Filtrer les réservations null
                        .map(reservation -> new LinkDto("reservations/" + reservation.getId()))
                        .toList()
        );
    }

    public PlayersResponseDto reservationIdPlayers(int id) throws InvalidNameException, NameNotFoundException {
        List<String> ply = reservationDao.findOne(id).getPlayers();
        List<LinkDto> l = new ArrayList<>();
        for(String e:ply){
            l.add(new LinkDto(e));
        }
        return  new PlayersResponseDto(l);
    }

    public Reservation getReservation(int i) throws InvalidNameException, NameNotFoundException {
        return reservationDao.findOne(i);
    }
    public int findReservationIndexByName(String name) throws NameNotFoundException, DeletedReservationException {
        // Récupérer toutes les réservations (exemple ici : reservationDao.getAllReservations())
        // Méthode fictive pour obtenir la liste des réservations
        List<Reservation> reservations = (List<Reservation>) reservationDao.findAll();

        // Itérer sur la liste pour trouver la réservation par son nom
        for (int i = 0; i < reservations.size(); i++) {
            Reservation reservation = reservations.get(i);

            // Si l'élément est null, c'est qu'il a été supprimé
            if (reservation == null) {
                continue; // Passer au suivant
            }
            
            // Vérifier si c'est la réservation recherchée
            if (reservation.getId().equals(name)) {
                // Retourner l'index de la réservation dans la liste
                return i;  // Retourne l'index de la réservation
            }
        }

        // Maintenant, vérifier si l'ID pourrait correspondre à un élément supprimé
        // On vérifie s'il y a des éléments null dans la liste qui pourraient avoir eu cet ID
        // Pour cela, on utilise une heuristique : si on trouve des éléments null, 
        // il est possible que l'un d'eux ait eu cet ID
        boolean hasDeletedElements = reservations.contains(null);
        
        if (hasDeletedElements) {
            // Il est possible que l'ID demandé soit celui d'un élément supprimé
            // Mais on ne peut pas être sûr, donc on considère que c'est supprimé si l'ID a un format valide
            // (ici on assume qu'un ID valide fait 8 caractères comme générés par UUID)
            if (name != null && name.length() == 8) {
                throw new DeletedReservationException("Reservation with id " + name + " has been deleted.");
            }
        }

        // Si la réservation avec ce nom n'est pas trouvée, lancer une exception
        throw new NameNotFoundException("Reservation with name " + name + " not found.");
    }
}
