package com.buschmais.xo.neo4j.test.generics.composite;

import com.buschmais.xo.neo4j.api.annotation.Label;

@Label
public interface StringValueContainer extends ValueContainer<StringValue>, StringLabel {

}
