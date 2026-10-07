package com.mousejava.simplemsgplugin.listener;

import com.mousejava.simplemsgplugin.SimpleMsgPlugin;
import com.mousejava.simplemsgplugin.database.repository.OfflineMessagesRepository;
import com.mousejava.simplemsgplugin.database.repository.PlayersRepository;
import com.mousejava.simplemsgplugin.database.repository.PropertiesRepository;
import com.mousejava.simplemsgplugin.storage.LatestRecipientsStorage;
import com.mousejava.simplemsgplugin.utils.MessageUtils;
import com.mousejava.simplemsgplugin.utils.Scheduler;
import com.mousejava.simplemsgplugin.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerJoinQuitEventListeners implements Listener {
    private final SimpleMsgPlugin plugin;
    private final PlayersRepository players;
    private final PropertiesRepository properties;
    private final OfflineMessagesRepository offlineMessages;
    private final LatestRecipientsStorage latestRecipients;

    public PlayerJoinQuitEventListeners(JavaPlugin plugin, PlayersRepository players, PropertiesRepository properties, OfflineMessagesRepository offlineMessages, LatestRecipientsStorage latestRecipients) {
        this.plugin = (SimpleMsgPlugin) plugin;
        this.players = players;
        this.properties = properties;
        this.offlineMessages = offlineMessages;
        this.latestRecipients = latestRecipients;
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String name = player.getName();

        Map<String, Object> defaults = new LinkedHashMap<>();
        defaults.put("sound", plugin.getConfig().getString("default_sound", "false"));
        defaults.put("volume", plugin.getConfig().getInt("default_volume", 50));
        defaults.put("confirm_sending", plugin.getConfig().getBoolean("confirm_sending", true));

        Scheduler.runAsync(() -> {
            try {
                players.upsert(uuid, name);
                properties.setDefaults(uuid, defaults);
                players.refreshPlayerNames();

                if (!offlineMessages.findForReceiver(name).isEmpty() && player.isOnline()) {
                    Scheduler.runForEntityLater(player, () -> {
                        MessageUtils.sendMiniMessageIfPresent(player, "messages.mailmsg.have_unread");
                        Utils.msgPlaySound(properties, player);
                    }, 40);
                }
            } catch (Exception e) {
                plugin.getLogger().log(java.util.logging.Level.SEVERE, "Failed to process join for " + name, e);
            }
        });
    }

    @EventHandler
    public void playerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        latestRecipients.remove(player.getName());
        properties.invalidate(player.getUniqueId());
    }
}
