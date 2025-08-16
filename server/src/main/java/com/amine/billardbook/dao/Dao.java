package com.amine.billardbook.dao;

import javax.naming.InvalidNameException;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import java.io.Serializable;
import java.util.Collection;

/**
 * Interface de DAO générique : indépendante du type d'objet, de la structure de stockage (liste ou map) et du type des identifiants (string ou entier).<br>
 * Si un support de persistance devait être ajouté au projet, il le serait dans les classes d'implémentation de cette interface.<br>
 * <em>Note :</em> on pourra enrichir les DAOs de méthodes métier spécifiques dans les classes terminales
 * @param <T> Type des objets à prendre en charge par un DAO
 *
 * @author Lionel Médini
 */
public interface Dao<T> {
    /**
     * Ajoute un élément.
     * @param element L'élément à ajouter
     * @throws NameAlreadyBoundException Si la clé utilisée pour ajouter l'élément est déjà existante
     * @return La clé de l'élément ajouté
     */
    Serializable add(T element) throws NameAlreadyBoundException;

    /**
     * Supprime un élément.
     * @param element L'élément à supprimer
     * @throws NameNotFoundException Si l'élément à supprimer n'a pas été trouvé
     */
    void delete(T element) throws NameNotFoundException;

    /**
     * Supprime un élément.
     * @param id La clé de l'élément à supprimer
     * @throws NameNotFoundException Si l'élément à supprimer n'a pas été trouvé
     */
    void deleteById(Serializable id) throws NameNotFoundException, InvalidNameException;

    /**
     * Met à jour un élément ou le crée s'il n'existe pas.
     * @param id La clé de l'élément à mettre à jour
     * @param element L'élément par lequel remplacer l'ancien élément
     */
    void update(Serializable id, T element) throws InvalidNameException;

    /**
     * Renvoie la clé d'un élément.
     * @param element L'élément dont on recherche la clé
     * @return La clé de l'élément passé en paramètre
     * @throws NameNotFoundException Si l'élément à rechercher n'a pas été trouvé
     */
    Serializable getId(T element) throws NameNotFoundException;

    /**
     * Renvoie un élément à partir de sa clé.
     * @param id La clé de l'élément cherché
     * @return L'élément dont la clé est celle passée en paramètre
     * @throws NameNotFoundException Si la clé de l'élément à rechercher n'a pas été trouvée
     */
    T findOne(Serializable id) throws NameNotFoundException, InvalidNameException;

    /**
     * Renvoie tous les éléments.
     * @return La collection (potentiellement vide) d'éléments stockés
     */
    Collection<T> findAll();
}
