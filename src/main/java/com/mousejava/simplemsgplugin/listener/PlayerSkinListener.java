package com.mousejava.simplemsgplugin.listener;

import com.mousejava.simplemsgplugin.repository.SkinsRepository;
import com.mousejava.simplemsgplugin.service.SkinService;
import com.mousejava.simplemsgplugin.utils.Scheduler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerSkinListener implements Listener {
    private final SkinsRepository skinsRepository;
    private final SkinService skinService;

    public PlayerSkinListener(SkinsRepository skinsRepository, SkinService skinService) {
        this.skinsRepository = skinsRepository;
        this.skinService = skinService;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String base64 = skinService.getSkinBase64(player);
        if (base64 == null) return;

        UUID uuid = player.getUniqueId();

        Scheduler.run(() -> skinsRepository.upsert(uuid, base64));
    }
}
