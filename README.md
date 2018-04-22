# JPA Model Mapper

JPA Model Mapper aims to solve common JPA mapping issues and also issues when mapping entities to DTOs using [Model Mapper](http://modelmapper.org/).

## The LazyInitializationException issue:
Its crucial for performance to declare entities as lazy load so we don't need to fetch all related entities every time we need some data. But this technique leads to some issues. 
The most common one is the LazyInitializationException that can be pretty annoying sometimes. Most of the time we would just want a null object for a not loaded entity instead of an object that throws an exception if accessed. The examples below show how to solve the LazyInitializationException issue using JPA Model Mapper.  

#### Remapping an entity setting null for all not loaded entities: 
```
TypedQuery<SystemEntity> query =
        em.createQuery("select s from SystemEntity s where s.id = 1",  SystemEntity.class);

SystemEntity system = query.getSingleResult();
return new JpaModelMapper(em).mapEntity(system, SystemEntity.class);
```
#### Remapping an entity to a DTO setting null for all not loaded entities:
```
TypedQuery<SystemEntity> query =
        em.createQuery("select s from SystemEntity s where s.id = 1",  SystemEntity.class);

SystemEntity system = query.getSingleResult();
return new JpaModelMapper(em).mapEntity(system, SystemDTO.class);
```

#### Remapping a list of entities setting null for all not loaded entities: 
```
TypedQuery<SystemEntity> query =
        em.createQuery("select s from SystemEntity s join fetch s.monitoring", SystemEntity.class);

List<SystemEntity> systems = query.getResultList();
return new JpaModelMapper(em).mapEntities(systems, SystemEntity.class);
```

#### Remapping a list of entities to a list of DTOs setting null for all not loaded entities:
```
TypedQuery<SystemEntity> query =
        em.createQuery("select s from SystemEntity s join fetch s.monitoring", SystemEntity.class);

List<SystemEntity> systems = query.getResultList();
return new JpaModelMapper(em).mapEntities(systems, SystemDTO.class);
```

Additionally, the library comes with a MappingQuery feature which simplifies the process by mapping a query instead of its result. The examples below show this functionality.

#### Remapping a query setting null for all not loaded entities (Single result):
```
TypedQuery<SystemEntity> query =
        em.createQuery("select s from SystemEntity s where s.id = 1", SystemEntity.class);

return new JpaModelMapper(em).mapQuery(query, SystemEntity.class).getSingleResult();
```
The ``getSingleResult`` in [`MappingQuery.java`](src/main/java/com/vmf/modelmapper/jpa/MappingQuery.java) method has an exclusive feature. It calls the entity manager's [getSingleResult()](https://docs.oracle.com/javaee/6/api/javax/persistence/Query.html#getSingleResult()) method as expected but it does not throw the [NoResultException](https://docs.jboss.org/hibernate/jpa/2.1/api/javax/persistence/NoResultException.html) if there is no result to return. Instead, it returns null if there is no result. 

#### Remapping a query setting null for all not loaded entities (Multiple results):
```
TypedQuery<SystemEntity> query =
        em.createQuery("select s from SystemEntity s", SystemEntity.class);

return new JpaModelMapper(em).mapQuery(query, SystemEntity.class).getResultList();
```

##### Obs: Of course this works for DTOs the same way as well.