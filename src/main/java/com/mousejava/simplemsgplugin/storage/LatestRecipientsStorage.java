package com.mousejava.simplemsgplugin.storage;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class LatestRecipientsStorage {
    private final ConcurrentMap<String, String> recipients = new ConcurrentHashMap<>();

    public void put(String player, String recipient) {
        recipients.put(player, recipient);
    }

    public Optional<String> find(String player) {
        return Optional.ofNullable(recipients.get(player));
    }

    public void remove(String player) {
        recipients.remove(player);
    }

    public void clear() {
        recipients.clear();
    }
}
