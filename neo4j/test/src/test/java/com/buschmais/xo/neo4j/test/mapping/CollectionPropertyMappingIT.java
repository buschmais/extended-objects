package com.buschmais.xo.neo4j.test.mapping;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.buschmais.xo.api.XOManager;
import com.buschmais.xo.api.bootstrap.XOUnit;
import com.buschmais.xo.neo4j.test.AbstractNeo4JXOManagerIT;
import com.buschmais.xo.neo4j.test.mapping.composite.A;
import com.buschmais.xo.neo4j.test.mapping.composite.B;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class CollectionPropertyMappingIT extends AbstractNeo4JXOManagerIT {

    public CollectionPropertyMappingIT(XOUnit xoUnit) {
        super(xoUnit);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getXOUnits() {
        return xoUnits(A.class, B.class);
    }

    @Test
    public void setProperty() {
        XOManager xoManager = getXOManager();
        xoManager.currentTransaction().begin();
        A a = xoManager.create(A.class);
        B b = xoManager.create(B.class);
        Set<B> setOfB = a.getSetOfB();
        assertThat(setOfB.add(b), equalTo(true));
        assertThat(setOfB.add(b), equalTo(false));
        xoManager.currentTransaction().commit();
        xoManager.currentTransaction().begin();
        assertThat(setOfB.size(), equalTo(1));
        assertThat(setOfB.remove(b), equalTo(true));
        assertThat(setOfB.remove(b), equalTo(false));
        xoManager.currentTransaction().commit();
    }

    @Test
    public void mappedSetProperty() {
        XOManager xoManager = getXOManager();
        xoManager.currentTransaction().begin();
        A a = xoManager.create(A.class);
        B b = xoManager.create(B.class);
        a.getMappedSetOfB().add(b);
        xoManager.currentTransaction().commit();
        xoManager.currentTransaction().begin();
        TestResult result = executeQuery("match (a:A)-[:MAPPED_SET_OF_B]->(b) return b");
        assertThat(result.getColumn("b").size(), equalTo(1));
        assertThat(result.getColumn("b"), hasItem(b));
        xoManager.currentTransaction().commit();
    }

    @Test
    public void listProperty() {
        XOManager xoManager = getXOManager();
        xoManager.currentTransaction().begin();
        A a = xoManager.create(A.class);
        B b = xoManager.create(B.class);
        List<B> listOfB = a.getListOfB();
        assertThat(listOfB.add(b), equalTo(true));
        assertThat(listOfB.add(b), equalTo(true));
        xoManager.currentTransaction().commit();
        xoManager.currentTransaction().begin();
        assertThat(listOfB.size(), equalTo(2));
        assertThat(listOfB.remove(b), equalTo(true));
        assertThat(listOfB.remove(b), equalTo(true));
        assertThat(listOfB.remove(b), equalTo(false));
        xoManager.currentTransaction().commit();
    }

    @Test
    public void listPropertyByIndex() {
        XOManager xoManager = getXOManager();
        xoManager.currentTransaction().begin();
        A a = xoManager.create(A.class);
        B b1 = xoManager.create(B.class);
        B b2 = xoManager.create(B.class);
        List<B> listOfB = a.getListOfB();
        assertThat(listOfB.add(b1), equalTo(true));
        assertThat(listOfB.add(b2), equalTo(true));
        xoManager.currentTransaction().commit();
        xoManager.currentTransaction().begin();
        assertThat(listOfB.size(), equalTo(2));
        assertThat(a.getListOfB(), hasItem(b1));
        assertThat(a.getListOfB(), hasItem(b2));
        xoManager.currentTransaction().commit();
    }

    @Test
    public void mappedListProperty() {
        XOManager xoManager = getXOManager();
        xoManager.currentTransaction().begin();
        A a = xoManager.create(A.class);
        B b = xoManager.create(B.class);
        a.getMappedListOfB().add(b);
        a.getMappedListOfB().add(b);
        xoManager.currentTransaction().commit();
        xoManager.currentTransaction().begin();
        TestResult result = executeQuery("match (a:A)-[:MAPPED_LIST_OF_B]->(b) return b");
        assertThat(result.getColumn("b").size(), equalTo(2));
        assertThat(result.getColumn("b"), hasItem(b));
        xoManager.currentTransaction().commit();
    }
}
