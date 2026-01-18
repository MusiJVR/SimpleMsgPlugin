package simplemsgplugin.chatgroups;

import simplemsgplugin.SimpleMsgPlugin;
import simplemsgplugin.scheduler.Scheduler;
import simplemsgplugin.utils.MessageUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class Group {
    private final UUID id;
    private final String name;
    private final List<Player> players = new ArrayList<>();
    private Player owner;

    public Group(String name, Player owner) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.owner = owner;
    }

    public UUID getId() {
        return id;
    }

    public Player getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Player getPlayerByName(String playerName) {
        return players.stream()
                .filter(player -> player.getName().equals(playerName))
                .findFirst()
                .orElse(null);
    }

    public Player getPlayerById(UUID playerId) {
        return players.stream()
                .filter(player -> player.getId().equals(playerId))
                .findFirst()
                .orElse(null);
    }

    public void addPlayer(Player player) {
        sendMessage("messages.privatechat.join_notification", player.getName(), null);
        players.add(player);
    }

    public boolean removePlayer(UUID playerId) {
        AtomicReference<String> removedPlayerName = new AtomicReference<>();
        boolean val = players.removeIf(player -> {
            if (player.getId().equals(playerId)) {
                removedPlayerName.set(player.getName());
                return true;
            }
            return false;
        });
        if (val) {
            if (players.isEmpty()) {
                GroupManager.deleteGroup(owner.getId());
            } else {
                if (owner.getId().equals(playerId)) {
                    owner = players.get(0);
                }
            }
            sendMessage("messages.privatechat.leave_notification", removedPlayerName.get(), null);
        }
        return val;
    }

    public void sendMessage(String templatePath, String playerName, String message) {
        for (Player player : players) {
            org.bukkit.entity.Player bukkitPlayer = SimpleMsgPlugin.getInstance().getServer().getPlayer(player.getName());
            if (bukkitPlayer == null || !bukkitPlayer.isOnline()) continue;

            Scheduler.runForPlayer(bukkitPlayer, () -> {
                MessageUtils.sendMiniMessageTransformed(bukkitPlayer, templatePath,
                        raw -> raw
                                .replace("%group%", name)
                                .replace("%player%", playerName != null ? playerName : "")
                                .replace("%message%", message != null ? message : "")
                );
            });
        }

    }

    @Override
    public String toString() {
        return "Group{" + "id=" + id + ", name='" + name + '\'' + '}';
    }
}
