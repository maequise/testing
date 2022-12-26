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

@Repository
@Transactional
@Slf4j
@AllArgsConstructor
public abstract class AbstractJpa<ID, TYPE> implements JpaDao<ID, TYPE> {
    private EntityManager entityManager;

    private Class<?> clazz = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1].getClass();

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
            if(determineId(entity) == null){
                return insert(entity);
            }
            return entityManager.merge(entity);
        } catch (PersistenceException  | InsertException e) {
            throw new UpdateException("Error during the update of the entity", e);
        } catch(InvocationTargetException | IllegalAccessException e) {
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
            entityManager.remove(entity);
            entityManager.flush();

            return true;
        }catch (PersistenceException e){
            throw new DeleteException("Error during the delete !", e);
        }
    }

    @Override
    public TYPE findById(ID id) {
        try {
            return (TYPE) entityManager.find(clazz, id);
        } catch (Exception e){
            log.error("Error during the fetching data", e);
            return null;
        }
    }

    @Override
    public TYPE fetchByQuery(String jpql) {
        try {
            Query query = entityManager.createQuery(jpql);
            return (TYPE) query.getSingleResult();
        }catch (Exception e){
            log.error("Error during the fetch query : " + jpql,e);
        }

        return null;
    }

    @Override
    public List<TYPE> fetchListByQuery(String jpql) {
        try {
            Query query = entityManager.createQuery(jpql);

            return (List<TYPE>) query.getResultList();
        }catch (Exception e){
            log.error("");
        }

        return Collections.emptyList();
    }

    @Override
    public TYPE fetchByQueryWithParams(String jpql, Object... params) {
        return null;
    }

    @Override
    public List<TYPE> fetchListByQueryWithParams(String jpql, Object... params) {
        return null;
    }

    private Object determineId(TYPE entity) throws InvocationTargetException, IllegalAccessException {
        var fields = entity.getClass().getDeclaredFields();

        var field = Arrays.stream(fields).filter(f -> f.isAnnotationPresent(Id.class))
                .findFirst().orElseThrow(() -> new UnknownIdException("You must define and ID !"));

        var method = Arrays.stream(entity.getClass().getDeclaredMethods()).filter(m -> m.getName().equalsIgnoreCase("get".concat(field.getName()))
                && m.getReturnType().equals(field.getType())).findFirst();

        return method.isPresent() ? method.get().invoke(entity) : new UnknownIdException("No method found !");
    }
}
