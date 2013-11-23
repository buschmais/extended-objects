package com.buschmais.cdo.neo4j.impl.node.proxy.method;

import com.buschmais.cdo.api.CdoException;
import com.buschmais.cdo.api.CompositeObject;
import com.buschmais.cdo.neo4j.api.proxy.NodeProxyMethod;
import com.buschmais.cdo.neo4j.impl.common.proxy.method.AbstractProxyMethodService;
import com.buschmais.cdo.neo4j.impl.node.metadata.*;
import com.buschmais.cdo.neo4j.impl.node.InstanceManager;
import com.buschmais.cdo.neo4j.impl.common.proxy.method.composite.AsMethod;
import com.buschmais.cdo.neo4j.impl.node.proxy.method.object.EqualsMethod;
import com.buschmais.cdo.neo4j.impl.node.proxy.method.object.HashCodeMethod;
import com.buschmais.cdo.neo4j.impl.node.proxy.method.object.ToStringMethod;
import com.buschmais.cdo.neo4j.impl.node.proxy.method.property.*;
import com.buschmais.cdo.neo4j.impl.common.reflection.BeanMethod;
import com.buschmais.cdo.neo4j.impl.common.reflection.BeanPropertyMethod;
import com.buschmais.cdo.neo4j.impl.node.proxy.method.resultof.ResultOfMethod;
import com.buschmais.cdo.neo4j.impl.query.QueryExecutor;
import com.buschmais.cdo.neo4j.impl.query.proxy.method.RowProxyMethodService;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class NodeProxyMethodService extends AbstractProxyMethodService<Node, NodeProxyMethod> {

    public NodeProxyMethodService(NodeMetadataProvider nodeMetadataProvider, InstanceManager instanceManager, QueryExecutor queryExecutor) {
        for (NodeMetadata nodeMetadata : nodeMetadataProvider.getRegisteredNodeMetadata()) {
            for (AbstractMethodMetadata methodMetadata : nodeMetadata.getProperties()) {
                BeanMethod beanMethod = methodMetadata.getBeanMethod();
                NodeProxyMethod proxyMethod = null;
                if (methodMetadata instanceof ImplementedByMethodMetadata) {
                    ImplementedByMethodMetadata implementedByMethodMetadata = (ImplementedByMethodMetadata) methodMetadata;
                    Class<? extends NodeProxyMethod> proxyMethodType = implementedByMethodMetadata.getProxyMethodType();
                    try {
                        proxyMethod = proxyMethodType.newInstance();
                    } catch (InstantiationException e) {
                        throw new CdoException("Cannot instantiate proxy method of type " + proxyMethodType.getName(), e);
                    } catch (IllegalAccessException e) {
                        throw new CdoException("Unexpected exception while instantiating type " + proxyMethodType.getName(), e);
                    }
                }
                if (methodMetadata instanceof ResultOfMethodMetadata) {
                    ResultOfMethodMetadata resultOfMethodMetadata = (ResultOfMethodMetadata)methodMetadata;
                    proxyMethod=new ResultOfMethod(resultOfMethodMetadata, instanceManager, queryExecutor);
                }
                if (methodMetadata instanceof AbstractPropertyMethodMetadata) {
                    BeanPropertyMethod beanPropertyMethod = (BeanPropertyMethod) beanMethod;
                    if (methodMetadata instanceof PrimitivePropertyMethodMetadata) {
                        switch (beanPropertyMethod.getMethodType()) {
                            case GETTER:
                                proxyMethod = new PrimitivePropertyGetMethod((PrimitivePropertyMethodMetadata) methodMetadata, instanceManager);
                                break;
                            case SETTER:
                                proxyMethod = new PrimitivePropertySetMethod((PrimitivePropertyMethodMetadata) methodMetadata, instanceManager);
                                break;
                        }
                    } else if (methodMetadata instanceof EnumPropertyMethodMetadata) {
                        switch (beanPropertyMethod.getMethodType()) {
                            case GETTER:
                                proxyMethod = new EnumPropertyGetMethod((EnumPropertyMethodMetadata) methodMetadata, instanceManager);
                                break;
                            case SETTER:
                                proxyMethod = new EnumPropertySetMethod((EnumPropertyMethodMetadata) methodMetadata, instanceManager);
                                break;
                        }
                    } else if (methodMetadata instanceof ReferencePropertyMethodMetadata) {
                        switch (beanPropertyMethod.getMethodType()) {
                            case GETTER:
                                proxyMethod = new ReferencePropertyGetMethod((ReferencePropertyMethodMetadata) methodMetadata, instanceManager);
                                break;
                            case SETTER:
                                proxyMethod = new ReferencePropertySetMethod((ReferencePropertyMethodMetadata) methodMetadata, instanceManager);
                                break;
                        }
                    } else if (methodMetadata instanceof CollectionPropertyMethodMetadata) {
                        switch (beanPropertyMethod.getMethodType()) {
                            case GETTER:
                                proxyMethod = new CollectionPropertyGetMethod((CollectionPropertyMethodMetadata) methodMetadata, instanceManager);
                                break;
                            case SETTER:
                                proxyMethod = new CollectionPropertySetMethod((CollectionPropertyMethodMetadata) methodMetadata, instanceManager);
                                break;
                        }
                    }
                }
                if (proxyMethod == null) {
                    throw new CdoException("Unsupported metadata type " + methodMetadata);
                }
                addProxyMethod(proxyMethod, beanMethod.getMethod());
            }
        }
        addMethod(new AsMethod<Node>(), CompositeObject.class, "as", Class.class);
        addMethod(new HashCodeMethod(), Object.class, "hashCode");
        addMethod(new EqualsMethod(instanceManager), Object.class, "equals", Object.class);
        addMethod(new ToStringMethod(instanceManager), Object.class, "toString");
    }
}