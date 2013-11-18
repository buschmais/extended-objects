package com.buschmais.cdo.neo4j.test.demo.composite;

import com.buschmais.cdo.neo4j.api.annotation.Label;

@Label("Person")
public interface Person {

    String getName();

    void setName(String name);
}
