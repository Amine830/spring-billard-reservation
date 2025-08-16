package com.amine.billardbook.dto;

import java.util.List; /**
 * DTO représentant la liste des joueurs d'une réservation de billard.
 * @param players Liste des joueurs sous forme de chaînes (ex: "users/toto")
 */
public record PlayersResponseDto(List<LinkDto> players) {

    /**
     * Méthode statique pour créer un PlayersResponseDto à partir d'une liste de joueurs de billard.
     * @param players Liste des joueurs à inclure dans le DTO
     * @return Un objet PlayersResponseDto
     */
    public static PlayersResponseDto of(List<String> players) {
        List<LinkDto> prefixedPlayers = players.stream()
                .map(playerId -> new LinkDto("users/" + playerId))
                .toList();
        return new PlayersResponseDto(prefixedPlayers);
    }
}
