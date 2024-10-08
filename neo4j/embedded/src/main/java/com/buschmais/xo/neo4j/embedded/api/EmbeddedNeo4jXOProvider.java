package com.buschmais.xo.neo4j.embedded.api;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.buschmais.xo.api.XOException;
import com.buschmais.xo.api.bootstrap.XOUnit;
import com.buschmais.xo.neo4j.embedded.impl.datastore.EmbeddedDatastore;
import com.buschmais.xo.neo4j.embedded.impl.model.EmbeddedLabel;
import com.buschmais.xo.neo4j.embedded.impl.model.EmbeddedRelationshipType;
import com.buschmais.xo.neo4j.spi.metadata.NodeMetadata;
import com.buschmais.xo.neo4j.spi.metadata.RelationshipMetadata;
import com.buschmais.xo.spi.bootstrap.XODatastoreProvider;
import com.buschmais.xo.spi.datastore.Datastore;

import com.google.common.base.CaseFormat;
import org.neo4j.configuration.Config;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.graphdb.config.Setting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.joining;
import static org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME;

public class EmbeddedNeo4jXOProvider
    implements XODatastoreProvider<NodeMetadata<EmbeddedLabel>, EmbeddedLabel, RelationshipMetadata<EmbeddedRelationshipType>, EmbeddedRelationshipType> {

    public static final String PROPERTY_XO_NEO4J_EMBEDDED_PLUGINS = "xo.neo4j.embedded.plugins";

    private static final String NEO4J_PROPERTY_PREFIX = "neo4j.";

    private static final Pattern NEO4J_PROPERTY_PATTERN = Pattern.compile("neo4j\\.(.*)");

    private static final Logger LOG = LoggerFactory.getLogger(EmbeddedNeo4jXOProvider.class);

    @Override
    public Datastore<?, NodeMetadata<EmbeddedLabel>, EmbeddedLabel, RelationshipMetadata<EmbeddedRelationshipType>, EmbeddedRelationshipType> createDatastore(
        XOUnit xoUnit) {
        URI uri = xoUnit.getUri();
        Properties properties = xoUnit.getProperties();
        Map<String, String> neo4jProperties = getNeo4jProperties(properties);
        Config config = Config.newBuilder()
            .setRaw(neo4jProperties)
            .build();
        DatabaseManagementServiceFactory databaseManagementServiceFactory = lookupFactory(uri);
        DatabaseManagementService databaseManagementService = databaseManagementServiceFactory.createDatabaseManagementService(uri, config, properties);
        return new EmbeddedDatastore(databaseManagementService, databaseManagementService.database(DEFAULT_DATABASE_NAME));
    }

    @SuppressWarnings("unchecked")
    DatabaseManagementServiceFactory lookupFactory(URI uri) {
        String factoryClass = getFactoryClassName(uri);
        LOG.debug("try to lookup provider-class {}", factoryClass);

        try {
            return ((Class<? extends DatabaseManagementServiceFactory>) Class.forName(factoryClass)).getDeclaredConstructor()
                .newInstance();
        } catch (ReflectiveOperationException e) {
            throw new XOException("Cannot create datastore factory.", e);
        }
    }

    private String getFactoryClassName(URI uri) {
        String protocol = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, uri.getScheme());
        return DatabaseManagementServiceFactory.class.getPackage()
            .getName() + "." + protocol + DatabaseManagementServiceFactory.class.getSimpleName();
    }

    @Override
    public Class<? extends Enum<? extends ConfigurationProperty>> getConfigurationProperties() {
        return null;
    }

    private Map<String, String> getNeo4jProperties(Properties properties) {
        Map<String, String> neo4jProperties = new HashMap<>();
        for (String propertyName : properties.stringPropertyNames()) {
            Matcher matcher = NEO4J_PROPERTY_PATTERN.matcher(propertyName);
            if (matcher.matches()) {
                String neo4jProperty = matcher.group(1);
                neo4jProperties.put(neo4jProperty, properties.getProperty(propertyName));
            }
        }
        return neo4jProperties;
    }

    public static PropertiesBuilder propertiesBuilder() {
        return new PropertiesBuilder();
    }

    public static class PropertiesBuilder {

        private final Properties properties = new Properties();

        private PropertiesBuilder() {
        }

        public <T> PropertiesBuilder property(Setting<T> setting, T value) {
            if (value instanceof List<?>) {
                return property(setting.name(), ((List<?>) value).stream()
                    .map(Object::toString)
                    .collect(joining(",")));
            }
            return property(setting.name(), value);
        }

        public <T> PropertiesBuilder property(String setting, T value) {
            properties.setProperty(NEO4J_PROPERTY_PREFIX + setting, value.toString());
            return this;
        }

        public Properties build() {
            return properties;
        }
    }
}
