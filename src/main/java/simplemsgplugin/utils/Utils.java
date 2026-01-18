package simplemsgplugin.utils;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class Utils {
    public static void msgPlaySound(DatabaseDriver sql, Player player) {
        sql.selectData("sound, volume", "sounds", "WHERE uuid = ?", rs -> {
            if (rs.isEmpty() || !player.isOnline()) return;
            String messageSound = (String) rs.get(0).get("sound");
            int volumeSound = (int) rs.get(0).get("volume");
            if (!messageSound.equalsIgnoreCase("false")) {
                try {
                    player.playSound(player, Sound.valueOf(messageSound.toLowerCase()), (float) volumeSound / 100, 1.0f);
                } catch (Throwable ignored) {}
            }
        }, player.getUniqueId());
    }

    public static boolean checkDigits(String string) {
        if (string == null || string.isEmpty()) return false;
        for (int i = 0; i < string.length(); i++) {
            if (!Character.isDigit(string.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
