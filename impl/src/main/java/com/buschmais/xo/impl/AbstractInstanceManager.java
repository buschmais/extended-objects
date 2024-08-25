package com.buschmais.xo.impl;

import com.buschmais.xo.api.CompositeObject;
import com.buschmais.xo.api.XOException;
import com.buschmais.xo.api.metadata.type.CompositeTypeMetadata;
import com.buschmais.xo.impl.cache.TransactionalCache;
import com.buschmais.xo.impl.instancelistener.InstanceListenerService;
import com.buschmais.xo.impl.proxy.InstanceInvocationHandler;
import com.buschmais.xo.impl.proxy.ProxyMethodService;
import com.buschmais.xo.spi.session.InstanceManager;

import lombok.RequiredArgsConstructor;

/**
 * Abstract base implementation of an instance manager.
 * <p>
 * It provides functionality to map the lifecycle of proxy instances to their
 * corresponding datastore type.
 * </p>
 *
 * @param <DatastoreId>
 *     The id type of the datastore type.
 * @param <DatastoreType>
 *     The datastore type.
 */
@RequiredArgsConstructor
public abstract class AbstractInstanceManager<DatastoreId, DatastoreType> implements InstanceManager<DatastoreId, DatastoreType> {

    private final TransactionalCache<DatastoreId> cache;
    private final InstanceListenerService instanceListenerService;
    private final ProxyFactory proxyFactory;
    private final ProxyMethodService<DatastoreType> proxyMethodService;

    /**
     * Return the proxy instance which corresponds to the given datastore type for
     * reading.
     *
     * @param datastoreType
     *     The datastore type.
     * @param <T>
     *     The instance type.
     * @return The instance.
     */
    @Override
    public <T> T readInstance(DatastoreType datastoreType) {
        return getInstance(datastoreType, TransactionalCache.Mode.READ);
    }

    /**
     * Return the proxy instance which corresponds to the given datastore type for
     * writing.
     *
     * @param datastoreType
     *     The datastore type.
     * @param types
     *     The {@link CompositeTypeMetadata}.
     * @param <T>
     *     The instance type.
     * @return The instance.
     */
    public <T> T createInstance(DatastoreType datastoreType, CompositeTypeMetadata<?> types) {
        return newInstance(getDatastoreId(datastoreType), datastoreType, types, TransactionalCache.Mode.WRITE);
    }

    @Override
    public <T> T updateInstance(DatastoreType datastoreType) {
        return getInstance(datastoreType, TransactionalCache.Mode.WRITE);
    }

    /**
     * Return the proxy instance which corresponds to the given datastore type.
     *
     * @param datastoreType
     *     The datastore type.
     * @param <T>
     *     The instance type.
     * @return The instance.
     */
    private <T> T getInstance(DatastoreType datastoreType, TransactionalCache.Mode cacheMode) {
        DatastoreId id = getDatastoreId(datastoreType);
        Object instance = cache.get(id, cacheMode);
        if (instance == null) {
            CompositeTypeMetadata<?> types = getTypes(datastoreType);
            instance = newInstance(id, datastoreType, types, cacheMode);
            if (TransactionalCache.Mode.READ.equals(cacheMode)) {
                instanceListenerService.postLoad(instance);
            }
        }
        return (T) instance;
    }

    /**
     * Create a proxy instance which corresponds to the given datastore type.
     *
     * @param datastoreType
     *     The datastore type.
     * @param <T>
     *     The instance type.
     * @return The instance.
     */
    private <T> T newInstance(DatastoreId id, DatastoreType datastoreType, CompositeTypeMetadata<?> types, TransactionalCache.Mode cacheMode) {
        validateType(types);
        InstanceInvocationHandler invocationHandler = new InstanceInvocationHandler(datastoreType, proxyMethodService);
        T instance = proxyFactory.createInstance(invocationHandler, types.getCompositeType());
        cache.put(id, instance, cacheMode);
        return instance;
    }

    /**
     * Validates the given types.
     *
     * @param compositeTypeMetadata
     *     The types.
     */
    private void validateType(CompositeTypeMetadata<?> compositeTypeMetadata) {
        int size = compositeTypeMetadata.getMetadata()
            .size();
        if (size == 1) {
            if (compositeTypeMetadata.isAbstract()) {
                throw new XOException("Cannot create an instance of a single abstract type " + compositeTypeMetadata);
            }
        } else if (compositeTypeMetadata.isFinal()) {
            throw new XOException("Cannot create an instance overriding a final type " + compositeTypeMetadata);
        }
    }

    /**
     * Removes an instance, e.g. before deletion or migration.
     *
     * @param instance
     *     The instance.
     * @param <Instance>
     *     The instance type.
     */
    @Override
    public <Instance> void removeInstance(Instance instance) {
        DatastoreType datastoreType = getDatastoreType(instance);
        DatastoreId id = getDatastoreId(datastoreType);
        cache.remove(id);
    }

    /**
     * Destroys an instance, i.e. makes it unusable-
     *
     * @param instance
     *     The instance.
     * @param <Instance>
     *     The instance type.
     */
    @Override
    public <Instance> void closeInstance(Instance instance) {
        proxyFactory.getInvocationHandler(instance)
            .close();
    }

    /**
     * Determine if a given instance is a datastore type handled by this manager.
     *
     * @param instance
     *     The instance.
     * @param <Instance>
     *     The instance type.
     * @return <code>true</code> if the instance is handled by this manager.
     */
    @Override
    public <Instance> boolean isInstance(Instance instance) {
        if (instance instanceof CompositeObject) {
            Object delegate = ((CompositeObject) instance).getDelegate();
            return isDatastoreType(delegate);
        }
        return false;
    }

    /**
     * Return the datastore type represented by an instance.
     *
     * @param instance
     *     The instance.
     * @param <Instance>
     *     The instance type.
     * @return The corresponding datastore type.
     */
    @Override
    public <Instance> DatastoreType getDatastoreType(Instance instance) {
        InstanceInvocationHandler<DatastoreType> invocationHandler = proxyFactory.getInvocationHandler(instance);
        return invocationHandler.getDatastoreType();
    }

    /**
     * Closes this manager instance.
     */
    @Override
    public void close() {
        for (Object instance : cache.readInstances()) {
            closeInstance(instance);
        }
    }

    /**
     * Determine if a given object is a datastore type.
     *
     * @param o
     *     The object
     * @return <code>true</code> If the given object is a datastore type.
     */
    protected abstract boolean isDatastoreType(Object o);

    /**
     * Determines the {@link CompositeTypeMetadata} of a datastore type.
     *
     * @param datastoreType
     *     The datastore type.
     * @return The {@link CompositeTypeMetadata}.
     */
    protected abstract CompositeTypeMetadata<?> getTypes(DatastoreType datastoreType);

}
