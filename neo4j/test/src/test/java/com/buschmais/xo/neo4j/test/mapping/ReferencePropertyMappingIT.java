package com.buschmais.xo.neo4j.test.mapping;

import java.util.Collection;

import com.buschmais.xo.api.XOManager;
import com.buschmais.xo.api.bootstrap.XOUnit;
import com.buschmais.xo.neo4j.test.AbstractNeo4JXOManagerIT;
import com.buschmais.xo.neo4j.test.mapping.composite.A;
import com.buschmais.xo.neo4j.test.mapping.composite.B;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class ReferencePropertyMappingIT extends AbstractNeo4JXOManagerIT {

    public ReferencePropertyMappingIT(XOUnit xoUnit) {
        super(xoUnit);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getXOUnits() {
        return xoUnits(A.class, B.class);
    }

    @Test
    public void referenceProperty() {
        XOManager xoManager = getXOManager();
        xoManager.currentTransaction()
            .begin();
        A a = xoManager.create(A.class);
        B b1 = xoManager.create(B.class);
        B b2 = xoManager.create(B.class);
        a.setB(b1);
        xoManager.currentTransaction()
            .commit();
        xoManager.currentTransaction()
            .begin();
        assertThat(a.getB()).isEqualTo(b1);
        a.setB(b2);
        xoManager.currentTransaction()
            .commit();
        xoManager.currentTransaction()
            .begin();
        assertThat(a.getB()).isEqualTo(b2);
        a.setB(null);
        xoManager.currentTransaction()
            .commit();
        xoManager.currentTransaction()
            .begin();
        assertThat(a.getB()).isNull();
        xoManager.currentTransaction()
            .commit();
    }

    @Test
    public void mappedReferenceProperty() {
        XOManager xoManager = getXOManager();
        xoManager.currentTransaction()
            .begin();
        A a = xoManager.create(A.class);
        B b = xoManager.create(B.class);
        a.setMappedB(b);
        xoManager.currentTransaction()
            .commit();
        xoManager.currentTransaction()
            .begin();
        TestResult result = executeQuery("match (a:A)-[:MAPPED_B]->(b) return b");
        assertThat(result.getColumn("b")).contains(b);
        xoManager.currentTransaction()
            .commit();
    }

}
