package com.mousejava.simplemsgplugin.utils;

import com.mousejava.simplemsgplugin.database.repository.PropertiesRepository;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import java.util.UUID;

public final class Utils {
    public static void msgPlaySound(PropertiesRepository properties, Player player) {
        if (!player.isOnline()) return;

        UUID uuid = player.getUniqueId();
        Scheduler.runAsync(() -> {
            String sound = properties.getString(uuid, "sound", "false");
            int volume = properties.getInt(uuid, "volume", 50);
            Scheduler.runForEntity(player, () -> {
                if (!player.isOnline() || sound.equalsIgnoreCase("false")) return;
                try {
                    player.playSound(player, Sound.valueOf(sound.toUpperCase()), (float) volume / 100, 1.0f);
                } catch (Throwable ignored) { }
            });
        });
    }
}
