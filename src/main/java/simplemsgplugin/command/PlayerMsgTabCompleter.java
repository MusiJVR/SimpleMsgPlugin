package simplemsgplugin.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class PlayerMsgTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            Collection<? extends Player> onlinePlayers = Bukkit.getServer().getOnlinePlayers();
            ArrayList<String> onlinePlayerName = new ArrayList<>();
            for (Player onlinePlayer : onlinePlayers) {
                String name = onlinePlayer.getName();
                onlinePlayerName.add(name);
            }
            return onlinePlayerName;
        } else if (args.length == 2) {
            return Arrays.asList("<message>");
        }
        return new ArrayList<>();
    }
}