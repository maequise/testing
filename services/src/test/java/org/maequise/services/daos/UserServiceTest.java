package org.maequise.services.daos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.maequise.commons.exceptions.InsertException;
import org.maequise.models.entities.UserEntity;
import org.maequise.models.jpa.daos.UserDao;
import org.maequise.services.daos.impl.UserServiceImpl;
import org.maequise.services.dtos.UserDto;
import org.maequise.services.mappers.UserMapper;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    private UserService userService;

    private UserDao userDao;

    @BeforeEach
    void init() {
        this.userDao = mock(UserDao.class);
        this.userService = new UserServiceImpl(userDao, new UserMapper());
    }

    @Test
    void testRegisterNewUser() throws Exception {
        var userDto = createUserDto(null, "test", "mail");
        var entity = createUserEntity(1, "test", "mail");

        when(userDao.insert(any(UserEntity.class)))
                .thenReturn(entity);

        var captorDao = ArgumentCaptor.forClass(UserEntity.class);

        var userCreated = userService.registerNewUser(userDto);

        assertNotNull(userCreated);
        verify(userDao).insert(captorDao.capture());

        assertEquals(1, userCreated.getId());
        assertEquals(userDto.getUsername(), userCreated.getUsername());
        assertEquals(userDto.getEmail(), userCreated.getEmail());
    }

    @Test
    void testRegisterNewUserErrorDuringInsert() throws Exception {
        var userDto = createUserDto(null, "test", "mail");
        var entity = createUserEntity(1, "test", "mail");

        when(userDao.insert(any(UserEntity.class)))
                .thenThrow(new InsertException("error"));

        var captorDao = ArgumentCaptor.forClass(UserEntity.class);

        var userCreated = userService.registerNewUser(userDto);

        assertNull(userCreated);
        verify(userDao).insert(captorDao.capture());

        assertNull(captorDao.getValue().getId());
        assertEquals(userDto.getUsername(), captorDao.getValue().getUsername());
        assertEquals(userDto.getEmail(), captorDao.getValue().getEmail());
    }

    private UserDto createUserDto(Integer id, String name, String email){
        var user = new UserDto();

        user.setId(id);
        user.setUsername(name);
        user.setEmail(email);

        return user;
    }

    private UserEntity createUserEntity(Integer id, String name, String email){
        var entity = new UserEntity();

        entity.setId(id);
        entity.setUsername(name);
        entity.setEmail(email);

        return entity;
    }
}
