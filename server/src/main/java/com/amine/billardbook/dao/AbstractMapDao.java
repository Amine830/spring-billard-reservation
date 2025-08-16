package com.amine.billardbook.dao;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Début d'implémentation de l'interface DAO sous forme d'une Map d'objets.
 * Classe abstraite qui doit être instanciée en fonction du type d'objet stocké et de clé.
 * @param <T> Le type d'objet auquel s'applique le DAO ; défini dans une sous-classe
 *
 * @author Lionel Médini
 */

public abstract class AbstractMapDao<T> implements Dao<T> {
    protected Map<Serializable, T> collection = new HashMap<>();

    @Override
    public Serializable add(T element) throws NameAlreadyBoundException {
        Serializable key = getKeyForElement(element);
        if(this.collection.containsKey(key)) {
            throw new NameAlreadyBoundException(key.toString());
        }
        this.collection.put(key, element);
        return key;
    }

    @Override
    public void delete(T element) throws NameNotFoundException {
        Serializable key = getKeyForElement(element);
        deleteById(key);
    }

    @Override
    public void deleteById(Serializable id) throws NameNotFoundException {
        if(!this.collection.containsKey(id)) {
            throw new NameNotFoundException(id.toString());
        }
        this.collection.remove(id);
    }

    @Override
    public void update(Serializable id, T element) {
        this.collection.put(id, element);
    }

    @Override
    public Serializable getId(T element) {
        return getKeyForElement(element);
    }

    @Override
    public T findOne(Serializable id) throws NameNotFoundException {
        if(!this.collection.containsKey(id)) {
            throw new NameNotFoundException(id.toString());
        }
        return this.collection.get(id);
    }

    @Override
    public Collection<T> findAll() {
        return this.collection.values();
    }

    /**
     * Renvoie la clé correspondant au type spécifique de l'élément<br>
     * Exemples : un champ "id" d'une classe, un hash des champs de l'objet...
     * @param element élément du type de la classe à stocker dans le DAO
     * @return une clé qui est une instance d'une sous-classe de <code>Serializable</code>
     */
    protected abstract Serializable getKeyForElement(T element);
}
