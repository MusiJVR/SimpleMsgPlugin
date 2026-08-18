package com.mousejava.simplemsgplugin.handler;

import com.mousejava.simplemsgplugin.repository.PropertiesRepository;
import com.mousejava.simplemsgplugin.service.UpdateChecker;
import com.mousejava.simplemsgplugin.utils.MessageUtils;
import com.mousejava.simplemsgplugin.utils.Scheduler;
import com.mousejava.simplemsgplugin.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class UpdateNotifyListener implements Listener {
    private final JavaPlugin plugin;
    private final PropertiesRepository properties;

    public UpdateNotifyListener(JavaPlugin plugin, PropertiesRepository properties) {
        this.plugin = plugin;
        this.properties = properties;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        if (!plugin.getConfig().getBoolean("send_update_notifications", true))
            return;

        Player player = event.getPlayer();
        if (!player.hasPermission("simplemsgplugin.update_listener"))
            return;

        if (!UpdateChecker.isUpdateAvailable())
            return;

        Scheduler.runForEntityLater(player, () -> sendUpdateMessage(player), 40);
    }

    private void sendUpdateMessage(Player player) {
        String current = plugin.getPluginMeta().getVersion();
        String latest = UpdateChecker.getLatestVersion();
        String url = UpdateChecker.getDownloadUrl();
        String hoverText = MessageUtils.optionalPlain("messages.new_version_hover_text").orElse("");

        MessageUtils.sendMiniMessageTransformed(player, "messages.new_version_available",
                msg -> msg
                        .replace("<current_version>", current)
                        .replace("<new_version>", latest)
                        .replace("<url>", url)
                        .replace("<new_version_hover_text>", hoverText)
        );

        Utils.msgPlaySound(properties, player);
    }
}
