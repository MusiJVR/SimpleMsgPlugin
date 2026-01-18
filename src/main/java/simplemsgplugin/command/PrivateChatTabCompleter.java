package simplemsgplugin.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import simplemsgplugin.chatgroups.Group;
import simplemsgplugin.chatgroups.GroupManager;

import java.util.*;
import java.util.stream.Collectors;

public class PrivateChatTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        Player playerSender = (Player) sender;
        Group group = GroupManager.findGroupByPlayer(playerSender.getUniqueId());
        String input = args[0].toLowerCase();
        if (args.length == 1) {
            List<String> options = new ArrayList<>();
            if (group != null) {
                if (group.getOwner().getId().equals(playerSender.getUniqueId())) {
                    options.addAll(List.of("delete", "invite", "leave", "kick", "info"));
                }
                options.addAll(List.of("leave", "info"));
            }
            options.addAll(List.of("create", "join"));

            return options.stream()
                    .filter(s -> s.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            List<String> allPlayerName = new ArrayList<>();

            switch (input) {
                case "create": return Arrays.asList("<group name>");
                case "delete": return List.of();
                case "invite":
                    if (group != null) {
                        if (group.getOwner().getId().equals(playerSender.getUniqueId())) {
                            String inputPlayer = args[1].toLowerCase();
                            Collection<? extends Player> onlinePlayers = Bukkit.getServer().getOnlinePlayers();
                            for (Player player : onlinePlayers) {
                                if (player.getName().toLowerCase().startsWith(inputPlayer)) {
                                    allPlayerName.add(player.getName());
                                }
                            }
                        }
                    }
                    return allPlayerName;
                case "join": return Arrays.asList("<group ID>");
                case "leave": return List.of();
                case "kick":
                    if (group != null) {
                        if (group.getOwner().getId().equals(playerSender.getUniqueId())) {
                            String inputPlayer = args[1].toLowerCase();
                            List<String> groupPlayers = group.getPlayers().stream()
                                    .map(simplemsgplugin.chatgroups.Player::getName)
                                    .collect(Collectors.toList());
                            for (String player : groupPlayers) {
                                if (player.toLowerCase().startsWith(inputPlayer)) {
                                    allPlayerName.add(player);
                                }
                            }
                        }
                    }
                    return allPlayerName;
                case "info": return List.of();
                default: return List.of();
            }
        }
        return List.of();
    }
}
