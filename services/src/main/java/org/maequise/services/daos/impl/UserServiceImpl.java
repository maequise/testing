package org.maequise.services.daos.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.maequise.commons.exceptions.InsertException;
import org.maequise.models.jpa.daos.UserDao;
import org.maequise.services.daos.UserService;
import org.maequise.services.dtos.UserDto;
import org.maequise.services.mappers.UserMapper;

@AllArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private UserDao userDao;

    private UserMapper userMapper;

    @Override
    public UserDto registerNewUser(UserDto dto) {
        try {
            var userEntity = userDao.insert(userMapper.convertDtoToEntity(dto));

            return userMapper.convertEntityToDto(userEntity);
        } catch (InsertException e) {
            log.error("Error during the creation of user", e);

            return null;
        }
    }
}
