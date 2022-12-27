package org.maequise.models.jpa.daos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.maequise.models.entities.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class UserDaoTest {
    @Autowired
    private UserDao userDao;

    @BeforeEach
    void init() {
        userDao.deleteAll();
    }

    @Test
    void testInsertUser() throws Exception {
        assertTrue(userDao.fetchListByQuery("select e from UserEntity e").isEmpty());

        userDao.insert(createUserEntity("test", "email"));

        var results = userDao.fetchListByQuery("select e from UserEntity e");

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());

        assertEquals("test", results.get(0).getUsername());
        assertEquals("email", results.get(0).getEmail());
    }

    @Test
    void testUpdateUser() throws Exception {
        var userEntity = userDao.insert(createUserEntity("test", "mmailk"));

        assertNotNull(userEntity);
        assertNotNull(userEntity.getId());

        var newEntity = new UserEntity();
        newEntity.setId(userEntity.getId());
        newEntity.setUsername(userEntity.getUsername());
        newEntity.setEmail("I made a mistake !");

        userDao.update(newEntity);

        var foundEntity = userDao.findById(newEntity.getId());

        assertEquals("I made a mistake !", foundEntity.getEmail());
        assertEquals("mmailk", userEntity.getEmail());
    }

    @Test
    void testDeleteUser() throws Exception {
        var firstUser = userDao.insert(createUserEntity("test", "first"));
        var secondUser = userDao.insert(createUserEntity("test", "second"));

        var total = userDao.fetchListByQuery("select e from UserEntity e");

        assertFalse(total.isEmpty());
        assertEquals(2, total.size());

        var resultDeletion = userDao.delete(firstUser);

        assertTrue(resultDeletion);

        total = userDao.fetchListByQuery("select e from UserEntity e");
        assertFalse(total.isEmpty());
        assertEquals(1, total.size());
    }

    private UserEntity createUserEntity(String name, String email) {
        var user = new UserEntity();

        user.setUsername(name);
        user.setEmail(email);

        return user;
    }
}
