package org.maequise.services.mappers;

import org.maequise.models.entities.UserEntity;
import org.maequise.services.dtos.UserDto;

import java.util.Collections;
import java.util.List;

public class UserMapper implements Mapper<UserEntity, UserDto> {
    @Override
    public UserEntity convertDtoToEntity(UserDto dto) {
        if(dto == null){
            return null;
        }

        var entity = new UserEntity();

        entity.setId(dto.getId());
        entity.setEmail(dto.getEmail());
        entity.setUsername(dto.getUsername());

        return entity;
    }

    @Override
    public UserDto convertEntityToDto(UserEntity entity) {
        if(entity == null){
            return null;
        }

        var dto = new UserDto();

        dto.setId(entity.getId());
        dto.setUsername(entity.getUsername());
        dto.setEmail(entity.getEmail());

        return dto;
    }

    @Override
    public List<UserEntity> convertDtoToEntities(List<UserDto> dtos) {
        if(dtos == null){
            return Collections.emptyList();
        }

        return dtos.stream().map(this::convertDtoToEntity).toList();
    }

    @Override
    public List<UserDto> convertEntitiesToDtos(List<UserEntity> entities) {
        if(entities == null){
            return Collections.emptyList();
        }

        return entities.stream().map(this::convertEntityToDto).toList();
    }
}
