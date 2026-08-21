package com.mousejava.simplemsgplugin.service;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.mousejava.simplemsgplugin.repository.SkinsRepository;
import com.mousejava.simplemsgplugin.utils.ServerVersionUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

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
        Player online = Bukkit.getPlayer(uuid);
        String base64 = online != null
                ? getSkinBase64(online)
                : skinsRepository.findSkin(uuid).orElse(null);
        return buildPlayerHeadComponent(base64);
    }
}
