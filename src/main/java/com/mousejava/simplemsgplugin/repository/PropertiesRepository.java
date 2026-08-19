package com.mousejava.simplemsgplugin.repository;

import com.mousejava.simplemsgplugin.database.DatabaseManager;
import com.mousejava.simplemsgplugin.database.SchemaRepository;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public final class PropertiesRepository implements SchemaRepository {
    private final DatabaseManager database;

    public PropertiesRepository(DatabaseManager database) {
        this.database = database;
    }

    @Override
    public void initializeSchema() {
        database.execute("""
                CREATE TABLE IF NOT EXISTS smp_properties (
                    player_uuid CHAR(36) NOT NULL,
                    property_key VARCHAR(64) NOT NULL,
                    value_type VARCHAR(16) NOT NULL,
                    value TEXT NULL,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    PRIMARY KEY (player_uuid, property_key),
                    CONSTRAINT fk_smp_properties_player FOREIGN KEY (player_uuid) REFERENCES smp_players(uuid) ON DELETE CASCADE
                )
                """);
    }

    public Optional<Property> find(UUID uuid, String key) {
        return database.queryOne("SELECT value_type, value FROM smp_properties WHERE player_uuid = ? AND property_key = ?",
                rs -> new Property(rs.getString("value_type"), rs.getString("value")), uuid.toString(), normalize(key)
        );
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
        database.execute("""
                        INSERT INTO smp_properties (player_uuid, property_key, value_type, value) VALUES (?, ?, ?, ?)
                        ON DUPLICATE KEY UPDATE value_type = VALUES(value_type), value = VALUES(value)
                        """,
                uuid.toString(), normalize(key), type, encoded
        );
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
