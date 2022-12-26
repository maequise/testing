package org.maequise.models.jpa;

import jakarta.persistence.*;
import org.hibernate.NonUniqueResultException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.maequise.commons.exceptions.DeleteException;
import org.maequise.commons.exceptions.InsertException;
import org.maequise.commons.exceptions.UpdateException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.maequise.models.jpa.MockAbstractJpa.MockEntity;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
class AbstractJpaTest {
    @Mock
    private EntityManager entityManager;
    private MockAbstractJpa jpaDao;

    @BeforeEach
    void init() {
        this.jpaDao = new MockAbstractJpa(entityManager);
    }

    @Test
    void testInit() {
        assertNotNull(this.entityManager);
        assertNotNull(this.jpaDao);
    }

    @Test
    void testInsert() throws Exception {
        doNothing().when(entityManager).persist(any());
        doNothing().when(entityManager).flush();

        var captorPersistMethod = ArgumentCaptor.forClass(Object.class);
        jpaDao.insert(new MockEntity());

        verify(entityManager).persist(captorPersistMethod.capture());
        verify(entityManager).flush();

        assertNotNull(captorPersistMethod.getValue());
    }

    @Test
    void testInsertError() {
        doThrow(new PersistenceException("Error !")).when(entityManager).persist(any());

        assertThrows(InsertException.class, () -> jpaDao.insert(new MockEntity()));
    }

    @Test
    void testUpdate() throws Exception {
        var entityToUpdate = new MockEntity();
        entityToUpdate.setId(1);
        entityToUpdate.setAnyProp("old val");

        var updatedEntity = new MockEntity();
        updatedEntity.setId(1);
        updatedEntity.setAnyProp("new value");

        when(entityManager.merge(any()))
                .thenReturn(updatedEntity);
        doNothing().when(entityManager).flush();

        var captorInsertEntity = ArgumentCaptor.forClass(MockEntity.class);

        jpaDao.update(updatedEntity);

        verify(entityManager).merge(captorInsertEntity.capture());
        verify(entityManager).flush();

        assertNotNull(captorInsertEntity.getValue());
        assertEquals(1, captorInsertEntity.getValue().getId());
        assertEquals("new value", captorInsertEntity.getValue().getAnyProp());
    }

    @Test
    void testUpdateError() {
        when(entityManager.merge(any()))
                .thenThrow(new PersistenceException("Error during the update"));
        var entity = new MockEntity();
        entity.setId(1);

        assertThrows(UpdateException.class, () -> jpaDao.update(entity));
    }

    @Test
    void testUpdateErrorAndFlushFails() {
        when(entityManager.merge(any()))
                .thenThrow(new PersistenceException("Error during the update"));

        doThrow(new TransactionRequiredException()).when(entityManager).flush();

        var entity = new MockEntity();
        entity.setId(1);

        assertThrows(UpdateException.class, () -> jpaDao.update(entity));
    }

    @Test
    void testUpdateWithPersistence() throws Exception {
        var entityToUpdate = new MockEntity();
        entityToUpdate.setAnyProp("I should persist !");

        var entityUpdatedAndPersisted = new MockEntity();
        entityUpdatedAndPersisted.setAnyProp("I should persist !");
        entityUpdatedAndPersisted.setId(1);

        var captorOfPersist = ArgumentCaptor.forClass(MockEntity.class);

        doNothing().when(entityManager).persist(any());
        doNothing().when(entityManager).flush();

        jpaDao.update(entityToUpdate);

        verify(entityManager).persist(captorOfPersist.capture());
        verify(entityManager, times(2)).flush();

        assertNotNull(captorOfPersist.getValue());
    }

    @Test
    void testDeleteEntity() throws Exception {
        doNothing().when(entityManager).remove(any());

        var entity = createMockEntity(1, "ne");
        var resultOfDeletion = jpaDao.delete(entity);

        verify(entityManager).remove(entity);
        verify(entityManager).flush();

        assertTrue(resultOfDeletion);
    }

    @Test
    void testDeleteEntityError() {
        doThrow(new PersistenceException("Error during the delete")).when(entityManager).remove(any());

        assertThrows(DeleteException.class, () -> jpaDao.delete(createMockEntity(1, "test")));
    }

    @Test
    void testFindEntity() throws Exception {
        var entityToFind = createMockEntity(1, "found");

        when(entityManager.find(any(), any(Integer.class)))
                .thenReturn(entityToFind);

        var captorEntity = ArgumentCaptor.forClass(Class.class);
        var captorIdValue = ArgumentCaptor.forClass(Integer.class);

        jpaDao.findById(1);

        verify(entityManager).find(captorEntity.capture(), captorIdValue.capture());

        assertEquals(1, captorIdValue.getValue());
        assertEquals(MockEntity.class, captorEntity.getValue());
    }

    @Test
    void testFindEntityNoResult() throws Exception {
        when(entityManager.find(any(), any(Integer.class)))
                .thenReturn(null);

        var captorEntity = ArgumentCaptor.forClass(Class.class);
        var captorIdValue = ArgumentCaptor.forClass(Integer.class);

        var result = jpaDao.findById(1);

        verify(entityManager).find(captorEntity.capture(), captorIdValue.capture());

        assertEquals(1, captorIdValue.getValue());
        assertEquals(MockEntity.class, captorEntity.getValue());
        assertNull(result);
    }

    @Test
    void testFindEntityWithErrorReturnNull() {
        when(entityManager.find(any(), any(Integer.class)))
                .thenThrow(new IllegalArgumentException("Error"));

        var captorEntity = ArgumentCaptor.forClass(Class.class);
        var captorIdValue = ArgumentCaptor.forClass(Integer.class);

        var result = jpaDao.findById(1);

        verify(entityManager).find(captorEntity.capture(), captorIdValue.capture());

        assertEquals(1, captorIdValue.getValue());
        assertEquals(MockEntity.class, captorEntity.getValue());
        assertNull(result);
    }

    @Test
    void testFindEntityWithQuery() throws Exception {
        var entityFound = createMockEntity(1, "by found");
        var query = mock(Query.class);

        when(query.getSingleResult())
                .thenReturn(entityFound);

        when(entityManager.createQuery(anyString()))
                .thenReturn(query);

        var captorQuery = ArgumentCaptor.forClass(String.class);

        var resultFound = jpaDao.fetchByQuery("select e from MockEntity where e.anyProp=\"test\"");

        verify(entityManager).createQuery(captorQuery.capture());
        verify(query).getSingleResult();

        assertNotNull(resultFound);

        assertEquals("select e from MockEntity where e.anyProp=\"test\"", captorQuery.getValue());
    }

    @Test
    void testFindEntitiesWithQuery() throws Exception {
        var entityFound = new ArrayList<MockEntity>();

        entityFound.add(createMockEntity(1, "by found"));
        entityFound.add(createMockEntity(2, "df"));

        var query = mock(Query.class);

        when(query.getResultList())
                .thenReturn(entityFound);

        when(entityManager.createQuery(anyString()))
                .thenReturn(query);

        var captorQuery = ArgumentCaptor.forClass(String.class);

        var resultFound = jpaDao.fetchListByQuery("select e from MockEntity");

        verify(entityManager).createQuery(captorQuery.capture());
        verify(query).getResultList();

        assertNotNull(resultFound);

        assertEquals("select e from MockEntity", captorQuery.getValue());
    }

    @Test
    void testFindEntitiesWithQueryGenerateError() throws Exception {
        var entityFound = new ArrayList<MockEntity>();

        entityFound.add(createMockEntity(1, "by found"));
        entityFound.add(createMockEntity(2, "df"));

        var query = mock(Query.class);

        when(query.getResultList())
                .thenThrow(new IllegalStateException("Trying to update"));

        when(entityManager.createQuery(anyString()))
                .thenReturn(query);

        var captorQuery = ArgumentCaptor.forClass(String.class);

        var resultFound = jpaDao.fetchListByQuery("UPDATE e from MockEntity");

        verify(entityManager).createQuery(captorQuery.capture());
        verify(query).getResultList();

        assertTrue(resultFound.isEmpty());

        assertEquals("UPDATE e from MockEntity", captorQuery.getValue());
    }

    @Test
    void testFindEntityNoResultWithQuery() throws Exception {
        var query = mock(Query.class);

        when(entityManager.createQuery(anyString()))
                .thenReturn(query);

        when(query.getSingleResult()).thenThrow(new NoResultException("no result"));

        var captorQuery = ArgumentCaptor.forClass(String.class);

        var resultFound = jpaDao.fetchByQuery("select e from MockEntity where e.anyProp=\"test\"");

        verify(entityManager).createQuery(captorQuery.capture());
        verify(query).getSingleResult();

        assertNull(resultFound);

        assertEquals("select e from MockEntity where e.anyProp=\"test\"", captorQuery.getValue());
    }

    @Test
    void testFindEntityNonUniqueResultWithQuery() throws Exception {
        var query = mock(Query.class);

        when(entityManager.createQuery(anyString()))
                .thenReturn(query);

        when(query.getSingleResult()).thenThrow(new NonUniqueResultException(2));

        var captorQuery = ArgumentCaptor.forClass(String.class);

        var resultFound = jpaDao.fetchByQuery("select e from MockEntity where e.anyProp=\"test\"");

        verify(entityManager).createQuery(captorQuery.capture());
        verify(query).getSingleResult();

        assertNull(resultFound);

        assertEquals("select e from MockEntity where e.anyProp=\"test\"", captorQuery.getValue());
    }

    @Test
    void testFindEntitiesEmptyResultWithQuery() throws Exception {
        var entityFound = new ArrayList<MockEntity>();

        var query = mock(Query.class);

        when(query.getResultList())
                .thenReturn(entityFound);

        when(entityManager.createQuery(anyString()))
                .thenReturn(query);

        var captorQuery = ArgumentCaptor.forClass(String.class);

        var resultFound = jpaDao.fetchListByQuery("select e from MockEntity");

        verify(entityManager).createQuery(captorQuery.capture());
        verify(query).getResultList();

        assertNotNull(resultFound);

        assertEquals("select e from MockEntity", captorQuery.getValue());
        assertTrue(resultFound.isEmpty());
    }

    @Test
    void testFetchByQueryWithNamedOrPositionalParams() throws Exception {
        var entity = createMockEntity(1, "test");

        var query = mock(TypedQuery.class);

        var captorQueryQuery = ArgumentCaptor.forClass(String.class);
        var captorQueryTyped = ArgumentCaptor.forClass(Class.class);
        var captorParamPosition = ArgumentCaptor.forClass(Integer.class);
        var captorParamValue = ArgumentCaptor.forClass(Object.class);

        when(query.setParameter(anyInt(), any()))
                .thenReturn(query);

        when(query.getSingleResult())
                .thenReturn(entity);

        when(entityManager.createQuery(anyString(), any()))
                .thenReturn(query);

        var resultReturn = jpaDao.fetchByQueryWithParams("select e from MockEntity e where e.anyProp=?1", "test");

        verify(entityManager).createQuery(captorQueryQuery.capture(), captorQueryTyped.capture());
        verify(query).setParameter(captorParamPosition.capture(), captorParamValue.capture());

        assertNotNull(resultReturn);
        assertEquals("select e from MockEntity e where e.anyProp=?1", captorQueryQuery.getValue());
    }

    @Test
    void testFetchByQueryWithNamedOrPositionalParamsThrowNoResult() throws Exception {
        var entity = createMockEntity(1, "test");

        var query = mock(TypedQuery.class);

        var captorQueryQuery = ArgumentCaptor.forClass(String.class);
        var captorQueryTyped = ArgumentCaptor.forClass(Class.class);
        var captorParamPosition = ArgumentCaptor.forClass(Integer.class);
        var captorParamValue = ArgumentCaptor.forClass(Object.class);

        when(query.setParameter(anyInt(), any()))
                .thenReturn(query);

        when(query.getSingleResult())
                .thenThrow(new NoResultException("No entity found"));

        when(entityManager.createQuery(anyString(), any()))
                .thenReturn(query);

        var resultReturn = jpaDao.fetchByQueryWithParams("select e from MockEntity e where e.anyProp=?1", "test");

        verify(entityManager).createQuery(captorQueryQuery.capture(), captorQueryTyped.capture());
        verify(query).setParameter(captorParamPosition.capture(), captorParamValue.capture());

        assertNull(resultReturn);
        assertEquals("select e from MockEntity e where e.anyProp=?1", captorQueryQuery.getValue());
    }

    @Test
    void testFetchByQueryWithNamedOrPositionalParamsThrowNonUniqueResult() throws Exception {
        var entity = createMockEntity(1, "test");

        var query = mock(TypedQuery.class);

        var captorQueryQuery = ArgumentCaptor.forClass(String.class);
        var captorQueryTyped = ArgumentCaptor.forClass(Class.class);
        var captorParamPosition = ArgumentCaptor.forClass(Integer.class);
        var captorParamValue = ArgumentCaptor.forClass(Object.class);

        when(query.setParameter(anyInt(), any()))
                .thenReturn(query);

        when(query.getSingleResult())
                .thenThrow(new jakarta.persistence.NonUniqueResultException("Non unique"));

        when(entityManager.createQuery(anyString(), any()))
                .thenReturn(query);

        var resultReturn = jpaDao.fetchByQueryWithParams("select e from MockEntity e where e.anyProp=?1", "test");

        verify(entityManager).createQuery(captorQueryQuery.capture(), captorQueryTyped.capture());
        verify(query).setParameter(captorParamPosition.capture(), captorParamValue.capture());

        assertNull(resultReturn);
        assertEquals("select e from MockEntity e where e.anyProp=?1", captorQueryQuery.getValue());
    }

    @Test
    void testFetchByQueryWithNamedParams() throws Exception {
        var entity = createMockEntity(1, "test");

        var query = mock(TypedQuery.class);

        var captorQueryQuery = ArgumentCaptor.forClass(String.class);
        var captorQueryTyped = ArgumentCaptor.forClass(Class.class);
        var captorParamPosition = ArgumentCaptor.forClass(String.class);
        var captorParamValue = ArgumentCaptor.forClass(Object.class);

        when(query.setParameter(anyString(), any()))
                .thenReturn(query);

        when(query.getSingleResult())
                .thenReturn(entity);

        when(entityManager.createQuery(anyString(), any()))
                .thenReturn(query);

        var resultReturn = jpaDao.fetchByQueryWithParams("select e from MockEntity e where e.anyProp=:param", Collections.singletonMap("param", "test"));

        verify(entityManager).createQuery(captorQueryQuery.capture(), captorQueryTyped.capture());
        verify(query).setParameter(captorParamPosition.capture(), captorParamValue.capture());

        assertNotNull(resultReturn);
        assertEquals("select e from MockEntity e where e.anyProp=:param", captorQueryQuery.getValue());
        assertEquals(MockEntity.class, captorQueryTyped.getValue());
    }

    @Test
    void testFetchByQueryWithNamedParamsNoResult() throws Exception {
        var entity = createMockEntity(1, "test");

        var query = mock(TypedQuery.class);

        var captorQueryQuery = ArgumentCaptor.forClass(String.class);
        var captorQueryTyped = ArgumentCaptor.forClass(Class.class);
        var captorParamPosition = ArgumentCaptor.forClass(String.class);
        var captorParamValue = ArgumentCaptor.forClass(Object.class);

        when(query.setParameter(anyString(), any()))
                .thenReturn(query);

        when(query.getSingleResult())
                .thenThrow(new NoResultException("Nothing found"));

        when(entityManager.createQuery(anyString(), any()))
                .thenReturn(query);

        var resultReturn = jpaDao.fetchByQueryWithParams("select e from MockEntity e where e.anyProp=:param", Collections.singletonMap("param", "test"));

        verify(entityManager).createQuery(captorQueryQuery.capture(), captorQueryTyped.capture());
        verify(query).setParameter(captorParamPosition.capture(), captorParamValue.capture());

        assertNull(resultReturn);
        assertEquals("select e from MockEntity e where e.anyProp=:param", captorQueryQuery.getValue());
        assertEquals(MockEntity.class, captorQueryTyped.getValue());
    }

    @Test
    void testFetchByQueryWithNamedParamsNonUniqueResult() throws Exception {
        var entity = createMockEntity(1, "test");

        var query = mock(TypedQuery.class);

        var captorQueryQuery = ArgumentCaptor.forClass(String.class);
        var captorQueryTyped = ArgumentCaptor.forClass(Class.class);
        var captorParamPosition = ArgumentCaptor.forClass(String.class);
        var captorParamValue = ArgumentCaptor.forClass(Object.class);

        when(query.setParameter(anyString(), any()))
                .thenReturn(query);

        when(query.getSingleResult())
                .thenThrow(new jakarta.persistence.NonUniqueResultException("More than 1 found"));

        when(entityManager.createQuery(anyString(), any()))
                .thenReturn(query);

        var resultReturn = jpaDao.fetchByQueryWithParams("select e from MockEntity e where e.anyProp=:param", Collections.singletonMap("param", "test"));

        verify(entityManager).createQuery(captorQueryQuery.capture(), captorQueryTyped.capture());
        verify(query).setParameter(captorParamPosition.capture(), captorParamValue.capture());

        assertNull(resultReturn);
        assertEquals("select e from MockEntity e where e.anyProp=:param", captorQueryQuery.getValue());
        assertEquals(MockEntity.class, captorQueryTyped.getValue());
    }

    @Test
    void testFetchByQueryListWithParams() throws Exception {
        var entities = new ArrayList<MockEntity>();

        entities.add(createMockEntity(1, "test"));
        entities.add(createMockEntity(2, "test2"));


        var query = mock(TypedQuery.class);

        var captorQueryQuery = ArgumentCaptor.forClass(String.class);
        var captorQueryTyped = ArgumentCaptor.forClass(Class.class);
        var captorParamPosition = ArgumentCaptor.forClass(String.class);
        var captorParamValue = ArgumentCaptor.forClass(Object.class);

        when(query.setParameter(anyString(), any()))
                .thenReturn(query);

        when(query.getResultList())
                .thenReturn(entities);

        when(entityManager.createQuery(anyString(), any()))
                .thenReturn(query);

        var resultReturn = jpaDao.fetchListByQueryWithParams("select e from MockEntity e where e.anyProp IN (?1)", Collections.singletonMap("param", Stream.of(1,2).toList()));

        verify(entityManager).createQuery(captorQueryQuery.capture(), captorQueryTyped.capture());
        verify(query).setParameter(captorParamPosition.capture(), captorParamValue.capture());

        assertNotNull(resultReturn);
        assertEquals("select e from MockEntity e where e.anyProp IN (?1)", captorQueryQuery.getValue());
        assertEquals(MockEntity.class, captorQueryTyped.getValue());
    }

    @Test
    void testFetchListByQueryWithParams() throws Exception {
        var entities = new ArrayList<MockEntity>();

        entities.add(createMockEntity(1, "test"));
        entities.add(createMockEntity(2, "test2"));


        var query = mock(TypedQuery.class);

        var captorQueryQuery = ArgumentCaptor.forClass(String.class);
        var captorQueryTyped = ArgumentCaptor.forClass(Class.class);
        var captorParamPosition = ArgumentCaptor.forClass(Integer.class);
        var captorParamValue = ArgumentCaptor.forClass(Object.class);

        when(query.setParameter(anyInt(), any()))
                .thenReturn(query);

        when(query.getResultList())
                .thenReturn(entities);

        when(entityManager.createQuery(anyString(), any()))
                .thenReturn(query);

        var resultReturn = jpaDao.fetchListByQueryWithParams("select e from MockEntity e where e.anyProp = ?1", 3);

        verify(entityManager).createQuery(captorQueryQuery.capture(), captorQueryTyped.capture());
        verify(query).setParameter(captorParamPosition.capture(), captorParamValue.capture());

        assertFalse(resultReturn.isEmpty());
        assertEquals("select e from MockEntity e where e.anyProp = ?1", captorQueryQuery.getValue());
        assertEquals(2, resultReturn.size());
        assertEquals(MockEntity.class, captorQueryTyped.getValue());
    }

    @Test
    void testFetchByQueryListWithNamedParamsEmptyResults() throws Exception {
        var query = mock(TypedQuery.class);

        var captorQueryQuery = ArgumentCaptor.forClass(String.class);
        var captorQueryTyped = ArgumentCaptor.forClass(Class.class);
        var captorParamPosition = ArgumentCaptor.forClass(String.class);
        var captorParamValue = ArgumentCaptor.forClass(Object.class);

        when(query.setParameter(anyString(), any()))
                .thenReturn(query);

        when(query.getResultList())
                .thenThrow(new IllegalStateException("An error happened"));

        when(entityManager.createQuery(anyString(), any()))
                .thenReturn(query);

        var resultReturn = jpaDao.fetchListByQueryWithParams("select e from MockEntity e where e.anyProp = :param", Collections.singletonMap("param", 1));

        verify(entityManager).createQuery(captorQueryQuery.capture(), captorQueryTyped.capture());
        verify(query).setParameter(captorParamPosition.capture(), captorParamValue.capture());

        assertTrue(resultReturn.isEmpty());
        assertEquals("select e from MockEntity e where e.anyProp = :param", captorQueryQuery.getValue());
        assertEquals(MockEntity.class, captorQueryTyped.getValue());
    }

    @Test
    void testFetchByQueryListWithPositionalParamsEmptyResults() throws Exception {
        var query = mock(TypedQuery.class);

        var captorQueryQuery = ArgumentCaptor.forClass(String.class);
        var captorQueryTyped = ArgumentCaptor.forClass(Class.class);
        var captorParamPosition = ArgumentCaptor.forClass(Integer.class);
        var captorParamValue = ArgumentCaptor.forClass(Object.class);

        when(query.setParameter(anyInt(), any()))
                .thenReturn(query);

        when(query.getResultList())
                .thenThrow(new IllegalStateException("An error happened"));

        when(entityManager.createQuery(anyString(), any()))
                .thenReturn(query);

        var resultReturn = jpaDao.fetchListByQueryWithParams("select e from MockEntity e where e.anyProp = ?1", 3);

        verify(entityManager).createQuery(captorQueryQuery.capture(), captorQueryTyped.capture());
        verify(query).setParameter(captorParamPosition.capture(), captorParamValue.capture());

        assertTrue(resultReturn.isEmpty());
        assertEquals("select e from MockEntity e where e.anyProp = ?1", captorQueryQuery.getValue());
        assertEquals(MockEntity.class, captorQueryTyped.getValue());
    }

    private MockEntity createMockEntity(Integer id, String prop){
        var entity = new MockEntity();

        entity.setId(id);
        entity.setAnyProp(prop);

        return entity;
    }
}
