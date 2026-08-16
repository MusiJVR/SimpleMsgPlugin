package com.mousejava.simplemsgplugin.chatgroups;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class GroupPlayer {
    private final UUID id;
    private final String name;

    public GroupPlayer(String name, UUID uuid) {
        this.id = uuid;
        this.name = name;
    }

    public GroupPlayer(Player player) {
        this(player.getName(), player.getUniqueId());
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void updateCommands() {
        Player player = Bukkit.getPlayer(id);
        if (player != null)
            player.updateCommands();
    }

    @Override
    public String toString() {
        return "Player{" + "id=" + id + ", name='" + name + '\'' + '}';
    }
}
