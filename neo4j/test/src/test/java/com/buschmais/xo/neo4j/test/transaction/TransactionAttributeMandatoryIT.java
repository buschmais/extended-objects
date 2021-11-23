package com.buschmais.xo.neo4j.test.transaction;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.buschmais.xo.api.*;
import com.buschmais.xo.api.bootstrap.XOUnit;
import com.buschmais.xo.neo4j.test.AbstractNeo4JXOManagerIT;
import com.buschmais.xo.neo4j.test.Neo4jDatabase;
import com.buschmais.xo.neo4j.test.transaction.composite.A;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class TransactionAttributeMandatoryIT extends AbstractNeo4JXOManagerIT {

    public TransactionAttributeMandatoryIT(XOUnit xoUnit) {
        super(xoUnit);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getXOUnits() {
        return xoUnits(asList(Neo4jDatabase.MEMORY, Neo4jDatabase.BOLT), Arrays.asList(A.class), Collections.<Class<?>> emptyList(), ValidationMode.AUTO,
                ConcurrencyMode.SINGLETHREADED, Transaction.TransactionAttribute.MANDATORY);
    }

    @Test
    public void withoutTransactionContext() {
        XOManager xoManager = getXOManager();
        xoManager.currentTransaction().begin();
        A a = xoManager.create(A.class);
        a.setValue("value1");
        xoManager.currentTransaction().commit();
        assertThat(xoManager.currentTransaction().isActive(), equalTo(false));
        try {
            a.getValue();
            Assert.fail("A XOException is expected.");
        } catch (XOException e) {
        }
        try {
            a.setValue("value2");
            Assert.fail("A XOException is expected.");
        } catch (XOException e) {
        }
    }
}
