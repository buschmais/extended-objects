package com.buschmais.xo.impl.proxy.example;

import java.util.Map;

import com.buschmais.xo.api.CompositeObject;
import com.buschmais.xo.api.metadata.method.MethodMetadata;
import com.buschmais.xo.api.metadata.method.PrimitivePropertyMethodMetadata;
import com.buschmais.xo.api.metadata.reflection.AnnotatedMethod;
import com.buschmais.xo.api.metadata.reflection.SetPropertyMethod;
import com.buschmais.xo.api.metadata.type.TypeMetadata;
import com.buschmais.xo.impl.SessionContext;
import com.buschmais.xo.impl.proxy.AbstractProxyMethodService;
import com.buschmais.xo.impl.proxy.example.composite.AsMethod;
import com.buschmais.xo.impl.proxy.example.property.PrimitivePropertySetMethod;

public class ExampleProxyMethodService<Entity> extends AbstractProxyMethodService<Map<PrimitivePropertyMethodMetadata<?>, Object>> {

    public ExampleProxyMethodService(Class<?> type, SessionContext<?, Entity, ?, ?, ?, ?, ?, ?, ?> sessionContext) {
        for (TypeMetadata typeMetadata : sessionContext.getMetadataProvider()
            .getRegisteredMetadata()
            .values()) {
            if (typeMetadata.getAnnotatedType()
                .getAnnotatedElement()
                .isAssignableFrom(type)) {
                for (MethodMetadata<?, ?> methodMetadata : typeMetadata.getProperties()) {
                    if (methodMetadata instanceof PrimitivePropertyMethodMetadata<?>) {
                        AnnotatedMethod method = methodMetadata.getAnnotatedMethod();
                        if (method instanceof SetPropertyMethod) {
                            addProxyMethod(new PrimitivePropertySetMethod((PrimitivePropertyMethodMetadata<?>) methodMetadata), method.getAnnotatedElement());
                        }
                    }
                }
            }
        }
        addMethod(new AsMethod(), CompositeObject.class, "as", Class.class);
    }

}
