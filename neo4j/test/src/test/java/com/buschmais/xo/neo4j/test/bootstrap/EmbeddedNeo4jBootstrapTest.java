package com.buschmais.xo.neo4j.test.bootstrap;

import com.buschmais.xo.api.XOManager;
import com.buschmais.xo.api.XOManagerFactory;
import com.buschmais.xo.api.bootstrap.XO;
import com.buschmais.xo.neo4j.embedded.api.EmbeddedNeo4jDatastoreSession;
import com.buschmais.xo.neo4j.test.bootstrap.composite.A;
import com.buschmais.xo.spi.datastore.DatastoreSession;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EmbeddedNeo4jBootstrapTest {

    @Test
    public void bootstrap() {
        XOManagerFactory xoManagerFactory = XO.createXOManagerFactory("Neo4jEmbedded");
        assertThat(xoManagerFactory).isNotNull();
        XOManager xoManager = xoManagerFactory.createXOManager();
        assertThat(xoManager.getDatastoreSession(DatastoreSession.class)).isInstanceOf(EmbeddedNeo4jDatastoreSession.class);
        xoManager.currentTransaction()
            .begin();
        A a = xoManager.create(A.class);
        a.setName("Test");
        xoManager.currentTransaction()
            .commit();
        xoManager.close();
        xoManagerFactory.close();
    }

}
