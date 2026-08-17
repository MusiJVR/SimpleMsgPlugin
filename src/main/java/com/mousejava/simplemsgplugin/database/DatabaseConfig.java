package com.mousejava.simplemsgplugin.database;

import org.bukkit.configuration.file.FileConfiguration;

public record DatabaseConfig(String host, int port, String database, String username, String password, String properties, int maximumPoolSize, long connectionTimeoutMs) {
    public static DatabaseConfig from(FileConfiguration config) {
        return new DatabaseConfig(
                config.getString("database.ip", "localhost"),
                config.getInt("database.port", 3306),
                config.getString("database.dbname", ""),
                config.getString("database.username", ""),
                config.getString("database.password", ""),
                config.getString("database.properties", "verifyServerCertificate=false&useSSL=false&useUnicode=true&characterEncoding=utf8"),
                positive(config.getInt("database.maximum_pool_size", 10), "database.maximum_pool_size"),
                positive(config.getLong("database.connection_timeout_ms", 10_000L), "database.connection_timeout_ms")
        );
    }

    public String jdbcUrl() {
        String query = properties == null || properties.isBlank() ? "" : "?" + properties;
        return "jdbc:mysql://" + host + ":" + port + "/" + database + query;
    }

    private static int positive(int value, String path) {
        if (value < 1)
            throw new IllegalArgumentException(path + " must be greater than zero");

        return value;
    }

    private static long positive(long value, String path) {
        if (value < 1)
            throw new IllegalArgumentException(path + " must be greater than zero");

        return value;
    }
}
