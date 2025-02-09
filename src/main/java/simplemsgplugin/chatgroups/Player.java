package simplemsgplugin.chatgroups;

import java.util.UUID;

public class Player {
    private final UUID id;
    private final String name;

    public Player(String name, UUID uuid) {
        this.id = uuid;
        this.name = name;
    }

    public Player(org.bukkit.entity.Player player) {
        this(player.getName(), player.getUniqueId());
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Player{" + "id=" + id + ", name='" + name + '\'' + '}';
    }
}
