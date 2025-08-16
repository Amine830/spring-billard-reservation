package com.amine.billardbook.dto;


import java.util.List;

/**
 * UserRegisteredReservationsDto .
 */
public class UserRegisteredReservationsDto {
    private List<LinkDto> registeredReservations;

    public UserRegisteredReservationsDto(List<LinkDto> registeredReservations) {
        this.registeredReservations = registeredReservations;
    }

    public List<LinkDto> getRegisteredReservations() {
        return registeredReservations;
    }

    public void setRegisteredReservations(List<LinkDto> registeredReservations) {
        this.registeredReservations = registeredReservations;
    }
}
