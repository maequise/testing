package org.maequise.models.jpa;

import org.maequise.commons.exceptions.InsertException;
import org.maequise.commons.exceptions.UpdateException;

public interface JpaDao<ID, TYPE> {
    TYPE insert(TYPE entity) throws InsertException;

    TYPE update(TYPE entity) throws UpdateException;
}
