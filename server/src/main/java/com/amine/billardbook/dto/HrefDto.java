package com.amine.billardbook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Classe représentant un lien HATEOAS.
 * Utilisé pour les réponses de l'API.
 * @param href Lien
 **/
public record HrefDto(
        @JsonProperty("href") String href) {
}
