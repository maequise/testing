package org.maequise.models.jpa;

import org.maequise.commons.exceptions.DeleteException;
import org.maequise.commons.exceptions.InsertException;
import org.maequise.commons.exceptions.UpdateException;

import java.util.List;

public interface JpaDao<ID, TYPE> {
    TYPE insert(TYPE entity) throws InsertException;

    TYPE update(TYPE entity) throws UpdateException;

    boolean delete(TYPE entity) throws DeleteException;

    TYPE findById(ID id);

    TYPE fetchByQuery(String jpql);

    List<TYPE> fetchListByQuery(String jpql);

    TYPE fetchByQueryWithParams(String jpql, Object... params);

    List<TYPE> fetchListByQueryWithParams(String jpql, Object... params);
}
