package org.maequise.models.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Query;
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
    void testFetchByQueryWithParams() throws Exception {

    }

    @Test
    void testFetchByQueryListWithParams() throws Exception {

    }

    @Test
    void testFetchByQueryWithParamsNoResult() throws Exception {

    }@Test
    void testFetchByQueryListWithParamsNoResults() throws Exception {

    }

    private MockEntity createMockEntity(Integer id, String prop){
        var entity = new MockEntity();

        entity.setId(id);
        entity.setAnyProp(prop);

        return entity;
    }
}
