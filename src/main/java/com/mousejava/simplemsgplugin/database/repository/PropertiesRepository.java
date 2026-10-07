package com.mousejava.simplemsgplugin.database.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mousejava.simplemsgplugin.database.api.DatabaseManager;
import com.mousejava.simplemsgplugin.database.api.SchemaRepository;
import com.mousejava.simplemsgplugin.database.dialect.SqlDialect;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class PropertiesRepository implements SchemaRepository {
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();
    private final DatabaseManager<SqlDialect> database;
    private final Map<UUID, Map<String, Property>> cache = new ConcurrentHashMap<>();

    public PropertiesRepository(DatabaseManager<SqlDialect> database) {
        this.database = database;
    }

    @Override
    public void initializeSchema() {
        database.dialect().propertiesSchema().forEach(database::execute);
    }

    public Optional<Property> find(UUID uuid, String key) {
        return Optional.ofNullable(cache.computeIfAbsent(uuid, this::load).get(normalize(key)));
    }

    private Map<String, Property> load(UUID uuid) {
        return database.queryOne(database.dialect().propertiesFind(),
                        rs -> GSON.fromJson(rs.getString("properties"), JsonObject.class), uuid.toString())
                .map(PropertiesRepository::toMap)
                .orElseGet(Map::of);
    }

    public void invalidate(UUID uuid) {
        cache.remove(uuid);
    }

    public Object get(UUID uuid, String key, Object defaultValue) {
        return find(uuid, key).map(Property::decode).orElse(defaultValue);
    }

    public boolean getBoolean(UUID uuid, String key, boolean defaultValue) {
        Object value = get(uuid, key, defaultValue);

        if (value instanceof Boolean b)
            return b;

        if (value instanceof Number n)
            return n.intValue() != 0;

        return Boolean.parseBoolean(String.valueOf(value));
    }

    public int getInt(UUID uuid, String key, int defaultValue) {
        Object value = get(uuid, key, defaultValue);
        if (value instanceof Number n)
            return n.intValue();

        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    public String getString(UUID uuid, String key, String defaultValue) {
        return String.valueOf(get(uuid, key, defaultValue));
    }

    public void set(UUID uuid, String key, Object value) {
        String type = typeOf(value);
        String encoded = value == null ? null : String.valueOf(value);

        modify(uuid, properties -> {
            properties.add(normalize(key), GSON.toJsonTree(new Property(type, encoded)));
            return true;
        });
    }

    public void setDefaults(UUID uuid, Map<String, Object> defaults) {
        modify(uuid, properties -> {
            boolean changed = false;
            for (var entry : defaults.entrySet()) {
                String key = normalize(entry.getKey());
                if (!properties.has(key)) {
                    Object v = entry.getValue();
                    properties.add(key, GSON.toJsonTree(new Property(typeOf(v), v == null ? null : String.valueOf(v))));
                    changed = true;
                }
            }
            return changed;
        });
    }

    private void modify(UUID uuid, Function<JsonObject, Boolean> mutator) {
        cache.compute(uuid, (id, old) -> {
            JsonObject[] result = new JsonObject[1];

            database.transaction(connection -> {
                try (var insert = connection.prepareStatement(database.dialect().propertiesInsertIfAbsent())) {
                    insert.setString(1, uuid.toString());
                    insert.executeUpdate();
                }

                JsonObject properties;
                try (var select = connection.prepareStatement(database.dialect().propertiesFindForUpdate())) {
                    select.setString(1, uuid.toString());
                    try (var rows = select.executeQuery()) {
                        if (!rows.next())
                            throw new SQLException("Missing properties row for " + uuid);

                        properties = GSON.fromJson(rows.getString("properties"), JsonObject.class);
                    }
                }

                if (mutator.apply(properties)) {
                    try (var update = connection.prepareStatement(database.dialect().propertiesUpdate())) {
                        update.setString(1, GSON.toJson(properties));
                        update.setString(2, uuid.toString());
                        update.executeUpdate();
                    }
                }

                result[0] = properties;
                return null;
            });

            return toMap(result[0]);
        });
    }

    public record Property(String type, String value) {
        public Object decode() {
            if (value == null) return null;

            return switch (type.toUpperCase(Locale.ROOT)) {
                case "BOOLEAN" -> Boolean.parseBoolean(value);
                case "INTEGER" -> parseInt(value);
                case "DOUBLE" -> parseDouble(value);
                case "FLOAT" -> parseFloat(value);
                default -> value;
            };
        }

        private static Integer parseInt(String value) {
            try {
                return Integer.valueOf(value);
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        private static Double parseDouble(String value) {
            try {
                return Double.valueOf(value);
            } catch (NumberFormatException e) {
                return 0d;
            }
        }

        private static Float parseFloat(String value) {
            try {
                return Float.valueOf(value);
            } catch (NumberFormatException e) {
                return 0f;
            }
        }
    }

    private static Map<String, Property> toMap(JsonObject json) {
        Map<String, Property> map = new HashMap<>();
        for (var entry : json.entrySet()) {
            Property property = GSON.fromJson(entry.getValue(), Property.class);
            if (property != null)
                map.put(entry.getKey(), property);
        }
        return Map.copyOf(map);
    }

    private static String normalize(String key) {
        return key.toLowerCase(Locale.ROOT);
    }

    private static String typeOf(Object value) {
        if (value instanceof Boolean)
            return "BOOLEAN";

        if (value instanceof Integer || value instanceof Long || value instanceof Short || value instanceof Byte)
            return "INTEGER";

        if (value instanceof Float)
            return "FLOAT";

        if (value instanceof Number)
            return "DOUBLE";

        return "STRING";
    }
}
