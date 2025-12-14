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

public class NotificationTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> completions = new ArrayList<>();

            try {
                Registry.SOUNDS.forEach(sound -> {
                    NamespacedKey key = sound.getKey();
                    if (key != null) {
                        String name = key.getKey();
                        if (name.startsWith(input) && !completions.contains(name)) {
                            completions.add(name);
                        }
                    }
                });
            } catch (Throwable ignored) {}

            if (completions.isEmpty()) {
                try {
                    for (Sound sound : Sound.values()) {
                        String name = sound.name().toLowerCase();
                        if (name.startsWith(input)) {
                            completions.add(name);
                        }
                    }
                } catch (Throwable ignored) {}
            }

            Collections.sort(completions);
            return completions;
        }

        if (args.length == 2) {
            List<String> volumes = new ArrayList<>();
            String input = args[1];

            for (int v : new int[]{0, 25, 50, 75, 100}) {
                String s = String.valueOf(v);
                if (s.startsWith(input)) {
                    volumes.add(s);
                }
            }

            if (volumes.isEmpty() && input.isEmpty()) {
                volumes.add("<0-100>");
            }

            return volumes;
        }

        return List.of();
    }
}
