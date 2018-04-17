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

import org.modelmapper.Condition;
import org.modelmapper.MappingException;
import org.modelmapper.internal.Errors;
import org.modelmapper.spi.MappingContext;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceUnitUtil;
import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * Represents a condition built specifically to solve JPA Lazy Load situations.
 * @param <S> : Source type.
 * @param <D> : Destination type.
 */
public class JpaLazyAsNullCondition<S, D> implements Condition<S, D> {

    private EntityManager em;

    public JpaLazyAsNullCondition(EntityManager em) {
        this.em = em;
    }

    @Override
    public boolean applies(MappingContext<S, D> context) {
        PersistenceUnitUtil unitUtil = em.getEntityManagerFactory().getPersistenceUnitUtil();
        Object source = context.getSource();
        setNullForAllLazyLoadEntities(source, unitUtil);
        return true;
    }

    private void setNullForAllLazyLoadEntities(Object source, PersistenceUnitUtil unitUtil) {
        for (Field field : source.getClass().getDeclaredFields()) {
            field.setAccessible(true);

            try {
                Object fieldValue = field.get(source);
                if (!unitUtil.isLoaded(fieldValue)) {
                    fieldValue = null;
                    field.set(source, fieldValue);
                }

                if (fieldValue != null) {
                    boolean isEntity = Arrays.asList(field.getType().getAnnotations())
                            .stream()
                            .filter(x -> x.annotationType().equals(Entity.class))
                            .count() > 0;

                    if (isEntity) {
                        setNullForAllLazyLoadEntities(fieldValue, unitUtil);
                    }
                }
            } catch (IllegalAccessException e) {
                Errors errors = new Errors();
                errors.addMessage(e, "Failed to map field %s ", field.getType());
                throw new MappingException(errors.getMessages());
            }

        }
    }
}