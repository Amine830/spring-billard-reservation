package com.amine.billardbook.controller;

import com.amine.billardbook.dto.PlayersResponseDto;
import com.amine.billardbook.dto.ReservationListDto;
import com.amine.billardbook.exception.DeletedReservationException;
import com.amine.billardbook.service.ReservationResourceService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.naming.NameNotFoundException;
import java.util.ArrayList;

/**
 * Controller pour les ressources de réservation.
 */
@RestController
@RequestMapping("/reservations")
public class ReservationResourceController {

    @Autowired
    private ReservationResourceService reservationResourceService;

    @GetMapping(produces = {"application/json"})
    public ResponseEntity<ReservationListDto> listReservations() {
        return ResponseEntity.ok(reservationResourceService.getAllReservationsDto());
    }
    @GetMapping(value = "/{reservationId}/players", produces = "application/json")
    public ResponseEntity<PlayersResponseDto> playersPourLaReservation(@PathVariable String reservationId) {
        try {
            //System.out.println("DEBUG: Demande pour reservationId: " + reservationId);
            
            int reservationIndex = reservationResourceService.findReservationIndexByName(reservationId);
            //System.out.println("DEBUG: Index trouvé: " + reservationIndex);
            
            PlayersResponseDto playersResponse = reservationResourceService.reservationIdPlayers(reservationIndex);
            //System.out.println("DEBUG: PlayersResponse reçu: " + playersResponse);

            if (playersResponse == null) {
                //System.out.println("DEBUG: PlayersResponse est null");
                // Si aucune donnée n'est trouvée, renvoyer un JSON avec liste vide
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new PlayersResponseDto(new ArrayList<>()));
            }

            if (playersResponse.players() == null || playersResponse.players().isEmpty()) {
                //System.out.println("DEBUG: Liste de joueurs de billard est vide");
                // Retourner une liste vide au lieu de 404
                return ResponseEntity.ok(new PlayersResponseDto(new ArrayList<>()));
            }

            //System.out.println("DEBUG: Retour d'une réponse avec " + playersResponse.players().size() + " joueurs de billard");
            return ResponseEntity.ok(playersResponse);
        } catch (NameNotFoundException e) {
            //System.out.println("DEBUG: NameNotFoundException: " + e.getMessage());
            // Retourner un JSON avec une liste vide au lieu d'une réponse sans corps
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new PlayersResponseDto(new ArrayList<>()));

        } catch (DeletedReservationException e) {
            //System.out.println("DEBUG: DeletedReservationException: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.GONE)
                    .body(new PlayersResponseDto(new ArrayList<>()));

        } catch (Exception e) {
            //System.out.println("DEBUG: Exception générale: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new PlayersResponseDto(new ArrayList<>()));

        }
    }

    @GetMapping(value = "/{reservationId}/players/**", produces = {"application/json"})
    public ResponseEntity<Void> sousCollectionDeLaReservation(@PathVariable String reservationId, HttpServletRequest request){
        try {
            String fullPath = request.getRequestURI();
            String basePath = "/" + reservationId + "/players/";
            String dynamicPart = fullPath.substring(fullPath.indexOf(basePath) + basePath.length());

            // Construction du chemin final
            String  playersUrl = "/users/" + dynamicPart + "?reservationId=" + reservationId;
            // Vérifier que la réservation existe
            reservationResourceService.getReservation(
                  reservationResourceService.findReservationIndexByName(reservationId));

            return ResponseEntity.status(HttpStatus.FOUND).header("Link", playersUrl).build();

        } catch (NameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        } catch (DeletedReservationException e){
            return   ResponseEntity.status(HttpStatus.GONE).build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        }
    }

}
