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
}
