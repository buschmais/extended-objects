package com.buschmais.xo.impl.proxy.collection;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.buschmais.xo.api.metadata.method.EntityCollectionPropertyMethodMetadata;
import com.buschmais.xo.impl.SessionContext;
import com.buschmais.xo.spi.session.InstanceManager;

public class EntityCollectionProxy<Instance, Entity, Relation>
    extends AbstractCollectionProxy<Instance, Entity, Relation, EntityCollectionPropertyMethodMetadata<?>> implements Collection<Instance> {

    public EntityCollectionProxy(SessionContext<?, Entity, ?, ?, ?, Relation, ?, ?, ?> sessionContext, Entity entity,
        EntityCollectionPropertyMethodMetadata<?> metadata) {
        super(sessionContext, entity, metadata);
    }

    public Iterator<Instance> iterator() {
        final SessionContext<?, Entity, ?, ?, ?, Relation, ?, ?, ?> sessionContext = getSessionContext();
        final EntityCollectionPropertyMethodMetadata<?> metadata = getMetadata();
        final Iterator<Entity> iterator = sessionContext.getEntityPropertyManager()
            .getEntityCollection(getEntity(), metadata);
        return sessionContext.getInterceptorFactory()
            .addInterceptor(new Iterator<Instance>() {

                private Instance instance = null;

                @Override
                public boolean hasNext() {
                    while (!isInstance() && iterator.hasNext()) {
                        instance = sessionContext.getEntityInstanceManager()
                            .readInstance(iterator.next());
                    }
                    return isInstance();
                }

                private boolean isInstance() {
                    return instance != null && metadata.getElementType()
                        .isAssignableFrom(instance.getClass());
                }

                @Override
                public Instance next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    Instance next = instance;
                    instance = null;
                    return next;
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException("Remove not supported");
                }
            }, Iterator.class);
    }

    @Override
    public boolean add(Instance instance) {
        getSessionContext().getEntityPropertyManager()
            .createEntityReference(getEntity(), getMetadata(), instance);
        return true;
    }

    @Override
    public boolean remove(Object o) {
        SessionContext<?, Entity, ?, ?, ?, Relation, ?, ?, ?> sessionContext = getSessionContext();
        InstanceManager<?, Entity> instanceManager = sessionContext.getEntityInstanceManager();
        if (instanceManager.isInstance(o)) {
            return sessionContext.getEntityPropertyManager()
                .removeEntityReference(getEntity(), getMetadata(), o);
        }
        return false;
    }
}
