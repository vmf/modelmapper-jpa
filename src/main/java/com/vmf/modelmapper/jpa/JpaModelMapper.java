/**
 * Copyright 2018 Vinícius M. Freitas.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vmf.modelmapper.jpa;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.modelmapper.internal.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represents a model mapper which solves common JPA situations.
 */
public class JpaModelMapper {

    private ModelMapper modelMapper;
    private EntityManager em;

    /**
     * Creates a new JpaModelMapper.
     * @param em used to execute the SELECT query.
     */
    public JpaModelMapper(EntityManager em) {
        this(em, null);
    }

    /**
     * Creates a new JpaModelMapper.
     * @param em used to execute the SELECT query.
     * @param modelMapper uses this model mapper instance instead of creating a new one.
     */
    public JpaModelMapper(EntityManager em,
                          ModelMapper modelMapper) {
        this.em = em;
        this.modelMapper = modelMapper;
    }

    private <S, D> ModelMapper buildMapper(Class<S> sourceType, Class<D> destinationType) {
        if (modelMapper == null)
            modelMapper = new ModelMapper();

        createJpaLazyAsNullTypeMap(sourceType, destinationType);
        return modelMapper;
    }

    private <S, D> TypeMap<S, D> createJpaLazyAsNullTypeMap(Class<S> sourceType, Class<D> destinationType) {
        TypeMap<S, D> typeMap = modelMapper.getTypeMap(sourceType, destinationType);
        if (typeMap != null)
            return typeMap;

        return modelMapper.createTypeMap(sourceType, destinationType).setCondition(new JpaLazyAsNullCondition(em));
    }

    /**
     * If present, maps an entity to an instance of {@code destinationType}.
     * @param source possible object to map from.
     * @param destinationType type to map to.
     * @param <D> destination type.
     * @return If present, fully mapped instance of {@code destinationType}, otherwise null.
     */
    public <D> D mapEntity(Optional<?> source, Class<D> destinationType) {
        if (!source.isPresent())
            return null;
        return mapEntity(source.get(), destinationType);
    }

    /**
     * Maps {@code entity} to an instance of {@code destinationType}.
     * @param entity object to map from.
     * @param destinationType type to map to.
     * @param <D> destination type.
     * @return fully mapped instance of {@code destinationType}.
     */
    public <D> D mapEntity(Object entity, Class<D> destinationType) {
        Assert.notNull(entity, "source");
        Assert.notNull(destinationType, "destinationType");
        return buildMapper(entity.getClass(), destinationType)
                .map(entity, destinationType);
    }

    /**
     * Maps {@code entity} to {@code destination}.
     * @param entity object to map from.
     * @param destination object to map to.
     */
    public void mapEntity(Object entity, Object destination) {
        Assert.notNull(entity, "source");
        Assert.notNull(destination, "destination");
        buildMapper(entity.getClass(), destination.getClass())
                .map(entity, destination);
    }

    /**
     * Maps {@code entities} to a list of {@code destinationType}.
     * @param entities object to map from.
     * @param destinationType type to map to.
     * @param <S> source type.
     * @param <D> destination type.
     * @return a mapped list.
     */
    public <S, D> List<D> mapEntities(List<S> entities, Class<D> destinationType) {
        Assert.notNull(entities, "source");
        Assert.notNull(destinationType, "destinationType");
        ModelMapper mapper = buildMapper(entities.getClass(), destinationType.getClass());
        return entities
                .stream()
                .map(entity -> mapper.map(entity, destinationType))
                .collect(Collectors.toList());
    }

    /**
     * Creates a mapping query from {@code query} with mapping options to instance of {@code destinationType}.
     * @param query to be used as a source object to map from.
     * @param destinationType type to map to.
     * @param <S> source type.
     * @param <D> destination type.
     * @return mapping query.
     */
    public <S, D> MappingQuery<S,D> mapQuery(TypedQuery<S> query, Class<D> destinationType) {
        Assert.notNull(query, "query");
        Assert.notNull(destinationType, "destinationType");
        return new QueryMapper<>(this, query, destinationType);
    }
}