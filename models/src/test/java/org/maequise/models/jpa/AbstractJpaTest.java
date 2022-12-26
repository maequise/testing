package org.maequise.models.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.maequise.commons.exceptions.InsertException;
import org.maequise.commons.exceptions.UpdateException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
}
