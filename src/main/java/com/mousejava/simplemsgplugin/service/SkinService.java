package com.mousejava.simplemsgplugin.service;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.mousejava.simplemsgplugin.database.repository.SkinsRepository;
import com.mousejava.simplemsgplugin.utils.ServerVersionUtils;
import com.mousejava.simplemsgplugin.utils.Scheduler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.Map;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public final class SkinService {
    private static final String STEVE_SKIN = "{\"player\":{\"name\":\"\"}}";

    private final SkinsRepository skinsRepository;

    public SkinService(SkinsRepository skinsRepository) {
        this.skinsRepository = skinsRepository;
    }

    public String getSkinBase64(Player player) {
        PlayerProfile profile = player.getPlayerProfile();
        if (profile == null)
            return null;

        return profile.getProperties().stream()
                .filter(p -> p.getName().equals("textures"))
                .map(ProfileProperty::getValue)
                .findFirst()
                .orElse(null);
    }

    public Component buildPlayerHeadComponent(String base64) {
        if (!ServerVersionUtils.supportsPlayerHeadComponent())
            return Component.empty();

        if (base64 == null || base64.isBlank())
            return GsonComponentSerializer.gson().deserialize(STEVE_SKIN);

        String json = "{\"player\":{\"properties\":[{\"name\":\"textures\",\"value\":\"%s\"}]}}".formatted(base64);
        return GsonComponentSerializer.gson().deserialize(json);
    }

    public Component resolveHeadComponent(UUID uuid) {
        if (!Bukkit.isPrimaryThread()) {
            CompletableFuture<Map<UUID, String>> snapshot = new CompletableFuture<>();
            Scheduler.run(() -> {
                if (snapshot.isDone()) return;

                try {
                    Player online = Bukkit.getPlayer(uuid);
                    snapshot.complete(online == null ? Map.of()
                            : Collections.singletonMap(uuid, getSkinBase64(online)));
                } catch (Exception failure) {
                    snapshot.completeExceptionally(failure);
                }
            });

            Map<UUID, String> onlineSkin = snapshot.orTimeout(30, TimeUnit.SECONDS).join();
            String base64 = onlineSkin.containsKey(uuid) ? onlineSkin.get(uuid)
                    : skinsRepository.findSkin(uuid).orElse(null);
            return buildPlayerHeadComponent(base64);
        }

        Player online = Bukkit.getPlayer(uuid);
        String base64 = online != null ? getSkinBase64(online)
                : skinsRepository.findSkin(uuid).orElse(null);
        return buildPlayerHeadComponent(base64);
    }
}
