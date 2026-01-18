package simplemsgplugin.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import simplemsgplugin.SimpleMsgPlugin;
import simplemsgplugin.utils.DatabaseCacheManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class PlayerMsgTabCompleter implements TabCompleter {
    private final DatabaseCacheManager cacheManager;

    public PlayerMsgTabCompleter(DatabaseCacheManager cacheManager) {
        this.cacheManager = cacheManager;

        this.cacheManager.createCache("player_names", "player_name", "sounds", null);
        this.cacheManager.scheduleAutoRefresh("player_names", "player_name", "sounds", null, 5 * 60 * 20);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            try {
                String inputPlayer = args[0].toLowerCase();
                List<String> allPlayerName = null;
                for (Map<String, Object> player : cacheManager.getCache("player_names")) {
                    if (player.get("player_name").toString().toLowerCase().startsWith(inputPlayer)) {
                        if (allPlayerName == null) {
                            allPlayerName = new ArrayList<>();
                        }
                        allPlayerName.add(player.get("player_name").toString());
                    }
                }

                if (allPlayerName != null) {
                    Collections.sort(allPlayerName);
                }

                return allPlayerName;
            } catch (Exception e) {
                SimpleMsgPlugin.getInstance().getLogger().log(Level.WARNING, "Error player message tab completer", e);
            }
        } else if (args.length == 2) {
            return Arrays.asList("<message>");
        }
        return new ArrayList<>();
    }
}
