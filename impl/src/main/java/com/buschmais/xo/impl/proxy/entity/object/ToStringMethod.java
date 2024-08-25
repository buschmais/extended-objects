package com.buschmais.xo.impl.proxy.entity.object;

import java.util.Set;

import com.buschmais.xo.api.metadata.type.CompositeTypeMetadata;
import com.buschmais.xo.api.metadata.type.DatastoreEntityMetadata;
import com.buschmais.xo.api.metadata.type.EntityTypeMetadata;
import com.buschmais.xo.impl.SessionContext;
import com.buschmais.xo.impl.proxy.common.object.AbstractDatastoreTypeToStringMethod;
import com.buschmais.xo.spi.datastore.DatastoreEntityManager;
import com.buschmais.xo.spi.datastore.DatastorePropertyManager;

public class ToStringMethod<Entity, EntityMetadata extends DatastoreEntityMetadata<EntityDiscriminator>, EntityDiscriminator>
    extends AbstractDatastoreTypeToStringMethod<Entity> {

    private final SessionContext<?, Entity, EntityMetadata, EntityDiscriminator, ?, ?, ?, ?, ?> sessionContext;

    private final DatastoreEntityManager<?, Entity, EntityMetadata, EntityDiscriminator, ?> datastoreEntityManager;

    public ToStringMethod(SessionContext<?, Entity, EntityMetadata, EntityDiscriminator, ?, ?, ?, ?, ?> sessionContext) {
        this.sessionContext = sessionContext;
        this.datastoreEntityManager = sessionContext.getDatastoreSession()
            .getDatastoreEntityManager();
    }

    @Override
    protected String getId(Entity datastoreType) {
        return datastoreEntityManager.getEntityId(datastoreType)
            .toString();
    }

    protected CompositeTypeMetadata<EntityTypeMetadata<EntityMetadata>> getDynamicType(Entity entity) {
        Set<EntityDiscriminator> discriminators = datastoreEntityManager.getEntityDiscriminators(entity);
        return sessionContext.getMetadataProvider()
            .getTypes(discriminators);
    }

    @Override
    protected DatastorePropertyManager<Entity, ?> getDatastorePropertyManager() {
        return datastoreEntityManager;
    }
}
