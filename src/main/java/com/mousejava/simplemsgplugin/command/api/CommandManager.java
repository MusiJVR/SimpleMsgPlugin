package com.mousejava.simplemsgplugin.command.api;

import com.mousejava.simplemsgplugin.command.*;
import com.mousejava.simplemsgplugin.utils.DatabaseCacheManager;
import com.mousejava.simplemsgplugin.utils.DatabaseDriver;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public final class CommandManager {
    public static void init(JavaPlugin plugin, DatabaseDriver database, DatabaseCacheManager cacheManager) {
        List<ICommand> commands = List.of(
                new HelpCommand(plugin),
                new ReloadCommand(plugin),
                new PropertiesCommand(database),
                new PlayerMsgCommand(plugin, database, cacheManager),
                new ReplyMsgCommand(plugin),
                new AcceptSendCommand(plugin, database),
                new MailCommand(database),
                new NotificationCommand(database),
                new PrivateChatCommand(),
                new BlacklistCommand(database)
        );

        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            commands.forEach(command -> event.registrar().register(command.create(), command.description(), command.aliases()));
        });
    }
}
