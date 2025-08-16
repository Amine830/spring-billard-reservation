package com.amine.billardbook.dto;


import java.util.List;

/**
 * UserOwnedReservationsDto.
 */
public class UserOwnedReservationsDto {
    private List<LinkDto> ownedReservations;

    public UserOwnedReservationsDto(List<LinkDto> ownedReservations) {
        this.ownedReservations = ownedReservations;
    }

    public List<LinkDto> getOwnedReservations() {
        return ownedReservations;
    }

    public void setOwnedReservations(List<LinkDto> ownedReservations) {
        this.ownedReservations = ownedReservations;
    }
}
