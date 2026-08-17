package com.mousejava.simplemsgplugin.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

public final class DatabaseManager implements AutoCloseable {
    private final HikariDataSource dataSource;

    public DatabaseManager(String poolName, DatabaseConfig databaseConfig) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPoolName(poolName);
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hikariConfig.setJdbcUrl(databaseConfig.jdbcUrl());
        hikariConfig.setUsername(databaseConfig.username());
        hikariConfig.setPassword(databaseConfig.password());
        hikariConfig.setMaximumPoolSize(databaseConfig.maximumPoolSize());
        hikariConfig.setConnectionTimeout(databaseConfig.connectionTimeoutMs());

        dataSource = new HikariDataSource(hikariConfig);
    }

    public void execute(String sql, Object... parameters) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, parameters);
            statement.execute();
        } catch (SQLException exception) {
            throw new DatabaseException("Failed to execute a database statement", exception);
        }
    }

    public <T> Optional<T> queryOne(String sql, RowMapper<T> mapper, Object... parameters) {
        List<T> rows = query(sql, mapper, parameters);
        return rows.isEmpty() ? Optional.empty() : Optional.ofNullable(rows.get(0));
    }

    public <T> List<T> query(String sql, RowMapper<T> mapper, Object... parameters) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, parameters);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<T> rows = new ArrayList<>();
                while (resultSet.next()) {
                    rows.add(mapper.map(resultSet));
                }
                return rows;
            }
        } catch (SQLException exception) {
            throw new DatabaseException("Failed to query the database", exception);
        }
    }

    public boolean isRunning() {
        return !dataSource.isClosed();
    }

    @Override
    public void close() {
        dataSource.close();
    }

    private void bind(PreparedStatement statement, Object[] parameters) throws SQLException {
        for (int index = 0; index < parameters.length; index++) {
            statement.setObject(index + 1, parameters[index]);
        }
    }
}
