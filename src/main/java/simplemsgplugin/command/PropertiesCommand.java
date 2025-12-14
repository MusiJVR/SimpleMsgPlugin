package simplemsgplugin.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import simplemsgplugin.utils.DatabaseDriver;
import simplemsgplugin.utils.MessageUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PropertiesCommand implements CommandExecutor {
    public enum PropertyType {
        BOOLEAN
    }

    private final DatabaseDriver dbDriver;

    public static final Map<String, PropertyType> PROPERTIES = Map.of(
            "confirm_sending", PropertyType.BOOLEAN
    );

    public PropertiesCommand(DatabaseDriver dbDriver) {
        this.dbDriver = dbDriver;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (args.length != 2) {
            MessageUtils.sendMiniMessageIfPresent(sender, "messages.msgproperties.usage");
            return true;
        }

        UUID uuid = player.getUniqueId();
        String playerName = player.getName();
        String propertyName = args[0].toLowerCase();
        String valueRaw = args[1].toLowerCase();

        if (!PROPERTIES.containsKey(propertyName)) {
            MessageUtils.sendMiniMessageIfPresent(sender, "messages.msgproperties.unknown_property");
            return true;
        }

        PropertyType type = PROPERTIES.get(propertyName);

        switch (type) {
            case BOOLEAN -> {
                Boolean value = parseBoolean(valueRaw);
                if (value == null) {
                    MessageUtils.sendMiniMessageIfPresent(sender, "messages.msgproperties.confirm_sending.usage");
                    return true;
                }

                List<Map<String, Object>> rsProperties = dbDriver.selectData("uuid", "properties", "WHERE uuid = ?", uuid);
                if (rsProperties.isEmpty()) {
                    Map<String, Object> insertMap = new HashMap<>();
                    insertMap.put("uuid", uuid);
                    insertMap.put("player_name", playerName);
                    insertMap.put(propertyName, value);
                    dbDriver.insertData("properties", insertMap);
                } else {
                    Map<String, Object> updateMap = new HashMap<>();
                    updateMap.put(propertyName, value);
                    dbDriver.updateData("properties", updateMap, "uuid = ? AND player_name = ?", uuid, playerName);
                }

                MessageUtils.sendMiniMessageTransformed(sender, "messages.msgproperties.confirm_sending.property_set",
                        raw -> raw.replace("%property%", propertyName).replace("%value%", value ? "ON" : "OFF"));
            }
        }

        return true;
    }

    private Boolean parseBoolean(String input) {
        return switch (input) {
            case "on", "true", "yes" -> true;
            case "off", "false", "no" -> false;
            default -> null;
        };
    }
}
