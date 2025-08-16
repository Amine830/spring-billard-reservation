package com.amine.billardbook.model;

import jakarta.annotation.Nonnull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Réservations de tables de billard créées par les utilisateurs.
 * Une réservation comporte :<br>
 * - une date de début et une date de fin<br>
 * - une liste d'IDs de joueurs (max 4)<br>
 * - une liste de commentaires sur la partie<br>
 * - un booléen (partie complète / en recherche de joueurs)<br>
 * Pour la créer, il faut indiquer l'ID de l'utilisateur qui l'a créée.
 */
public class Reservation {
    private static final int MAX_PLAYERS = 4;
    private final String id; // ID unique et stable
    private String tableId;
    private final String ownerId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private final List<String> players = new ArrayList<>();
    private final List<Comment> comments = new ArrayList<>();

    /**
     * Création d'une réservation de table de billard.
     * @param tableId Nom de la table de billard
     * @param creatorId Login de l'utilisateur créateur
     * @param startTime Date et heure de début de la réservation
     * @param endTime   Date et heure de fin de la réservation
     */
    public Reservation(String tableId, String creatorId, LocalDateTime startTime, LocalDateTime endTime) {
        this.id = java.util.UUID.randomUUID().toString().substring(0, 8); // ID unique stable
        this.tableId = tableId;
        // Pour permettre de distinguer les instances
        this.startTime = startTime;
        this.endTime = endTime;
        this.ownerId = creatorId;
        this.addPlayer(creatorId);
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getTableId() {
        return tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void addComment(Comment comment) {
        comments.add(comment);
    }

    public List<String> getPlayers() {
        return players;
    }

    public boolean hasPlayer(@Nonnull String player) {
        return players.contains(Objects.requireNonNull(player));
    }

    public boolean addPlayer(String player) {
        if(hasPlayer(player) || isCompleted()) {
            return false;
        }
        return players.add(player);
    }

    public boolean removePlayer(@Nonnull String player) {
        return players.remove(Objects.requireNonNull(player));
    }

    public boolean isCompleted() {
        return this.players.size() >= MAX_PLAYERS;
    }

    public String getId() {
        return this.id; // Retourne l'ID unique stable
    }
}
