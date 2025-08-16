package com.amine.billardbook.dto;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.amine.billardbook.model.Comment;
import com.amine.billardbook.model.Reservation;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * DTO permettant de sérialiser une réservation de table de billard (voir doc OpenAPI).
 * @param tableId La table de billard
 * @param ownerId Le propriétaire de la réservation
 * @param startTime La date et l'heure de début
 * @param endTime La date et l'heure de fin
 * @param players Les joueurs inscrits
 * @param comments Les commentaires sur la partie
 */


public record ReservationResponseDto(
        String tableId,
        String ownerId,
        @JsonFormat(
                shape = JsonFormat.Shape.STRING,
                pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        ZonedDateTime startTime,
        @JsonFormat(
                shape = JsonFormat.Shape.STRING,
                pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        ZonedDateTime endTime,
        @JacksonXmlElementWrapper(localName = "players")
        List<LinkDto> players,
        @JacksonXmlElementWrapper(localName = "comments")
        List<Comment> comments
) {
    public static ReservationResponseDto of(Reservation reservation) {
        return new ReservationResponseDto(
                reservation.getTableId(),
                reservation.getOwnerId(),
                ZonedDateTime.of(reservation.getStartTime(), ZoneId.systemDefault()),
                ZonedDateTime.of(reservation.getEndTime(), ZoneId.systemDefault()),
                reservation.getPlayers().stream()
                        .map(player -> new LinkDto("users/" + player))
                        .toList(),
                reservation.getComments()
        );
    }
}

