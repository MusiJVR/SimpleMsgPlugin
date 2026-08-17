package com.mousejava.simplemsgplugin.utils;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import com.mousejava.simplemsgplugin.repository.PropertiesRepository;
import java.util.UUID;

public final class Utils {
    public static void msgPlaySound(PropertiesRepository properties, Player player) {
        if (!player.isOnline()) return;
        UUID uuid = player.getUniqueId();
        String messageSound = properties.getString(uuid, "sound", "false");
        int volumeSound = properties.getInt(uuid, "volume", 50);

        if (!messageSound.equalsIgnoreCase("false")) {
            try {
                player.playSound(player, Sound.valueOf(messageSound.toUpperCase()), (float) volumeSound / 100, 1.0f);
            } catch (Throwable ignored) {}
        }
    }

    public static boolean checkDigits(String string) {
        if (string == null || string.isEmpty())
            return false;

        for (int i = 0; i < string.length(); i++) {
            if (!Character.isDigit(string.charAt(i)))
                return false;
        }

        return true;
    }
}
