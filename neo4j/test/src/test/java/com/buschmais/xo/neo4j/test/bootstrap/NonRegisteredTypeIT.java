package com.buschmais.xo.neo4j.test.bootstrap;

import java.util.Collection;

import com.buschmais.xo.api.XOException;
import com.buschmais.xo.api.XOManager;
import com.buschmais.xo.api.bootstrap.XOUnit;
import com.buschmais.xo.neo4j.test.AbstractNeo4JXOManagerIT;
import com.buschmais.xo.neo4j.test.bootstrap.composite.A;
import com.buschmais.xo.neo4j.test.bootstrap.composite.B;
import com.buschmais.xo.neo4j.test.bootstrap.composite.B2B;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class NonRegisteredTypeIT extends AbstractNeo4JXOManagerIT {

    public NonRegisteredTypeIT(XOUnit xoUnit) {
        super(xoUnit);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getXOUnits() {
        return xoUnits(B.class);
    }

    @Test
    public void entity() {
        XOManager xoManager = getXOManager();
        xoManager.currentTransaction()
            .begin();
        try {
            xoManager.create(A.class);
            fail("Expecting an " + XOException.class.getName());
        } catch (XOException e) {
            assertThat(e.getMessage()).as("Exception message must contain name of non-registered type.")
                .contains(A.class.getName());
        }
        xoManager.currentTransaction()
            .commit();
    }

    @Test
    public void relation() {
        XOManager xoManager = getXOManager();
        xoManager.currentTransaction()
            .begin();
        B b1 = xoManager.create(B.class);
        B b2 = xoManager.create(B.class);
        try {
            xoManager.create(b1, B2B.class, b2);
            fail("Expecting an " + XOException.class.getName());
        } catch (XOException e) {
            assertThat(e.getMessage()).as("Exception message must contain name of non-registered type.")
                .contains(B2B.class.getName());
        }
        xoManager.currentTransaction()
            .commit();
    }

}
