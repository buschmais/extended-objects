package com.buschmais.xo.neo4j.test.relation.typed;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.IsCollectionContaining.hasItems;

import java.util.Collection;

import com.buschmais.xo.api.XOManager;
import com.buschmais.xo.api.bootstrap.XOUnit;
import com.buschmais.xo.neo4j.test.AbstractNeo4JXOManagerIT;
import com.buschmais.xo.neo4j.test.relation.typed.composite.TreeNode;
import com.buschmais.xo.neo4j.test.relation.typed.composite.TreeNodeRelation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class TreeIT extends AbstractNeo4JXOManagerIT {

    public TreeIT(XOUnit xoUnit) {
        super(xoUnit);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getXOUnits() {
        return xoUnits(TreeNode.class, TreeNodeRelation.class);
    }

    @Test
    public void tree() {
        XOManager xoManager = getXOManager();
        xoManager.currentTransaction().begin();
        TreeNode parent = xoManager.create(TreeNode.class);
        parent.setName("parent");
        TreeNode child1 = xoManager.create(TreeNode.class);
        child1.setName("child 1");
        TreeNode child2 = xoManager.create(TreeNode.class);
        child2.setName("child 2");
        TreeNodeRelation relation1 = xoManager.create(parent, TreeNodeRelation.class, child1);
        relation1.setVersion(1);
        TreeNodeRelation relation2 = xoManager.create(parent, TreeNodeRelation.class, child2);
        relation2.setVersion(1);
        xoManager.currentTransaction().commit();
        xoManager.currentTransaction().begin();
        assertThat(parent.getParent(), equalTo(null));
        assertThat(parent.getChildren(), hasItems(relation1, relation2));
        assertThat(relation1.getParent(), equalTo(parent));
        assertThat(relation1.getChild(), equalTo(child1));
        assertThat(relation2.getParent(), equalTo(parent));
        assertThat(relation2.getChild(), equalTo(child2));
        assertThat(child1.getParent(), equalTo(relation1));
        assertThat(child1.getChildren().isEmpty(), equalTo(true));
        assertThat(child2.getParent(), equalTo(relation2));
        assertThat(child2.getChildren().isEmpty(), equalTo(true));
    }
}
