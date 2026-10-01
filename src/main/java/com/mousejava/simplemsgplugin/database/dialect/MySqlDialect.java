package com.mousejava.simplemsgplugin.database.dialect;

import java.util.List;

public final class MySqlDialect implements SqlDialect {
    @Override
    public List<String> playersSchema() {
        return List.of("""
                CREATE TABLE IF NOT EXISTS smp_players (
                    uuid CHAR(36) NOT NULL,
                    nickname VARCHAR(16) NOT NULL,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    PRIMARY KEY (uuid),
                    UNIQUE KEY uk_smp_players_nickname (nickname)
                )
                """);
    }

    @Override
    public String playersUpsert() {
        return """
                INSERT INTO smp_players (uuid, nickname) VALUES (?, ?)
                ON DUPLICATE KEY UPDATE nickname = VALUES(nickname)
                """;
    }

    @Override
    public List<String> propertiesSchema() {
        return List.of("""
                CREATE TABLE IF NOT EXISTS smp_properties (
                    player_uuid CHAR(36) NOT NULL,
                    properties LONGTEXT NOT NULL,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    PRIMARY KEY (player_uuid),
                    CONSTRAINT fk_smp_properties_player FOREIGN KEY (player_uuid) REFERENCES smp_players(uuid) ON DELETE CASCADE
                )
                """);
    }

    @Override
    public String propertiesInsertIfAbsent() {
        return """
                INSERT INTO smp_properties (player_uuid, properties) VALUES (?, '{}')
                ON DUPLICATE KEY UPDATE player_uuid = VALUES(player_uuid)
                """;
    }

    @Override
    public String propertiesFind() {
        return "SELECT properties FROM smp_properties WHERE player_uuid = ?";
    }

    @Override
    public String propertiesFindForUpdate() {
        return "SELECT properties FROM smp_properties WHERE player_uuid = ? FOR UPDATE";
    }

    @Override
    public String propertiesUpdate() {
        return "UPDATE smp_properties SET properties = ? WHERE player_uuid = ?";
    }

    @Override
    public List<String> offlineMessagesSchema() {
        return List.of("""
                CREATE TABLE IF NOT EXISTS smp_offline_messages (
                    id BIGINT NOT NULL AUTO_INCREMENT,
                    sender_uuid CHAR(36) NULL,
                    sender_name VARCHAR(16) NOT NULL,
                    receiver_name VARCHAR(16) NOT NULL,
                    message TEXT NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (id),
                    INDEX ix_smp_offline_receiver (receiver_name)
                )
                """);
    }

    @Override
    public List<String> blacklistSchema() {
        return List.of("""
                CREATE TABLE IF NOT EXISTS smp_blacklist (
                    owner_uuid CHAR(36) NOT NULL,
                    blocked_uuid CHAR(36) NOT NULL,
                    blocked_name VARCHAR(16) NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (owner_uuid, blocked_uuid),
                    CONSTRAINT fk_smp_blacklist_owner FOREIGN KEY (owner_uuid) REFERENCES smp_players(uuid) ON DELETE CASCADE,
                    CONSTRAINT fk_smp_blacklist_blocked FOREIGN KEY (blocked_uuid) REFERENCES smp_players(uuid) ON DELETE CASCADE
                )
                """);
    }

    @Override
    public String blacklistUpsert() {
        return """
                INSERT INTO smp_blacklist (owner_uuid, blocked_uuid, blocked_name) VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE blocked_name = VALUES(blocked_name)
                """;
    }

    @Override
    public List<String> skinsSchema() {
        return List.of("""
                CREATE TABLE IF NOT EXISTS smp_player_skins (
                    player_uuid CHAR(36) NOT NULL,
                    skin_base64 TEXT,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    PRIMARY KEY (player_uuid),
                    CONSTRAINT fk_smp_player_skins_player FOREIGN KEY (player_uuid) REFERENCES smp_players(uuid) ON DELETE CASCADE
                )
                """);
    }

    @Override
    public String skinsUpsert() {
        return """
                INSERT INTO smp_player_skins (player_uuid, skin_base64) VALUES (?, ?)
                ON DUPLICATE KEY UPDATE skin_base64 = VALUES(skin_base64)
                """;
    }

    @Override
    public String playersFindUuidByName() {
        return "SELECT uuid FROM smp_players WHERE LOWER(nickname) = LOWER(?)";
    }

    @Override
    public String playersFindName() {
        return "SELECT nickname FROM smp_players WHERE uuid = ?";
    }

    @Override
    public String playersFindAllNames() {
        return "SELECT nickname FROM smp_players ORDER BY nickname";
    }

    @Override
    public String offlineMessagesSave() {
        return "INSERT INTO smp_offline_messages (sender_uuid, sender_name, receiver_name, message) VALUES (?, ?, ?, ?)";
    }

    @Override
    public String offlineMessagesFindForReceiver() {
        return "SELECT sender_uuid, sender_name, receiver_name, message FROM smp_offline_messages WHERE LOWER(receiver_name) = LOWER(?) ORDER BY created_at, id";
    }

    @Override
    public String offlineMessagesDeleteForReceiver() {
        return "DELETE FROM smp_offline_messages WHERE LOWER(receiver_name) = LOWER(?)";
    }

    @Override
    public String blacklistIsBlocked() {
        return "SELECT 1 FROM smp_blacklist WHERE owner_uuid = ? AND blocked_uuid = ?";
    }

    @Override
    public String blacklistRemove() {
        return "DELETE FROM smp_blacklist WHERE owner_uuid = ? AND blocked_uuid = ?";
    }

    @Override
    public String blacklistListNames() {
        return "SELECT blocked_name FROM smp_blacklist WHERE owner_uuid = ? ORDER BY blocked_name";
    }

    @Override
    public String skinsFindSkin() {
        return "SELECT skin_base64 FROM smp_player_skins WHERE player_uuid = ?";
    }
}
