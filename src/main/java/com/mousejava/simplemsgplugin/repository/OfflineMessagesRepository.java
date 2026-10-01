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
        database.dialect().offlineMessagesSchema().forEach(database::execute);
    }

    public void save(UUID senderUuid, String senderName, String receiverName, String message) {
        database.execute(database.dialect().offlineMessagesSave(),
                senderUuid == null ? null : senderUuid.toString(), senderName, receiverName, message
        );
    }

    public List<OfflineMessage> findForReceiver(String receiverName) {
        return database.query(database.dialect().offlineMessagesFindForReceiver(),
                rs -> new OfflineMessage(rs.getString("sender_uuid"), rs.getString("sender_name"), rs.getString("receiver_name"), rs.getString("message")), receiverName
        );
    }

    public void deleteForReceiver(String receiverName) {
        database.execute(database.dialect().offlineMessagesDeleteForReceiver(),
                receiverName
        );
    }
}
