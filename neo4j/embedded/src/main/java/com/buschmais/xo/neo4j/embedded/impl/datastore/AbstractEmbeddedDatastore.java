package com.buschmais.xo.neo4j.embedded.impl.datastore;

import com.buschmais.xo.neo4j.embedded.impl.model.EmbeddedLabel;
import com.buschmais.xo.neo4j.embedded.impl.model.EmbeddedRelationshipType;
import com.buschmais.xo.neo4j.spi.AbstractNeo4jDatastore;
import com.buschmais.xo.neo4j.spi.AbstractNeo4jMetadataFactory;
import com.buschmais.xo.neo4j.spi.metadata.NodeMetadata;
import com.buschmais.xo.neo4j.spi.metadata.RelationshipMetadata;
import com.buschmais.xo.spi.datastore.DatastoreMetadataFactory;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.RelationshipType;

/**
 * Abstract base implementation for embedded graph stores.
 */
public abstract class AbstractEmbeddedDatastore extends AbstractNeo4jDatastore<EmbeddedLabel, EmbeddedRelationshipType, EmbeddedDatastoreSessionImpl> {

    protected final GraphDatabaseService graphDatabaseService;

    /**
     * Constructor.
     *
     * @param graphDatabaseService
     *            The graph database service.
     */
    public AbstractEmbeddedDatastore(GraphDatabaseService graphDatabaseService) {
        this.graphDatabaseService = graphDatabaseService;
    }

    @Override
    public DatastoreMetadataFactory<NodeMetadata<EmbeddedLabel>, EmbeddedLabel, RelationshipMetadata<EmbeddedRelationshipType>, EmbeddedRelationshipType> getMetadataFactory() {
        return new AbstractNeo4jMetadataFactory<EmbeddedLabel, EmbeddedRelationshipType>() {

            protected EmbeddedLabel createLabel(String value) {
                return new EmbeddedLabel(value);
            }

            protected EmbeddedRelationshipType createRelationshipType(String name) {
                return new EmbeddedRelationshipType(RelationshipType.withName(name));
            }

            @Override
            protected boolean isBatchableDefault() {
                return false;
            }
        };
    }

    @Override
    public EmbeddedDatastoreSessionImpl createSession() {
        return new EmbeddedDatastoreSessionImpl(graphDatabaseService);
    }

    public GraphDatabaseService getGraphDatabaseService() {
        return graphDatabaseService;
    }
}
