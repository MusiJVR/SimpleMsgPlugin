package com.mousejava.simplemsgplugin.chatgroups;

import com.mousejava.simplemsgplugin.SimpleMsgPlugin;
import com.mousejava.simplemsgplugin.utils.Scheduler;
import com.mousejava.simplemsgplugin.utils.MessageUtils;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Group {
    private final UUID id;
    private final String name;
    private final List<GroupPlayer> players = new ArrayList<>();
    private GroupPlayer owner;

    public Group(String name, GroupPlayer owner) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.owner = owner;
    }

    public UUID getId() {
        return id;
    }

    public GroupPlayer getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public List<GroupPlayer> getPlayers() {
        return players;
    }

    public GroupPlayer getPlayerByName(String playerName) {
        return players.stream()
                .filter(player -> player.getName().equals(playerName))
                .findFirst()
                .orElse(null);
    }

    public GroupPlayer getPlayerById(UUID playerId) {
        return players.stream()
                .filter(player -> player.getId().equals(playerId))
                .findFirst()
                .orElse(null);
    }

    public void updateCommands() {
        players.forEach(GroupPlayer::updateCommands);
    }

    public void addPlayer(GroupPlayer player) {
        sendMessage("messages.privatechat.join_notification", player.getName(), null);
        players.add(player);
        player.updateCommands();
    }

    public boolean removePlayer(UUID playerId) {
        GroupPlayer removedPlayer = players.stream()
                .filter(player -> player.getId().equals(playerId))
                .findFirst()
                .orElse(null);

        if (removedPlayer == null)
            return false;

        players.remove(removedPlayer);
        removedPlayer.updateCommands();

        if (players.isEmpty()) {
            GroupManager.deleteGroup(owner.getId());
        } else {
            if (owner.getId().equals(playerId))
                owner = players.get(0);
        }

        sendMessage("messages.privatechat.leave_notification", removedPlayer.getName(), null);
        return true;
    }

    public void sendMessage(String templatePath, String playerName, String message) {
        for (GroupPlayer player : players) {
            Player bukkitPlayer = SimpleMsgPlugin.getInstance().getServer().getPlayer(player.getName());
            if (bukkitPlayer == null || !bukkitPlayer.isOnline()) continue;

            Scheduler.runForEntity(bukkitPlayer, () -> {
                MessageUtils.sendMiniMessageTransformed(bukkitPlayer, templatePath,
                        raw -> raw
                                .replace("<group>", name)
                                .replace("<player>", playerName != null ? playerName : "")
                                .replace("<message>", message != null ? message : "")
                );
            });
        }

    }

    @Override
    public String toString() {
        return "Group{" + "id=" + id + ", name='" + name + '\'' + '}';
    }
}
