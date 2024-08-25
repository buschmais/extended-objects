package com.buschmais.xo.api.metadata.method;

import com.buschmais.xo.api.metadata.reflection.PropertyMethod;
import com.buschmais.xo.api.metadata.type.RelationTypeMetadata;

import static com.buschmais.xo.api.metadata.type.RelationTypeMetadata.Direction;

public class EntityCollectionPropertyMethodMetadata<DatastoreMetadata> extends AbstractEntityRelationMethodMetadata<DatastoreMetadata> {

    public EntityCollectionPropertyMethodMetadata(PropertyMethod propertyMethod, RelationTypeMetadata relationshipType, Direction direction,
        Class<?> elementType, DatastoreMetadata datastoreMetadata) {
        super(propertyMethod, relationshipType, direction, elementType, datastoreMetadata);
    }

}
