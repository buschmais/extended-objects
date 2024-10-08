package com.buschmais.xo.neo4j.test.implementedby;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assume.assumeThat;

@RunWith(Parameterized.class)
public class EntityImplementedByIT extends AbstractNeo4JXOManagerIT {

    public EntityImplementedByIT(XOUnit xoUnit) {
        super(xoUnit);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getXOUnits() {
        return xoUnits(A.class, B.class, A2B.class);
    }

    @Test
    public void nonPropertyMethod() {
        XOManager xoManager = getXOManagerFactory().createXOManager();
        xoManager.currentTransaction()
            .begin();
        A a = xoManager.create(A.class);
        a.setValue(1);
        int i = a.incrementValue();
        assertThat(i).isEqualTo(2);
        xoManager.currentTransaction()
            .commit();
        xoManager.close();
    }

    @Test
    public void propertyMethods() {
        assumeThat(getXOManagerFactory().getXOUnit()
            .getProvider(), equalTo(Neo4jDatabase.MEMORY.getProvider()));
        XOManager xoManager = getXOManagerFactory().createXOManager();
        xoManager.currentTransaction()
            .begin();
        A a = xoManager.create(A.class);
        a.setCustomValue("VALUE");
        String value = a.getCustomValue();
        assertThat(value).isEqualTo("set_VALUE_get");
        xoManager.currentTransaction()
            .commit();
        xoManager.close();
    }

    @Test
    public void compareTo() {
        XOManager xoManager = getXOManagerFactory().createXOManager();
        xoManager.currentTransaction()
            .begin();
        A a1 = xoManager.create(A.class);
        a1.setValue(100);
        A a2 = xoManager.create(A.class);
        a2.setValue(200);
        xoManager.currentTransaction()
            .commit();
        xoManager.currentTransaction()
            .begin();
        assertThat(a1.compareTo(a2)).isEqualTo(-100);
        xoManager.currentTransaction()
            .commit();
        xoManager.close();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void unsupportedOperation() {
        XOManager xoManager = getXOManagerFactory().createXOManager();
        xoManager.currentTransaction()
            .begin();
        A a = xoManager.create(A.class);
        xoManager.currentTransaction()
            .commit();
        xoManager.currentTransaction()
            .begin();
        try {
            a.unsupportedOperation();
        } finally {
            xoManager.currentTransaction()
                .commit();
        }
    }
}
