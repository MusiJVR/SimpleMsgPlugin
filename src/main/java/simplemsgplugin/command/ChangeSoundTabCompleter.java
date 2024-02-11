package simplemsgplugin.command;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;

public class ChangeSoundTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            String inputSound = args[0].toUpperCase();
            ArrayList<String> soundCollection = new ArrayList<>();
            for (Object valSound:Sound.values()) {
                soundCollection.add(valSound.toString());
            }
            List<String> soundNames = null;
            for (String sound:soundCollection) {
                if (sound.startsWith(inputSound)) {
                    if (soundNames == null) {
                        soundNames = new ArrayList<>();
                    }
                    soundNames.add(sound);
                }
            }
            if (soundNames != null) {
                Collections.sort(soundNames);
            }
            return soundNames;
        }
        return new ArrayList<>();
    }
}