package simplemsgplugin.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import simplemsgplugin.utils.DatabaseDriver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class PlayerMsgTabCompleter implements TabCompleter {
    private final DatabaseDriver dbDriver;

    public PlayerMsgTabCompleter(DatabaseDriver dbDriver) {
        this.dbDriver = dbDriver;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            try {
                String inputPlayer = args[0].toLowerCase();
                List<Map<String, Object>> rsPlayerNames = dbDriver.selectData("player_name", "sounds", null);

                List<String> allPlayerName = null;
                for (Map<String, Object> player : rsPlayerNames) {
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
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            }
        } else if (args.length == 2) {
            return Arrays.asList("<message>");
        }
        return new ArrayList<>();
    }
}
