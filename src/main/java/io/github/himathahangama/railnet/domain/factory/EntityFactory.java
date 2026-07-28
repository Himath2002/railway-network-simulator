package io.github.himathahangama.railnet.domain.factory;

import io.github.himathahangama.railnet.domain.entity.NamedEntity;
import io.github.himathahangama.railnet.domain.factory.config.EntityConfig;

public interface EntityFactory<T extends NamedEntity, C extends EntityConfig<T>> {
    T create(C config);
}
