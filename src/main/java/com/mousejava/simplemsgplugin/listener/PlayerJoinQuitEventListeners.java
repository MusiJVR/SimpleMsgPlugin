package com.mousejava.simplemsgplugin.listener;

import com.mousejava.simplemsgplugin.SimpleMsgPlugin;
import com.mousejava.simplemsgplugin.repository.OfflineMessagesRepository;
import com.mousejava.simplemsgplugin.repository.PlayersRepository;
import com.mousejava.simplemsgplugin.repository.PropertiesRepository;
import com.mousejava.simplemsgplugin.database.DatabaseCacheManager;
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

import java.util.UUID;

public class PlayerJoinQuitEventListeners implements Listener {
    private final SimpleMsgPlugin plugin;
    private final PlayersRepository players;
    private final PropertiesRepository properties;
    private final OfflineMessagesRepository offlineMessages;
    private final DatabaseCacheManager cache;
    private final LatestRecipientsStorage latestRecipients;

    public PlayerJoinQuitEventListeners(JavaPlugin plugin, PlayersRepository players, PropertiesRepository properties, OfflineMessagesRepository offlineMessages, DatabaseCacheManager cache, LatestRecipientsStorage latestRecipients) {
        this.plugin = (SimpleMsgPlugin) plugin;
        this.players = players;
        this.properties = properties;
        this.offlineMessages = offlineMessages;
        this.cache = cache;
        this.latestRecipients = latestRecipients;
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        players.upsert(uuid, player.getName());

        properties.set(uuid, "sound", properties.getString(uuid, "sound", plugin.getConfig().getString("default_sound", "false")));
        properties.set(uuid, "volume", properties.getInt(uuid, "volume", plugin.getConfig().getInt("default_volume", 50)));
        properties.set(uuid, "confirm_sending", properties.getBoolean(uuid, "confirm_sending", plugin.getConfig().getBoolean("confirm_sending", true)));

        cache.refreshPlayerNames();

        if (!offlineMessages.findForReceiver(player.getName()).isEmpty()) {
            Scheduler.runForEntityLater(player, () -> {
                MessageUtils.sendMiniMessageIfPresent(player, "messages.mailmsg.have_unread");
                Utils.msgPlaySound(properties, player);
            }, 40);
        }
    }

    @EventHandler
    public void playerQuit(PlayerQuitEvent event) {
        latestRecipients.remove(event.getPlayer().getName());
    }
}
