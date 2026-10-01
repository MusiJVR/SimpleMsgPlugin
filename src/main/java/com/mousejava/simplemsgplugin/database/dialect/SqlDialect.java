package com.mousejava.simplemsgplugin.database.dialect;

import java.util.List;

public interface SqlDialect {
    List<String> playersSchema();
    String playersUpsert();
    List<String> propertiesSchema();
    String propertiesInsertIfAbsent();
    String propertiesFind();
    String propertiesFindForUpdate();
    String propertiesUpdate();
    List<String> offlineMessagesSchema();
    List<String> blacklistSchema();
    String blacklistUpsert();
    List<String> skinsSchema();
    String skinsUpsert();
    String playersFindUuidByName();
    String playersFindName();
    String playersFindAllNames();
    String offlineMessagesSave();
    String offlineMessagesFindForReceiver();
    String offlineMessagesDeleteForReceiver();
    String blacklistIsBlocked();
    String blacklistRemove();
    String blacklistListNames();
    String skinsFindSkin();
}
