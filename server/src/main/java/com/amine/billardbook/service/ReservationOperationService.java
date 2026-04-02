package com.amine.billardbook.service;

import com.amine.billardbook.connection.ConnectionManager;
import com.amine.billardbook.dto.CommentRequestDto;
import com.amine.billardbook.dto.ReservationRequestDto;
import com.amine.billardbook.exception.DeletedReservationException;
import com.amine.billardbook.model.Comment;
import com.amine.billardbook.model.Reservation;
import com.amine.billardbook.dao.ReservationDao;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.naming.InvalidNameException;
import javax.naming.NameNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Méthodes de service du contrôleur d'opérations métier sur les réservations.
 */
@Service
public class ReservationOperationService {

    @Autowired
    private ConnectionManager connectionManager;
    @Autowired
    private ReservationDao reservationDao;

    public String createReservation(ReservationRequestDto reservationRequestDto, HttpServletRequest request, HttpServletResponse response) {
        String creatorId = connectionManager.getUser(request);
        Reservation reservation = new Reservation(
                reservationRequestDto.getTableId(),
                creatorId,
                reservationRequestDto.getStartTime(),
                reservationRequestDto.getEndTime()
        );
        reservationDao.add(reservation);
        updateClaim(request, response, creatorId);


        return reservation.getId(); // Retourne l'ID de la nouvelle réservation
    }

    /**
     * .
     *
     * @param reservationRequestDto
     * @param reservationId
     * @param request
     * @param response
     * @throws NameNotFoundException
     * @throws InvalidNameException
     */

    public String updateReservation(ReservationRequestDto reservationRequestDto, String reservationId,
                                  HttpServletRequest request, HttpServletResponse response)
            throws NameNotFoundException, InvalidNameException {

        int reservationIndex = findReservationIndexByName(reservationId);
        Reservation reservation = reservationDao.findOne(reservationIndex);
        if (reservation == null) {
            throw new NameNotFoundException("Reservation not found");
        }
        String creatorId = connectionManager.getUser(request);
        
        // Mise à jour simple - l'ID reste stable maintenant
        reservation.setTableId(reservationRequestDto.getTableId());
        reservation.setStartTime(reservationRequestDto.getStartTime());
        reservation.setEndTime(reservationRequestDto.getEndTime());
        reservationDao.update(reservationIndex, reservation);
        updateClaim(request, response, creatorId);
        return reservation.getId(); // L'ID ne change jamais
    }

    /**
     * .
     * @param reservationId
     * @return
     * @throws InvalidNameException
     * @throws NameNotFoundException
     */

    public Reservation getReservation(int reservationId) throws InvalidNameException, NameNotFoundException {
        Reservation reservation = reservationDao.findOne(reservationId);
        if (reservation == null) {
            throw new NameNotFoundException("Reservation not found");

        }
        return reservation;

    }

    /**
     * .
     * @param reservationId
     * @param request
     * @param response
     * @throws NameNotFoundException
     * @throws InvalidNameException
     */

    public void deleteReservation(int reservationId, HttpServletRequest request, HttpServletResponse response) throws
            NameNotFoundException, InvalidNameException {

        if (reservationDao.findOne(reservationId) == null) {
            throw new NameNotFoundException("Reservation not found");
        }
        reservationDao.deleteById(reservationId);
        String creatorId = connectionManager.getUser(request);
        updateClaim(request, response, creatorId);

    }

    /**
     *.
     * @param id
     * @param request
     * @param response
     * @throws InvalidNameException
     * @throws NameNotFoundException
     */

    public void registre(int id, HttpServletRequest request, HttpServletResponse response) throws InvalidNameException, NameNotFoundException {
        String creatorId = connectionManager.getUser(request);
        Reservation reservation = reservationDao.findOne(id);
        if (reservation.isCompleted() || reservation.hasPlayer(creatorId)) {
            throw new InvalidNameException();
        } else {
            reservation.addPlayer(creatorId);
            updateClaim(request, response, creatorId);
        }

    }

    /**
     * .
     * @param request
     * @param response
     * @param creatorId
     */

    private void updateClaim(HttpServletRequest request, HttpServletResponse response, String creatorId) {
        List<Integer> ownedReservations = reservationDao.findByOwner(creatorId);
        List<Integer> playedReservations = reservationDao.findByPlayer(creatorId);

        //System.out.println("DEBUG: Updating claims for user " + creatorId);
        //System.out.println("DEBUG: Owned reservations indexes: " + ownedReservations);
        //System.out.println("DEBUG: Played reservations indexes: " + playedReservations);

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", creatorId);
        claims.put("own", ownedReservations);
        claims.put("ply", playedReservations);
        connectionManager.updateUser(request, response, claims);
    }

    /**
     * .
     * @param id
     * @param request
     * @param response
     * @throws InvalidNameException
     * @throws NameNotFoundException
     */

    public void unRegistre(int id, HttpServletRequest request, HttpServletResponse response) throws InvalidNameException, NameNotFoundException {
        String creatorId = connectionManager.getUser(request);
        Reservation reservation = reservationDao.findOne(id);
        if (!reservation.hasPlayer(creatorId)) {
            throw new InvalidNameException();
        } else {
            reservation.removePlayer(creatorId);
            updateClaim(request, response, creatorId);
        }


    }

    /**
     * .
     * @param id
     * @param commentRequestDto
     * @param request
     * @param response
     * @throws InvalidNameException
     * @throws NameNotFoundException
     */

    public void commentById(String id, CommentRequestDto commentRequestDto, HttpServletRequest request, HttpServletResponse response)
            throws InvalidNameException, NameNotFoundException {

        int reservationIndex = findReservationIndexByName(id);
        Reservation reservation = reservationDao.findOne(reservationIndex);
        
        String creatorId = connectionManager.getUser(request);
        Comment comment = new Comment(creatorId, commentRequestDto.getCommentText());
        reservation.addComment(comment);
        updateClaim(request, response, creatorId);
    }

    /**
     * .
     * @param name
     * @return
     * @throws NameNotFoundException
     */

    public Integer findReservationIndexByName(String name) throws NameNotFoundException, DeletedReservationException {
        // Récupérer toutes les réservations
        List<Reservation> reservations = (List<Reservation>) reservationDao.findAll();

        //System.out.println("DEBUG: Searching for reservation: " + name);
        // Debug logging intentionally removed.

        // Itérer sur la liste pour trouver la réservation par son ID
        for (int i = 0; i < reservations.size(); i++) {
            Reservation reservation = reservations.get(i);

            // Si l'élément est null, c'est qu'il a été supprimé
            if (reservation == null) {
                continue; // Passer au suivant
            }

            // Vérifier si c'est la réservation recherchée
            if (reservation.getId().equals(name)) {
                //System.out.println("DEBUG: Found reservation " + name + " at index " + i);
                return i;
            }
        }

        // Maintenant, vérifier si l'ID pourrait correspondre à un élément supprimé
        // On vérifie s'il y a des éléments null dans la liste qui pourraient avoir eu cet ID
        boolean hasDeletedElements = reservations.contains(null);
        
        if (hasDeletedElements) {
            // Il est possible que l'ID demandé soit celui d'un élément supprimé
            // Mais on ne peut pas être sûr, donc on considère que c'est supprimé si l'ID a un format valide
            // (ici on assume qu'un ID valide fait 8 caractères comme générés par UUID)
            if (name != null && name.length() == 8) {
                throw new DeletedReservationException("Reservation with id " + name + " has been deleted.");
            }
        }

        //System.out.println("DEBUG: Reservation " + name + " not found!");
        throw new NameNotFoundException("Reservation with name " + name + " not found.");

    }
}
