package io.github.himathahangama.railnet.network;

import io.github.himathahangama.railnet.domain.entity.NamedEntity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Insertion-ordered, name-addressable registry for one entity type.
 */
public final class EntityManager<T extends NamedEntity> {
    private final Map<String, T> entities = new LinkedHashMap<>();

    public synchronized void addEntity(T entity) {
        Objects.requireNonNull(entity, "entity");
        if (entities.putIfAbsent(entity.getName(), entity) != null) {
            throw new IllegalArgumentException(
                    "Entity " + entity.getName() + " already exists.");
        }
    }

    public synchronized T findEntity(String name) {
        validateName(name);
        return entities.get(name);
    }

    public synchronized boolean containsEntity(String name) {
        validateName(name);
        return entities.containsKey(name);
    }

    public synchronized List<T> getEntities() {
        return List.copyOf(entities.values());
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Entity name cannot be blank.");
        }
    }
}
