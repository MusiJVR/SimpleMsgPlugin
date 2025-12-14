package simplemsgplugin.utils;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class Utils {
    public static void msgPlaySound(DatabaseDriver sql, Player player) {
        List<Map<String, Object>> rsGetSound = sql.selectData("sound, volume", "sounds", "WHERE uuid = ?", player.getUniqueId());
        String messageSound = (String) rsGetSound.get(0).get("sound");
        int volumeSound = (int) rsGetSound.get(0).get("volume");
        if (!messageSound.equalsIgnoreCase("false")) {
            try {
                player.playSound(player, Sound.valueOf(messageSound.toLowerCase()), (float) volumeSound / 100, 1.0f);
            } catch (Throwable ignored) {}
        }
    }

    public static boolean checkDigits(String string) {
        boolean digits = true;
        for (int i = 0; i < string.length() && digits; i++) {
            if (!Character.isDigit(string.charAt(i))) {
                digits = false;
            }
        }
        return digits;
    }
}
