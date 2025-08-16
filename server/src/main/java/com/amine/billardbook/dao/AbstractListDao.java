package com.amine.billardbook.dao;

import com.amine.billardbook.exception.DeletedReservationException;

import javax.naming.InvalidNameException;
import javax.naming.NameNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Début d'implémentation de l'interface DAO sous forme d'une List d'objets.
 * Classe abstraite qui doit être instanciée en fonction du type d'objet stocké (sa clé sera nécessairement un Integer contenant l'index de l'objet).
 * @param <T> Le type d'objet auquel s'applique le DAO ; défini dans une sous-classe
 *
 * @author Lionel Médini
 */
public abstract class AbstractListDao<T> implements Dao<T> {
    protected final List<T> collection = new ArrayList<>();

    @Override
    public Serializable add(T element) {
        this.collection.add(element);
        return this.collection.size() - 1;
    }

    @Override
    public void delete(T element) throws NameNotFoundException, NullPointerException {
        // La méthode remove de List décale tous les éléments.
        // On préfère remplacer les éléments supprimés par NULL, quitte à augmenter la taille de la liste.
        int index = this.collection.indexOf(element);
        if(index == -1) {
            throw new NameNotFoundException(element.toString());
        }
        this.collection.set(index, null);
    }

    @Override
    public void deleteById(Serializable id) throws NameNotFoundException, InvalidNameException, DeletedReservationException {
        try {
            T element = this.findOne(id);
            if(element == null) {
                // L'élément a déjà été supprimé.
                throw new NameNotFoundException(id.toString());
            }
            this.delete(element);
        } catch (NullPointerException e) {
            throw new NameNotFoundException(id.toString());
        }
    }

    @Override
    public void update(Serializable id, T element) throws InvalidNameException {
        try {
            this.collection.set((Integer) id, element);
        } catch(Exception e) {
            throw new InvalidNameException(id.toString());
        }
    }

    @Override
    public Serializable getId(T element) {
        return getKeyForElement(element);
    }

    @Override
    public T findOne(Serializable id) throws NameNotFoundException, InvalidNameException {
        try {
            return this.collection.get((Integer) id);
        } catch(ClassCastException e) {
            throw new InvalidNameException(id.toString());
        } catch(IndexOutOfBoundsException e) {
            throw new NameNotFoundException(id.toString());
        }
    }

    @Override
    public Collection<T> findAll() {
        return new ArrayList<>(this.collection);
    }

    /**
     * Renvoie la clé correspondant au type spécifique de l'élément<br>
     * Exemples : un champ "id" d'une classe, un hash des champs de l'objet...
     * @param element élément du type de la classe à stocker dans le DAO
     * @return un entier qui est l'index de l'élément dans la liste ou -1 s'il n'a pas été trouvé
     */
    protected Serializable getKeyForElement(T element) {
        return this.collection.indexOf(element);
    }
}
