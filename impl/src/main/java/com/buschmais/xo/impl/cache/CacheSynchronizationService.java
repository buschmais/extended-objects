package com.buschmais.xo.impl.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import com.buschmais.xo.api.ValidationMode;
import com.buschmais.xo.api.bootstrap.XOUnit;
import com.buschmais.xo.impl.AbstractInstanceManager;
import com.buschmais.xo.impl.SessionContext;
import com.buschmais.xo.impl.instancelistener.InstanceListenerService;
import com.buschmais.xo.spi.datastore.DatastorePropertyManager;
import com.buschmais.xo.spi.datastore.DatastoreSession;

public class CacheSynchronizationService<Entity, Relation> {

    private final SessionContext<?, Entity, ?, ?, ?, Relation, ?, ?, ?> sessionContext;
    private final XOUnit xoUnit;

    public CacheSynchronizationService(SessionContext<?, Entity, ?, ?, ?, Relation, ?, ?, ?> sessionContext, XOUnit xoUnit) {
        this.sessionContext = sessionContext;
        this.xoUnit = xoUnit;
    }

    public void flush() {
        DatastoreSession<?, Entity, ?, ?, ?, Relation, ?, ?, ?> datastoreSession = sessionContext.getDatastoreSession();
        InstanceListenerService instanceListenerService = sessionContext.getInstanceListenerService();
        flush(sessionContext.getRelationCache(), sessionContext.getRelationInstanceManager(), datastoreSession.getDatastoreRelationManager(),
            instanceListenerService);
        flush(sessionContext.getEntityCache(), sessionContext.getEntityInstanceManager(), datastoreSession.getDatastoreEntityManager(),
            instanceListenerService);
    }

    private <T> void flush(TransactionalCache<?> cache, AbstractInstanceManager<?, T> instanceManager, DatastorePropertyManager<T, ?> datastoreManager,
        InstanceListenerService instanceListenerService) {
        Collection<?> writtenInstances = cache.writtenInstances();
        if (!writtenInstances.isEmpty()) {
            List<T> entities = new ArrayList<>(writtenInstances.size());
            for (Object instance : writtenInstances) {
                T entity = instanceManager.getDatastoreType(instance);
                entities.add(entity);
                instanceListenerService.preUpdate(instance);
                validateInstance(instance);
                instanceListenerService.postUpdate(instance);
            }
            datastoreManager.flush(entities);
            cache.flush();
        }
    }

    public void clear() {
        DatastoreSession<?, Entity, ?, ?, ?, Relation, ?, ?, ?> datastoreSession = sessionContext.getDatastoreSession();
        clear(sessionContext.getRelationCache(), sessionContext.getRelationInstanceManager(), datastoreSession.getDatastoreRelationManager());
        clear(sessionContext.getEntityCache(), sessionContext.getEntityInstanceManager(), datastoreSession.getDatastoreEntityManager());
    }

    private <T> void clear(TransactionalCache<?> cache, AbstractInstanceManager<?, T> instanceManager, DatastorePropertyManager<T, ?> datastoreManager) {
        Collection<?> instances = cache.readInstances();
        for (Object instance : instances) {
            T datastoreType = instanceManager.getDatastoreType(instance);
            if (datastoreType != null) {
                datastoreManager.afterCompletion(datastoreType, xoUnit.isClearAfterCompletion());
            }
        }
        cache.clear();
    }

    private void validateInstance(Object instance) {
        if (!ValidationMode.NONE.equals(xoUnit.getValidationMode())) {
            Set<ConstraintViolation<Object>> constraintViolations = sessionContext.getInstanceValidationService()
                .validate(instance);
            if (!constraintViolations.isEmpty()) {
                throw new ConstraintViolationException(constraintViolations);
            }
        }
    }
}
