package com.mousejava.simplemsgplugin.storage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class OfflineMessageStorage {
    public record PendingMessage(String receiver, String message) { }

    private final ConcurrentMap<UUID, PendingMessage> messages = new ConcurrentHashMap<>();
    private final Set<PendingMessage> sending = Collections.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));

    public void put(UUID sender, String receiver, String message) {
        messages.put(sender, new PendingMessage(receiver, message));
    }

    public Optional<PendingMessage> find(UUID sender) {
        return Optional.ofNullable(messages.get(sender));
    }

    public boolean remove(UUID sender, PendingMessage message) {
        return messages.remove(sender, message);
    }

    public void remove(UUID sender) {
        messages.remove(sender);
    }

    public void clear() {
        messages.clear();
    }

    public boolean beginSending(PendingMessage message) {
        return sending.add(message);
    }

    public void finishSending(PendingMessage message) {
        sending.remove(message);
    }
}
