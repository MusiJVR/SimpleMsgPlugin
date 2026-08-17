package com.mousejava.simplemsgplugin;

import com.mousejava.simplemsgplugin.metrics.BStatsMetrics;
import com.mousejava.simplemsgplugin.command.*;
import com.mousejava.simplemsgplugin.command.api.ICommand;
import com.mousejava.simplemsgplugin.database.DatabaseCacheManager;
import com.mousejava.simplemsgplugin.database.DatabaseConfig;
import com.mousejava.simplemsgplugin.database.DatabaseManager;
import com.mousejava.simplemsgplugin.database.SchemaInitializer;
import com.mousejava.simplemsgplugin.handler.PlayerJoinQuitEventHandlers;
import com.mousejava.simplemsgplugin.handler.PrivateChatHandler;
import com.mousejava.simplemsgplugin.repository.*;
import com.mousejava.simplemsgplugin.storage.LatestRecipientsStorage;
import com.mousejava.simplemsgplugin.storage.OfflineMessageStorage;
import com.mousejava.simplemsgplugin.utils.MessageUtils;
import com.mousejava.simplemsgplugin.utils.Scheduler;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public final class SimpleMsgPlugin extends JavaPlugin {
    private static final int SERVICE_ID = 33452;

    private static SimpleMsgPlugin instance;
    private DatabaseManager database;
    private DatabaseCacheManager cacheManager;
    private PlayersRepository playersRepository;
    private PropertiesRepository propertiesRepository;
    private OfflineMessagesRepository offlineMessagesRepository;
    private BlacklistRepository blacklistRepository;
    private final OfflineMessageStorage offlineMessageStorage = new OfflineMessageStorage();
    private final LatestRecipientsStorage latestRecipientsStorage = new LatestRecipientsStorage();

    public static SimpleMsgPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        Scheduler.init(this);
        MessageUtils.init(this);
        BStatsMetrics.init(this, SERVICE_ID);

        database = new DatabaseManager(getName() + "Pool", DatabaseConfig.from(getConfig()));

        playersRepository = new PlayersRepository(database);
        propertiesRepository = new PropertiesRepository(database);
        offlineMessagesRepository = new OfflineMessagesRepository(database);
        blacklistRepository = new BlacklistRepository(database);

        new SchemaInitializer(List.of(
                playersRepository,
                propertiesRepository,
                offlineMessagesRepository,
                blacklistRepository
        )).initialize();

        cacheManager = new DatabaseCacheManager(playersRepository);
        cacheManager.refreshPlayerNames();
        cacheManager.schedulePlayerNameRefresh(5 * 60 * 20L);

        registerCommands();

        getServer().getPluginManager().registerEvents(new PlayerJoinQuitEventHandlers(this, playersRepository, propertiesRepository, offlineMessagesRepository, cacheManager, latestRecipientsStorage), this);
        getServer().getPluginManager().registerEvents(new PrivateChatHandler(), this);
    }

    @Override
    public void onDisable() {
        if (database != null && database.isRunning())
            database.close();

        if (cacheManager != null)
            cacheManager.close();

        offlineMessageStorage.clear();
        latestRecipientsStorage.clear();
    }

    private void registerCommands() {
        List<ICommand> commands = List.of(
                new HelpCommand(this),
                new ReloadCommand(this),
                new PropertiesCommand(propertiesRepository),
                new PlayerMsgCommand(this, playersRepository, propertiesRepository, offlineMessagesRepository, blacklistRepository, cacheManager, offlineMessageStorage, latestRecipientsStorage),
                new ReplyMsgCommand(latestRecipientsStorage),
                new AcceptSendCommand(offlineMessageStorage, offlineMessagesRepository, propertiesRepository),
                new MailCommand(offlineMessagesRepository),
                new NotificationCommand(propertiesRepository),
                new PrivateChatCommand(),
                new BlacklistCommand(blacklistRepository, playersRepository)
        );

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            commands.forEach(command -> event.registrar().register(command.create(), command.description(), command.aliases()));
        });
    }
}
