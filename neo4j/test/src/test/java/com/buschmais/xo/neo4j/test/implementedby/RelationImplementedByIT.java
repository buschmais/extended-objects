package com.buschmais.xo.neo4j.test.implementedby;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assume.assumeThat;

import java.util.Collection;

import com.buschmais.xo.api.XOManager;
import com.buschmais.xo.api.bootstrap.XOUnit;
import com.buschmais.xo.neo4j.test.AbstractNeo4JXOManagerIT;
import com.buschmais.xo.neo4j.test.Neo4jDatabase;
import com.buschmais.xo.neo4j.test.implementedby.composite.A;
import com.buschmais.xo.neo4j.test.implementedby.composite.A2B;
import com.buschmais.xo.neo4j.test.implementedby.composite.B;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class RelationImplementedByIT extends AbstractNeo4JXOManagerIT {

    public RelationImplementedByIT(XOUnit xoUnit) {
        super(xoUnit);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getXOUnits() {
        return xoUnits(A.class, B.class, A2B.class);
    }

    @Test
    public void nonPropertyMethod() {
        XOManager xoManager = getXOManagerFactory().createXOManager();
        xoManager.currentTransaction().begin();
        A a = xoManager.create(A.class);
        B b = xoManager.create(B.class);
        A2B a2b = xoManager.create(a, A2B.class, b);
        a2b.setValue(1);
        int i = a2b.incrementValue();
        assertThat(i, equalTo(2));
        xoManager.currentTransaction().commit();
        xoManager.close();
    }

    @Test
    public void propertyMethods() {
        assumeThat(getXOManagerFactory().getXOUnit().getProvider(), equalTo(Neo4jDatabase.MEMORY.getProvider()));
        XOManager xoManager = getXOManagerFactory().createXOManager();
        xoManager.currentTransaction().begin();
        A a = xoManager.create(A.class);
        B b = xoManager.create(B.class);
        A2B a2b = xoManager.create(a, A2B.class, b);
        a2b.setCustomValue("VALUE");
        String value = a2b.getCustomValue();
        assertThat(value, equalTo("set_VALUE_get"));
        xoManager.currentTransaction().commit();
        xoManager.close();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void unsupportedOperation() {
        XOManager xoManager = getXOManagerFactory().createXOManager();
        xoManager.currentTransaction().begin();
        A a = xoManager.create(A.class);
        B b = xoManager.create(B.class);
        A2B a2b = xoManager.create(a, A2B.class, b);
        xoManager.currentTransaction().commit();
        xoManager.currentTransaction().begin();
        try {
            a2b.unsupportedOperation();
        } finally {
            xoManager.currentTransaction().commit();
        }
    }
}
