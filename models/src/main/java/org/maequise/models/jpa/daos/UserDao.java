package org.maequise.models.jpa.daos;

import jakarta.persistence.EntityManager;
import org.maequise.models.entities.UserEntity;
import org.maequise.models.jpa.AbstractJpa;
import org.springframework.stereotype.Repository;

@Repository
public class UserDao extends AbstractJpa<Integer, UserEntity> {
}
