package org.maequise.models.jpa;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.maequise.commons.exceptions.DeleteException;
import org.maequise.commons.exceptions.InsertException;
import org.maequise.commons.exceptions.UnknownIdException;
import org.maequise.commons.exceptions.UpdateException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Repository
@Transactional
@Slf4j
public abstract class AbstractJpa<ID, TYPE> implements JpaDao<ID, TYPE> {
    @PersistenceContext
    private EntityManager entityManager;

    private Class<TYPE> clazz = (Class<TYPE>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];

    @Override
    public TYPE insert(TYPE entity) throws InsertException {
        try {
            entityManager.persist(entity);
            entityManager.flush();

            return entity;
        } catch (PersistenceException e) {
            log.error("Error during the insertion", e);
            throw new InsertException("Error during the persisting entity", e);
        }
    }

    @Override
    public TYPE update(TYPE entity) throws UpdateException {
        try {
            if (determineId(entity) == null) {
                return insert(entity);
            }
            return entityManager.merge(entity);
        } catch (PersistenceException | InsertException e) {
            throw new UpdateException("Error during the update of the entity", e);
        } catch (UnknownIdException e) {
            throw new UpdateException("Error during determining ID operation", e);
        } finally {
            try {
                entityManager.flush();
            } catch (TransactionRequiredException e) {
                //nothing to do !
            }
        }
    }

    @Override
    public boolean delete(TYPE entity) throws DeleteException {
        try {
            var attachedEntity = entityManager.merge(entity);

            entityManager.remove(attachedEntity);
            entityManager.flush();

            return true;
        } catch (PersistenceException e) {
            throw new DeleteException("Error during the delete !", e);
        }
    }

    @Override
    public TYPE findById(ID id) {
        try {
            return entityManager.find(clazz, id);
        } catch (Exception e) {
            log.error("Error during the fetching data", e);
            return null;
        }
    }

    @Override
    public TYPE fetchByQuery(String jpql) {
        try {
            Query query = entityManager.createQuery(jpql);
            return (TYPE) query.getSingleResult();
        } catch (Exception e) {
            log.error("Error during the fetch query : " + jpql, e);
        }

        return null;
    }

    @Override
    public List<TYPE> fetchListByQuery(String jpql) {
        try {
            Query query = entityManager.createQuery(jpql);

            return query.getResultList();
        } catch (Exception e) {
            log.error("");
        }

        return Collections.emptyList();
    }

    @Override
    public TYPE fetchByQueryWithParams(String jpql, Object... params) {
        if (params.length >= 1 && !(params[0] instanceof Map)) {
            return fetchByQueryWithPositionalParams(jpql, params);
        } else {
            return fetchByQueryWithNamedParams(jpql, (Map<String, Object>) params[0]);
        }
    }

    @Override
    public TYPE fetchByQueryWithPositionalParams(String jpql, Object... params) {
        try {
            TypedQuery<TYPE> query = entityManager.createQuery(jpql, clazz);

            for (int i = 1; i <= params.length; i++) {
                query.setParameter(i, params[i - 1]);
            }

            return query.getSingleResult();
        } catch (NoResultException e) {
            log.error("No entry found", e);
        } catch (NonUniqueResultException e) {
            log.error("More than 1 result found", e);
        }

        //default behavior
        return null;
    }

    @Override
    public TYPE fetchByQueryWithNamedParams(String jpql, Map<String, Object> params) {
        try {
            var query = entityManager.createQuery(jpql, clazz);

            params.forEach(query::setParameter);

            return query.getSingleResult();
        } catch (NoResultException e) {
            log.error("No entry found with the query {} and params", jpql, params);
            log.error("Error during the fetch", e);
        } catch (NonUniqueResultException e) {
            log.error("More than 1 entry found with the query {} and params", jpql, params);
            log.error("Error during the fetch", e);
        }
        return null;
    }

    @Override
    public List<TYPE> fetchListByQueryWithParams(String jpql, Object... params) {
        if (params.length >= 1 && !(params[0] instanceof Map)) {
            return fetchListByQueryWithPositionalParams(jpql, params);
        } else {
            return fetchListByQueryWithNamedParams(jpql, (Map<String, Object>) params[0]);
        }
    }

    @Override
    public List<TYPE> fetchListByQueryWithPositionalParams(String jpql, Object... params) {
        try {
            var query = entityManager.createQuery(jpql, clazz);

            for (var i = 1; i <= params.length; i++) {
                query.setParameter(i, params[i - 1]);
            }

            return query.getResultList();
        } catch (Exception e) {
            log.error("Error during the execution of the query {} with params {}", jpql, params);
            log.error("Error encountered", e);
        }
        return Collections.emptyList();
    }

    @Override
    public List<TYPE> fetchListByQueryWithNamedParams(String jpql, Map<String, Object> params) {
        try {
            var query = entityManager.createQuery(jpql, clazz);

            params.forEach(query::setParameter);

            return query.getResultList();
        } catch (Exception e) {
            log.error("Error during the execution of the query {} with params {}", jpql, params);
            log.error("Error encountered", e);
        }

        return Collections.emptyList();
    }

    @Override
    public int deleteAll() {
        try {
            var query = entityManager.createQuery("delete from " + clazz.getSimpleName() + " e");

            var totalDeleted =  query.executeUpdate();

            entityManager.flush();

            return totalDeleted;
        }catch (Exception e){
            log.error("Error during the execution of delete query", e);
        }

        return 0;
    }

    private Object determineId(TYPE entity) {
        var fields = entity.getClass().getDeclaredFields();

        var field = Arrays.stream(fields).filter(f -> f.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(() -> new UnknownIdException("You must define and ID !"));

        try {
            var method = Arrays.stream(entity.getClass().getDeclaredMethods())
                    .filter(m -> m.getName().equalsIgnoreCase("get".concat(field.getName()))
                            && m.getReturnType().equals(field.getType()))
                    .findFirst();

            return method.isPresent() ? method.get().invoke(entity) : new UnknownIdException("No method found !");
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new UnknownIdException("Error during the determining of the ID property");
        }
    }
}
