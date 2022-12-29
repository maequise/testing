package org.maequise.services.mappers;

import java.util.List;

public interface Mapper<E, D> {
    E convertDtoToEntity(D dto);

    D convertEntityToDto(E entity);

    List<E> convertDtoToEntities(List<D> dtos);

    List<D> convertEntitiesToDtos(List<E> entities);
}
