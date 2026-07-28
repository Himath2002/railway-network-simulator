package io.github.himathahangama.railnet.domain.factory.config;

import io.github.himathahangama.railnet.domain.entity.NamedEntity;

public abstract class EntityConfig<T extends NamedEntity> {
    protected final String name;

    protected EntityConfig(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract T create();

    public abstract void validate();
}