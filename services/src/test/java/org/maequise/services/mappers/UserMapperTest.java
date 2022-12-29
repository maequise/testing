package org.maequise.services.mappers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.maequise.models.entities.UserEntity;
import org.maequise.services.dtos.UserDto;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {
    Mapper<UserEntity, UserDto> mapper;

    @BeforeEach
    void init() {
        this.mapper = new UserMapper();
    }

    @Test
    void testConvertEntityToDto() {
        var entity = createUserEntity(1, "test", "test@test.com");
        var dto = createDto(1, "test", "test@test.com");

        var entityConverted = mapper.convertDtoToEntity(dto);

        assertNotNull(entityConverted);
        assertEquals(1, entityConverted.getId());
        assertEquals("test", entityConverted.getUsername());
        assertEquals("test@test.com", entityConverted.getEmail());

        assertEquals(entity, entityConverted);
    }

    @Test
    void testConvertEntityToDtoWithNullEntity() {
        var entityConverted = mapper.convertDtoToEntity(null);

        assertNull(entityConverted);
    }

    @Test
    void testConvertDtoToEntity() {
        var entity = createUserEntity(1, "test", "test@test.com");
        var dto = createDto(1, "test", "test@test.com");

        var dtoConverted = mapper.convertEntityToDto(entity);

        assertNotNull(dtoConverted);
        assertEquals(1, dtoConverted.getId());
        assertEquals("test", dtoConverted.getUsername());
        assertEquals("test@test.com", dtoConverted.getEmail());

        assertEquals(dto, dtoConverted);
    }

    @Test
    void testConvertDtoToEntityWithNullDto() {
        var dtoConverted = mapper.convertEntityToDto(null);

        assertNull(dtoConverted);
    }

    @Test
    void testConvertDtosToEntities() {
        var dtos = Collections.singletonList(createDto(1, "test", "test@mail.com"));
        var entities = Collections.singletonList(createUserEntity(1, "test", "test@mail.com"));

        var entitiesConverted = mapper.convertDtoToEntities(dtos);

        assertNotNull(entitiesConverted);
        assertFalse(entitiesConverted.isEmpty());

        assertEquals(1, entitiesConverted.size());
        assertEquals(1, entitiesConverted.get(0).getId());
        assertEquals("test", entitiesConverted.get(0).getUsername());
        assertEquals("test@mail.com", entitiesConverted.get(0).getEmail());

        assertEquals(entities, entitiesConverted);
    }

    @Test
    void testConvertDtosToEntitiesWithNullDtos() {
        var dtos = Collections.singletonList(createDto(1, "test", "test@mail.com"));
        var entities = Collections.singletonList(createUserEntity(1, "test", "test@mail.com"));

        var entitiesConverted = mapper.convertDtoToEntities(null);

        assertNotNull(entitiesConverted);
        assertTrue(entitiesConverted.isEmpty());
    }

    @Test
    void testConvertDtosToEntitiesWithEmptyDtos() {
        List<UserDto> dtos = Collections.emptyList();

        var entitiesConverted = mapper.convertDtoToEntities(dtos);

        assertNotNull(entitiesConverted);
        assertTrue(entitiesConverted.isEmpty());
    }

    @Test
    void testConvertEntitiesToDtos() {
        var dtos = Collections.singletonList(createDto(1, "test", "test@mail.com"));
        var entities = Collections.singletonList(createUserEntity(1, "test", "test@mail.com"));

        var dtosConverted = mapper.convertEntitiesToDtos(entities);

        assertNotNull(dtosConverted);
        assertFalse(dtosConverted.isEmpty());

        assertEquals(1, dtosConverted.size());
        assertEquals(1, dtosConverted.get(0).getId());
        assertEquals("test", dtosConverted.get(0).getUsername());
        assertEquals("test@mail.com", dtosConverted.get(0).getEmail());

        assertEquals(dtos, dtosConverted);
    }

    @Test
    void testConvertEntitiesToDtosWithEmptyEntities() {
        List<UserEntity> entities = Collections.emptyList();

        var dtosConverted = mapper.convertEntitiesToDtos(entities);

        assertNotNull(dtosConverted);
        assertTrue(dtosConverted.isEmpty());
    }

    @Test
    void testConvertEntitiesToDtosWithNullEntities() {
        var dtosConverted = mapper.convertEntitiesToDtos(null);

        assertNotNull(dtosConverted);
        assertTrue(dtosConverted.isEmpty());
    }

    private UserEntity createUserEntity(Integer id, String username, String email) {
        var entity = new UserEntity();

        entity.setId(id);
        entity.setEmail(email);
        entity.setUsername(username);

        return entity;
    }

    private UserDto createDto(Integer id, String username, String email) {
        var dto = new UserDto();

        dto.setId(id);
        dto.setUsername(username);
        dto.setEmail(email);

        return dto;
    }
}
