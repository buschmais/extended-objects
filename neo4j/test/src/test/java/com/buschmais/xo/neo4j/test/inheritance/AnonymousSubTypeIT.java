package com.buschmais.xo.neo4j.test.inheritance;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Collection;

import com.buschmais.xo.api.XOManager;
import com.buschmais.xo.api.bootstrap.XOUnit;
import com.buschmais.xo.neo4j.test.AbstractNeo4JXOManagerIT;
import com.buschmais.xo.neo4j.test.inheritance.composite.A;
import com.buschmais.xo.neo4j.test.inheritance.composite.D;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class AnonymousSubTypeIT extends AbstractNeo4JXOManagerIT {

    public AnonymousSubTypeIT(XOUnit xoUnit) {
        super(xoUnit);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getXOUnits() {
        return xoUnits(D.class);
    }

    @Test
    public void anonymousSubType() {
        XOManager xoManager = getXOManager();
        xoManager.currentTransaction().begin();
        D b = xoManager.create(D.class);
        b.setIndex("1");
        xoManager.currentTransaction().commit();
        closeXOmanager();
        xoManager = getXOManager();
        xoManager.currentTransaction().begin();
        A a = xoManager.find(A.class, "1").iterator().next();
        assertThat(a.getIndex(), equalTo("1"));
        xoManager.currentTransaction().commit();
    }

}
