package com.mousejava.simplemsgplugin;

import com.mousejava.simplemsgplugin.command.*;
import com.mousejava.simplemsgplugin.command.api.ICommand;
import com.mousejava.simplemsgplugin.database.DatabaseCacheManager;
import com.mousejava.simplemsgplugin.database.api.DatabaseConfig;
import com.mousejava.simplemsgplugin.database.api.DatabaseManager;
import com.mousejava.simplemsgplugin.database.api.SchemaRepository;
import com.mousejava.simplemsgplugin.database.dialect.*;
import com.mousejava.simplemsgplugin.database.repository.*;
import com.mousejava.simplemsgplugin.listener.*;
import com.mousejava.simplemsgplugin.service.*;
import com.mousejava.simplemsgplugin.storage.*;
import com.mousejava.simplemsgplugin.utils.MessageUtils;
import com.mousejava.simplemsgplugin.utils.Scheduler;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public final class SimpleMsgPlugin extends JavaPlugin {
    private static final int SERVICE_ID = 33452;
    private static final String PROJECT_ID = "simplemsgplugin";

    private static SimpleMsgPlugin instance;
    private DatabaseManager<SqlDialect> database;
    private DatabaseCacheManager cacheManager;
    private PlayersRepository playersRepository;
    private PropertiesRepository propertiesRepository;
    private OfflineMessagesRepository offlineMessagesRepository;
    private BlacklistRepository blacklistRepository;
    private SkinsRepository skinsRepository;
    private SkinService skinService;
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
        BStatsMetricsService.init(this, SERVICE_ID);
        UpdateCheckerService.init(this, PROJECT_ID);

        database = initializeDatabase();

        playersRepository = new PlayersRepository(database);
        propertiesRepository = new PropertiesRepository(database);
        offlineMessagesRepository = new OfflineMessagesRepository(database);
        blacklistRepository = new BlacklistRepository(database);
        skinsRepository = new SkinsRepository(database);

        List.of(
                playersRepository,
                propertiesRepository,
                offlineMessagesRepository,
                blacklistRepository,
                skinsRepository
        ).forEach(SchemaRepository::initializeSchema);

        skinService = new SkinService(skinsRepository);

        cacheManager = new DatabaseCacheManager(playersRepository);
        cacheManager.refreshPlayerNames();
        cacheManager.schedulePlayerNameRefresh(5 * 60 * 20L);

        registerListeners();
        registerCommands();
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

    private DatabaseManager<SqlDialect> initializeDatabase() {
        DatabaseConfig config = DatabaseConfig.from(getConfig(), getDataFolder());
        SqlDialect dialect = switch (config.type()) {
            case SQLITE -> new SqliteDialect();
            case MYSQL -> new MySqlDialect();
        };

        return new DatabaseManager<>(getName() + "Pool", config, dialect);
    }

    private void registerListeners() {
        List<Listener> listeners = List.of(
                new PlayerJoinQuitEventListeners(this, playersRepository, propertiesRepository, offlineMessagesRepository, cacheManager, latestRecipientsStorage),
                new PrivateChatListener(),
                new UpdateNotifyListener(this, propertiesRepository),
                new PlayerSkinListener(skinsRepository, skinService)
        );

        PluginManager pluginManager = getServer().getPluginManager();
        listeners.forEach(listener -> pluginManager.registerEvents(listener, this));
    }

    private void registerCommands() {
        List<ICommand> commands = List.of(
                new HelpCommand(this),
                new ReloadCommand(this),
                new PropertiesCommand(propertiesRepository),
                new PlayerMsgCommand(this, playersRepository, propertiesRepository, offlineMessagesRepository, blacklistRepository, cacheManager, offlineMessageStorage, latestRecipientsStorage, skinService),
                new ReplyMsgCommand(latestRecipientsStorage),
                new AcceptSendCommand(offlineMessageStorage, offlineMessagesRepository, propertiesRepository),
                new MailCommand(offlineMessagesRepository, skinService),
                new NotificationCommand(propertiesRepository),
                new PrivateChatCommand(),
                new BlacklistCommand(blacklistRepository, playersRepository)
        );

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands registrar = event.registrar();
            commands.forEach(command -> registrar.register(command.create(), command.description(), command.aliases()));
        });
    }
}
