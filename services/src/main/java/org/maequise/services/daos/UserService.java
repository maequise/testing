package org.maequise.services.daos;

import org.maequise.services.dtos.UserDto;

public interface UserService {
    UserDto registerNewUser(UserDto dto);
}
