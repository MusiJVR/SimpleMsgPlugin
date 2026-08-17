package com.mousejava.simplemsgplugin.database;

import java.util.List;

public final class SchemaInitializer {
    private final List<SchemaRepository> repositories;

    public SchemaInitializer(List<SchemaRepository> repositories) {
        this.repositories = List.copyOf(repositories);
    }

    public void initialize() {
        repositories.forEach(SchemaRepository::initializeSchema);
    }
}
