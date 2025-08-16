package com.amine.billardbook.dto;


import java.util.List;

/**
 * UserResponseDto.
 */
public class UserResponseDto {
    private String login;
    private String name;
    private List<LinkDto> ownedReservations;
    private List<LinkDto> playedReservations;

    public UserResponseDto(String login, String name, List<LinkDto> ownedReservations, List<LinkDto> playedReservations) {
        this.login = login;
        this.name = name;
        this.ownedReservations = ownedReservations;
        this.playedReservations = playedReservations;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<LinkDto> getOwnedReservations() {
        return ownedReservations;
    }

    public void setOwnedReservations(List<LinkDto> ownedReservations) {
        this.ownedReservations = ownedReservations;
    }

    public List<LinkDto> getPlayedReservations() {
        return playedReservations;
    }

    public void setPlayedReservations(List<LinkDto> playedReservations) {
        this.playedReservations = playedReservations;
    }
}
