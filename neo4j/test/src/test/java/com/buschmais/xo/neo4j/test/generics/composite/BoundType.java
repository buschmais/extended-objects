package com.buschmais.xo.neo4j.test.generics.composite;

import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("Bound")
public interface BoundType extends GenericSuperType<String> {
}
