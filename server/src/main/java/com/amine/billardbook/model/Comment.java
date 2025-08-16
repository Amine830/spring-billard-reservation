package com.amine.billardbook.model;

/**
 * Classe représentant un commentaire simple.
 * Il ne sera pas demandé de modifier/supprimer/... des commentaires.
 * @param authorId Login de l'auteur du commentaire
 * @param content Texte de contenu du commentaire
 */
public record Comment(String authorId, String content) { }
