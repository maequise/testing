package org.maequise.models.jpa;

import org.maequise.commons.exceptions.DeleteException;
import org.maequise.commons.exceptions.InsertException;
import org.maequise.commons.exceptions.UpdateException;

import java.util.List;
import java.util.Map;

public interface JpaDao<ID, TYPE> {
    TYPE insert(TYPE entity) throws InsertException;

    TYPE update(TYPE entity) throws UpdateException;

    boolean delete(TYPE entity) throws DeleteException;

    TYPE findById(ID id);

    TYPE fetchByQuery(String jpql);

    List<TYPE> fetchListByQuery(String jpql);

    TYPE fetchByQueryWithParams(String jpql, Object... params);

    TYPE fetchByQueryWithPositionalParams(String jpql, Object...param);

    TYPE fetchByQueryWithNamedParams(String jpql, Map<String, Object> params);

    List<TYPE> fetchListByQueryWithParams(String jpql, Object... params);

    List<TYPE> fetchListByQueryWithPositionalParams(String jpql, Object... params);

    List<TYPE> fetchListByQueryWithNamedParams(String jpql, Map<String, Object> params);

    int deleteAll();
}
