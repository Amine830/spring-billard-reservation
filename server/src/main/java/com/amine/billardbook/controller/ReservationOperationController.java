package com.amine.billardbook.controller;

import com.amine.billardbook.dto.CommentRequestDto;
import com.amine.billardbook.dto.ReservationRequestDto;
import com.amine.billardbook.dto.ReservationResponseDto;
import com.amine.billardbook.exception.DeletedReservationException;
import com.amine.billardbook.model.Reservation;
import com.amine.billardbook.service.ReservationOperationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.naming.InvalidNameException;
import javax.naming.NameNotFoundException;

/**
 * Controller pour les opérations sur les réservations .
 */
@RestController
@RequestMapping("/reservations")
public class ReservationOperationController {

    @Autowired
    private ReservationOperationService reservationOperationService;

    @PostMapping(consumes = {"application/json", "application/xml"})
    public ResponseEntity<String> createReservation(@RequestBody ReservationRequestDto reservationRequestDto,
                                                    HttpServletRequest request, HttpServletResponse response) {
        try {

            String reservationId = reservationOperationService.createReservation(reservationRequestDto, request, response);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .header("Location", "/reservations/" + reservationId)
                    .build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping(value = "/{reservationId}", consumes = {"application/json", "application/xml"})
    public ResponseEntity<String> updateReservation(@PathVariable String reservationId, @RequestBody
        ReservationRequestDto reservationRequestDto, HttpServletRequest request, HttpServletResponse response) {
        try {
            String newReservationId = reservationOperationService.updateReservation(reservationRequestDto, reservationId, request, response);
            
            // Si l'ID a changé (tableId différent), inclure le nouvel ID dans la réponse
            if (!reservationId.equals(newReservationId)) {
                return ResponseEntity.noContent()
                        .header("Location", "/reservations/" + newReservationId)
                        .build();
            } else {
                return ResponseEntity.noContent().build();
            }
        } catch (NameNotFoundException | NumberFormatException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping (value = "/{reservationId}", produces = {"application/json", "application/xml"})
    public ResponseEntity<ReservationResponseDto> getReservationById(@PathVariable String reservationId) {
        try {
          Reservation reservation = reservationOperationService.getReservation(
                  reservationOperationService.findReservationIndexByName(reservationId));
            ReservationResponseDto responseDto = ReservationResponseDto.of(reservation);
            return ResponseEntity.ok(responseDto);
        } catch (NameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (DeletedReservationException e) {
            return ResponseEntity.status(HttpStatus.GONE).build();
        } catch (Exception e) {
            // Log l'erreur pour debug
            // System.err.println("Erreur dans getReservationById: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @DeleteMapping(value = "/{reservationId}", produces = "application/json")
    public ResponseEntity<String> deleteReservation(@PathVariable String reservationId, HttpServletRequest request,
                                                    HttpServletResponse response) {
        try {
            reservationOperationService.deleteReservation(reservationOperationService.findReservationIndexByName(reservationId), request, response);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("\t\n" +
                    "\n" +
                    "Réservation correctement suppprimée");
        } catch (DeletedReservationException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("\t\n" +
                    "\n" +
                    "La réservation a existé mais a été supprimée");
        } catch (NameNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Réservation non trouvée");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping(value = "/{reservationId}/register", consumes = "application/json")
    public ResponseEntity<Void> registerByResaId(@PathVariable String reservationId, HttpServletRequest request, HttpServletResponse response) {
        try {

             reservationOperationService.registre(reservationOperationService.findReservationIndexByName(reservationId), request, response);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (NameNotFoundException e){
            return  ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (InvalidNameException e){
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
        } catch (DeletedReservationException e){
            return ResponseEntity.status(HttpStatus.GONE).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping(value = "/{reservationId}/unregister")
    public ResponseEntity<Void> unregisterByResaId(@PathVariable String reservationId, HttpServletRequest request,
                                                   HttpServletResponse response) {
        try {

            reservationOperationService.unRegistre(reservationOperationService.findReservationIndexByName(
                    reservationId), request, response);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

        } catch (NameNotFoundException e){
            return  ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        } catch (InvalidNameException e){
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();

        } catch (DeletedReservationException e){
            return ResponseEntity.status(HttpStatus.GONE).build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        }
    }

    @PostMapping(value = "/{reservationId}/comment", consumes = {"application/json", "application/xml"})
    public ResponseEntity<Void> commentByResaId(@PathVariable String reservationId, @RequestBody CommentRequestDto
            commentRequestDto, HttpServletRequest request, HttpServletResponse response) {
        try {
            reservationOperationService.commentById(reservationId, commentRequestDto, request, response);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (NameNotFoundException | InvalidNameException e){
            return  ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (DeletedReservationException e){
            return ResponseEntity.status(HttpStatus.GONE).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
