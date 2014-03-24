package com.buschmais.xo.neo4j.test.mapping;

import com.buschmais.xo.api.XOManager;
import com.buschmais.xo.api.bootstrap.XOUnit;
import com.buschmais.xo.neo4j.test.AbstractCdoManagerTest;
import com.buschmais.xo.neo4j.test.mapping.composite.E;
import com.buschmais.xo.neo4j.test.mapping.composite.E2F;
import com.buschmais.xo.neo4j.test.mapping.composite.F;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.net.URISyntaxException;
import java.util.Collection;

import static com.buschmais.xo.api.Query.Result;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class RelationResultOfTest extends AbstractCdoManagerTest {

    private E e;
    private F f1;
    private F f2;

    private E2F e2f1;
    private E2F e2f2;

    public RelationResultOfTest(XOUnit XOUnit) {
        super(XOUnit);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getCdoUnits() throws URISyntaxException {
        return cdoUnits(asList(Database.MEMORY), asList(E.class, F.class, E2F.class));
    }

    @Before
    public void createData() {
        XOManager XOManager = getXOManager();
        XOManager.currentTransaction().begin();
        e = XOManager.create(E.class);
        f1 = XOManager.create(F.class);
        e2f1 = XOManager.create(e, E2F.class, f1);
        e2f1.setValue("E2F1");
        f2 = XOManager.create(F.class);
        e2f2 = XOManager.create(e, E2F.class, f2);
        e2f2.setValue("E2F2");
        XOManager.currentTransaction().commit();
    }

    @Test
    public void resultUsingExplicitQuery() {
        XOManager XOManager = getXOManager();
        XOManager.currentTransaction().begin();
        Result<E2F.ByValue> byValue = e2f1.getResultByValueUsingExplicitQuery("E2F1");
        assertThat(byValue.getSingleResult().getF(), equalTo(f1));
        XOManager.currentTransaction().commit();
    }

    @Test
    public void resultUsingReturnType() {
        XOManager XOManager = getXOManager();
        XOManager.currentTransaction().begin();
        Result<E2F.ByValue> byValue = e2f1.getResultByValueUsingReturnType("E2F1");
        assertThat(byValue.getSingleResult().getF(), equalTo(f1));
        XOManager.currentTransaction().commit();
    }

    @Test
    public void byValueUsingExplicitQuery() {
        XOManager XOManager = getXOManager();
        XOManager.currentTransaction().begin();
        E2F.ByValue byValue = e2f1.getByValueUsingExplicitQuery("E2F1");
        assertThat(byValue.getF(), equalTo(f1));
        XOManager.currentTransaction().commit();
    }

    @Test
    public void byValueUsingReturnType() {
        XOManager XOManager = getXOManager();
        XOManager.currentTransaction().begin();
        E2F.ByValue byValue = e2f1.getByValueUsingReturnType("E2F1");
        assertThat(byValue.getF(), equalTo(f1));
        byValue = e2f1.getByValueUsingReturnType("unknownE2F");
        assertThat(byValue, equalTo(null));
        XOManager.currentTransaction().commit();
    }

    @Test
    public void byValueUsingImplicitThis() {
        XOManager XOManager = getXOManager();
        XOManager.currentTransaction().begin();
        E2F.ByValueUsingImplicitThis byValue = e2f1.getByValueUsingImplicitThis("E2F1");
        assertThat(byValue.getF(), equalTo(f1));
        XOManager.currentTransaction().commit();
    }

    @Test
    public void resultUsingCypher() {
        XOManager XOManager = getXOManager();
        XOManager.currentTransaction().begin();
        Result<F> result = e2f1.getResultUsingCypher("E2F1");
        assertThat(result, hasItems(equalTo(f1)));
        result = e2f1.getResultUsingCypher("unknownF");
        assertThat(result.iterator().hasNext(), equalTo(false));
        XOManager.currentTransaction().commit();
    }

    @Test
    public void singleResultUsingCypher() {
        XOManager XOManager = getXOManager();
        XOManager.currentTransaction().begin();
        F result = e2f1.getSingleResultUsingCypher("E2F1");
        assertThat(result, equalTo(f1));
        result = e2f1.getSingleResultUsingCypher("unknownF");
        assertThat(result, equalTo(null));
        XOManager.currentTransaction().commit();
    }

}