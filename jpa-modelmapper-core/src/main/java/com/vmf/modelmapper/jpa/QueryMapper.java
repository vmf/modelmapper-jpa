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

import java.util.List;

import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

/**
 * Represents a query to be mapped.
 * @param <S> : Source type.
 * @param <D> : Destination type.
 */
public class QueryMapper<S, D> implements MappingQuery<S, D> {

    private final JpaModelMapper mapper;
    private final TypedQuery<S> query;
    private final Class<D> destinationType;

    public QueryMapper(JpaModelMapper mapper,
                       TypedQuery<S> query,
                       Class<D> destinationType) {
        this.mapper = mapper;
        this.query = query;
        this.destinationType = destinationType;
    }

    /**
     * Execute a SELECT query that returns a single mapped result.
     * @return the result.
     */
    @Override
    public D getSingleResult() {
        try {
            S entity = query.getSingleResult();
            return mapper.mapEntity(entity, destinationType);
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Execute a SELECT query and return the mapped query results.
     * as a typed List.
     * @return a list of the results.
     */
    @Override
    public List<D> getResultList() {
        List<S> result = query.getResultList();
        return mapper.mapEntities(result, destinationType);
    }
}
