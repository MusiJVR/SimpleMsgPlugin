package com.mousejava.simplemsgplugin.database;

import com.mousejava.simplemsgplugin.database.dialect.MySqlDialect;
import com.mousejava.simplemsgplugin.database.dialect.SqlDialect;
import com.mousejava.simplemsgplugin.database.dialect.SqliteDialect;
import com.zaxxer.hikari.HikariConfig;

import java.util.Locale;

public enum DatabaseType {
    SQLITE(new SqliteDialect()) {
        @Override
        String jdbcUrl(DatabaseConfig config) {
            return "jdbc:sqlite:" + config.sqliteFile().getAbsolutePath();
        }

        @Override
        void configure(HikariConfig pool, DatabaseConfig config) {
            pool.setDriverClassName("org.sqlite.JDBC");
            pool.setMaximumPoolSize(1);
            pool.addDataSourceProperty("foreign_keys", "true");
            pool.addDataSourceProperty("busy_timeout", "10000");
            pool.addDataSourceProperty("journal_mode", "WAL");
        }
    },
    MYSQL(new MySqlDialect()) {
        @Override
        String jdbcUrl(DatabaseConfig config) {
            String query = config.properties() == null || config.properties().isBlank() ? "" : "?" + config.properties();
            return "jdbc:mysql://" + config.host() + ":" + config.port() + "/" + config.database() + query;
        }

        @Override
        void configure(HikariConfig pool, DatabaseConfig config) {
            pool.setDriverClassName("com.mysql.cj.jdbc.Driver");
            pool.setUsername(config.username());
            pool.setPassword(config.password());
            pool.setMaximumPoolSize(config.maximumPoolSize());
        }
    };

    private final SqlDialect dialect;

    DatabaseType(SqlDialect dialect) {
        this.dialect = dialect;
    }

    public SqlDialect dialect() {
        return dialect;
    }

    abstract String jdbcUrl(DatabaseConfig config);

    abstract void configure(HikariConfig pool, DatabaseConfig config);

    public static DatabaseType parse(String value) {
        if (value == null) return SQLITE;
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return SQLITE;
        }
    }
}
