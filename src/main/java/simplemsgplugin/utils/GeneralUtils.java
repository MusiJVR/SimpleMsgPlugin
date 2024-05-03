package simplemsgplugin.utils;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class GeneralUtils {
    public static void msgPlaySound(SqliteDriver sql, Player player) {
        try {
            List<Map<String, Object>> rsGetSound = sql.sqlSelectData("Sound, Volume", "SOUNDS", "UUID = '" + player.getUniqueId() + "'");
            String messageSound = (String) rsGetSound.get(0).get("Sound");
            Integer volumeSound = (Integer) rsGetSound.get(0).get("Volume");
            if (!messageSound.equals("false")) {
                for (Sound soundPlayer : Sound.values()) {
                    if (messageSound.equals(soundPlayer.toString())) {
                        player.playSound(player.getLocation(), Sound.valueOf(messageSound), (float) volumeSound / 100, 1.0f);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }
}