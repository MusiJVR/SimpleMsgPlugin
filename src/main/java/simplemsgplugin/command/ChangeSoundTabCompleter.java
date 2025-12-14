package simplemsgplugin.command;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChangeSoundTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) return List.of();

        String input = args[0].toLowerCase();
        List<String> completions = new ArrayList<>();

        for (Sound sound : Sound.values()) {
            String name = sound.name().toLowerCase();
            if (name.startsWith(input)) {
                completions.add(name);
            }
        }

        Collections.sort(completions);
        return completions;
    }
}
