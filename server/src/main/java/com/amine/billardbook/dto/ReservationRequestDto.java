package com.amine.billardbook.dto;

import java.time.LocalDateTime;

/**
 * DTO pour les requêtes de création/modification de réservation de table de billard.
 */
public class ReservationRequestDto {
    private String tableId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // Getters and setters
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

}
