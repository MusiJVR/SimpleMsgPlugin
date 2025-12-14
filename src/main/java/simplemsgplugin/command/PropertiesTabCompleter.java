package simplemsgplugin.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;

import static simplemsgplugin.command.PropertiesCommand.PROPERTIES;

public class PropertiesTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            List<String> completions = new ArrayList<>();
            for (String prop : PROPERTIES.keySet()) {
                if (prop.startsWith(prefix)) completions.add(prop);
            }
            return completions;
        }

        if (args.length == 2) {
            String propertyName = args[0].toLowerCase();
            if (PROPERTIES.containsKey(propertyName) && PROPERTIES.get(propertyName) == PropertiesCommand.PropertyType.BOOLEAN) {
                String prefix = args[1].toLowerCase();
                List<String> completions = new ArrayList<>();
                for (String option : List.of("on", "off")) {
                    if (option.startsWith(prefix)) completions.add(option);
                }
                return completions;
            }
        }

        return Collections.emptyList();
    }
}
