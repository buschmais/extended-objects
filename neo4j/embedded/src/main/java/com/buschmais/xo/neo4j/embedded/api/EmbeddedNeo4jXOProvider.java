package com.buschmais.xo.neo4j.embedded.api;

import java.net.MalformedURLException;
import java.net.URI;

import com.buschmais.xo.api.XOException;
import com.buschmais.xo.api.bootstrap.XOUnit;
import com.buschmais.xo.spi.bootstrap.XODatastoreProvider;
import com.buschmais.xo.spi.datastore.Datastore;

import com.google.common.base.CaseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmbeddedNeo4jXOProvider implements XODatastoreProvider {

    private static final Logger LOG = LoggerFactory.getLogger(EmbeddedNeo4jXOProvider.class);

    @Override
    public Datastore<?, ?, ?, ?, ?> createDatastore(XOUnit xoUnit) {
        URI uri = xoUnit.getUri();
        DatastoreFactory datastoreFactory = lookupFactory(uri);
        try {
            return datastoreFactory.createGraphDatabaseService(uri, xoUnit.getProperties());
        } catch (MalformedURLException e) {
            throw new XOException("Cannot create datastore.", e);
        }
    }

    @SuppressWarnings("unchecked")
    DatastoreFactory lookupFactory(URI uri) {
        String factoryClass = getFactoryClassName(uri);
        LOG.debug("try to lookup provider-class {}", factoryClass);

        try {
            return ((Class<? extends DatastoreFactory>) Class.forName(factoryClass)).getDeclaredConstructor()
                .newInstance();
        } catch (ReflectiveOperationException e) {
            throw new XOException("Cannot create datastore factory.", e);
        }
    }

    private String getFactoryClassName(URI uri) {
        String protocol = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, uri.getScheme());
        return DatastoreFactory.class.getPackage().getName() + "." + protocol + "DatastoreFactory";
    }

    @Override
    public Class<? extends Enum<? extends ConfigurationProperty>> getConfigurationProperties() {
        return null;
    }

}
