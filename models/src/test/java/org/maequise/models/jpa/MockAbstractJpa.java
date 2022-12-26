package org.maequise.models.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import lombok.Data;

public class MockAbstractJpa extends AbstractJpa<Integer, MockAbstractJpa.MockEntity> {

    public MockAbstractJpa(EntityManager entityManager) {
        super(entityManager);
    }

    @Data
    public static class MockEntity {
        @Id
        private Integer id;
        private String anyProp;
    }
}
