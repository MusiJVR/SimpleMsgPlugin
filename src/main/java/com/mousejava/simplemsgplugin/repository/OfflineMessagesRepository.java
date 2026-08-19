package com.mousejava.simplemsgplugin.repository;

import com.mousejava.simplemsgplugin.database.DatabaseManager;
import com.mousejava.simplemsgplugin.database.SchemaRepository;

import java.util.List;
import java.util.UUID;

public final class OfflineMessagesRepository implements SchemaRepository {
    public record OfflineMessage(String senderUuid, String senderName, String receiverName, String message) { }

    private final DatabaseManager database;

    public OfflineMessagesRepository(DatabaseManager database) {
        this.database = database;
    }

    @Override
    public void initializeSchema() {
        database.execute("""
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

    public void save(UUID senderUuid, String senderName, String receiverName, String message) {
        database.execute("INSERT INTO smp_offline_messages (sender_uuid, sender_name, receiver_name, message) VALUES (?, ?, ?, ?)",
                senderUuid == null ? null : senderUuid.toString(), senderName, receiverName, message
        );
    }

    public List<OfflineMessage> findForReceiver(String receiverName) {
        return database.query("SELECT sender_uuid, sender_name, receiver_name, message FROM smp_offline_messages WHERE LOWER(receiver_name) = LOWER(?) ORDER BY created_at, id",
                rs -> new OfflineMessage(rs.getString("sender_uuid"), rs.getString("sender_name"), rs.getString("receiver_name"), rs.getString("message")), receiverName
        );
    }

    public void deleteForReceiver(String receiverName) {
        database.execute("DELETE FROM smp_offline_messages WHERE LOWER(receiver_name) = LOWER(?)",
                receiverName
        );
    }
}
