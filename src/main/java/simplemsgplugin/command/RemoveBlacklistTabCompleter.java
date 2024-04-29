package simplemsgplugin.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class RemoveBlacklistTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            String inputPlayer = args[0].toLowerCase();
            Collection<? extends Player> onlinePlayers = Bukkit.getServer().getOnlinePlayers();
            List<String> onlinePlayerName = null;
            for (Player onlinePlayer : onlinePlayers) {
                if (onlinePlayer.toString().toLowerCase().startsWith(inputPlayer)) {
                    if (onlinePlayerName == null) {
                        onlinePlayerName = new ArrayList<>();
                    }
                    String name = onlinePlayer.getName();
                    onlinePlayerName.add(name);
                }
            }
            if (onlinePlayerName != null) {
                Collections.sort(onlinePlayerName);
            }
            return onlinePlayerName;
        }
        return new ArrayList<>();
    }
}