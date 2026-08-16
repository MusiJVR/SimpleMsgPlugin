package com.mousejava.simplemsgplugin.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mousejava.simplemsgplugin.command.api.Cmd;
import com.mousejava.simplemsgplugin.command.api.ICommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import com.mousejava.simplemsgplugin.utils.DatabaseDriver;
import com.mousejava.simplemsgplugin.utils.MessageUtils;
import com.mousejava.simplemsgplugin.utils.Utils;
import org.bukkit.entity.Player;

import java.util.*;

public class NotificationCommand implements ICommand {
    private final DatabaseDriver dbDriver;

    public NotificationCommand(DatabaseDriver dbDriver) {
        this.dbDriver = dbDriver;
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> create() {
        return Cmd.playerCommand("notificationmsg", "simplemsgplugin.notificationmsg", "messages.notificationmsg.usage")
                .then(
                        Cmd.executesPlayer(
                                Cmd.argument("sound", StringArgumentType.word(), soundSuggestions()),
                                this::executeWithoutVolume
                        ).then(
                                Cmd.executesPlayer(
                                        Cmd.argument("volume", IntegerArgumentType.integer(0, 100), Cmd.fixedSuggestionsUnsorted("0", "25", "50", "75", "100")),
                                        this::executeWithVolume
                                )
                        )
                )
                .build();
    }

    @Override
    public String description() {
        return "This command allows you to change the notification sound";
    }

    @Override
    public Set<String> aliases() {
        return Set.of("msgnotification", "notifymsg", "msgnotify", "notifypm", "pmnotify", "notify");
    }

    private SuggestionProvider<CommandSourceStack> soundSuggestions() {
        return (ctx, builder) -> {
            String remaining = builder.getRemainingLowerCase();
            Registry.SOUNDS.forEach(sound -> {
                String name = sound.getKey().getKey();
                if (name.toLowerCase(Locale.ROOT).startsWith(remaining))
                    builder.suggest(name);
            });
            return builder.buildFuture();
        };
    }

    private int executeWithoutVolume(CommandContext<CommandSourceStack> ctx, Player player) {
        return applyNotificationSettings(ctx, player, null);
    }

    private int executeWithVolume(CommandContext<CommandSourceStack> ctx, Player player) {
        int volume = IntegerArgumentType.getInteger(ctx, "volume");
        return applyNotificationSettings(ctx, player, volume);
    }

    private int applyNotificationSettings(CommandContext<CommandSourceStack> ctx, Player player, Integer volume) {
        UUID uuid = player.getUniqueId();

        String soundName = getValidSound(StringArgumentType.getString(ctx, "sound"));
        if (soundName == null) {
            MessageUtils.sendMiniMessageIfPresent(player, "messages.notificationmsg.sound_missing");
            return Command.SINGLE_SUCCESS;
        }

        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("sound", soundName);
        if (volume != null) {
            updateMap.put("volume", volume);
        }

        dbDriver.updateData("sounds", updateMap, "uuid = ?", uuid);

        MessageUtils.sendMiniMessageIfPresent(player, "messages.notificationmsg.successfully_changed");
        Utils.msgPlaySound(dbDriver, player);
        return Command.SINGLE_SUCCESS;
    }

    private String getValidSound(String name) {
        try {
            Sound.valueOf(name.toUpperCase());
            return name.toLowerCase();
        } catch (Throwable ignored) {}

        try {
            NamespacedKey key = NamespacedKey.minecraft(name.toLowerCase());
            if (Registry.SOUNDS.get(key) != null) {
                return key.getKey();
            }
        } catch (Throwable ignored) {}

        return null;
    }
}
