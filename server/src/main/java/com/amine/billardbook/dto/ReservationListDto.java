package com.amine.billardbook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO pour la liste des réservations.
 * @param reservations Liste des réservations.
 */
public record ReservationListDto(
   @JsonProperty("reservations")

   List<LinkDto> reservations
) {
}
