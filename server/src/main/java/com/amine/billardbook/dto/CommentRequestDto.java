package com.amine.billardbook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO pour les requêtes de commentaires.
 */
public class CommentRequestDto {

    @JsonProperty("content")
    private String commentText;


    // Getters et Setters
    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

}
