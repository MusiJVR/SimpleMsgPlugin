package com.mousejava.simplemsgplugin.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mousejava.simplemsgplugin.property.PropertyValueArgumentType;
import com.mousejava.simplemsgplugin.command.api.Cmd;
import com.mousejava.simplemsgplugin.command.api.ICommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import com.mousejava.simplemsgplugin.utils.DatabaseDriver;
import com.mousejava.simplemsgplugin.utils.MessageUtils;

import java.util.*;

public class PropertiesCommand implements ICommand {
    private final DatabaseDriver dbDriver;

    private static final Map<String, PropertyValueArgumentType.PropertyType> PROPERTIES = Map.of(
            "confirm_sending", PropertyValueArgumentType.PropertyType.BOOLEAN
    );

    public PropertiesCommand(DatabaseDriver dbDriver) {
        this.dbDriver = dbDriver;
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> create() {
        return Cmd.playerCommand("propertiesmsg", "simplemsgplugin.propertiesmsg", "messages.propertiesmsg.usage")
                .then(
                        Cmd.executesPlayer(
                                Cmd.argument("property", StringArgumentType.word(), propertiesSuggestions()),
                                (ctx, player) -> Cmd.usage(player, "messages.propertiesmsg.usage")
                        ).then(
                                Cmd.executesPlayer(
                                        Cmd.argument("value", PropertyValueArgumentType.propertyValue(PROPERTIES)),
                                        this::executeProperties
                                )
                        )
                )
                .build();
    }

    @Override
    public String description() {
        return "This command allows you to set player properties";
    }

    @Override
    public Set<String> aliases() {
        return Set.of("propmsg", "msgprop");
    }

    private SuggestionProvider<CommandSourceStack> propertiesSuggestions() {
        return (ctx, builder) -> {
            String remaining = builder.getRemainingLowerCase();
            PROPERTIES.keySet().forEach(value -> {
                if (value.toLowerCase(Locale.ROOT).startsWith(remaining))
                    builder.suggest(value);
            });
            return builder.buildFuture();
        };
    }

    private int executeProperties(CommandContext<CommandSourceStack> ctx, Player player) {
        UUID uuid = player.getUniqueId();
        String playerName = player.getName();
        String propertyName = StringArgumentType.getString(ctx, "property").toLowerCase(Locale.ROOT);

        if (!PROPERTIES.containsKey(propertyName)) {
            MessageUtils.sendMiniMessageIfPresent(player, "messages.propertiesmsg.unknown_property");
            return Command.SINGLE_SUCCESS;
        }

        Object value = PropertyValueArgumentType.getValue(ctx, "value");

        if (value == null) {
            MessageUtils.sendMiniMessageIfPresent(player, "messages.propertiesmsg.confirm_sending.usage");
            return Command.SINGLE_SUCCESS;
        }

        Object finalValue = value;
        dbDriver.selectData("uuid", "properties", "WHERE uuid = ?", rs -> {
            if (rs.isEmpty()) {
                Map<String, Object> insertMap = new HashMap<>();
                insertMap.put("uuid", uuid);
                insertMap.put("player_name", playerName);
                insertMap.put(propertyName, finalValue);
                dbDriver.insertData("properties", insertMap);
            } else {
                Map<String, Object> updateMap = new HashMap<>();
                updateMap.put(propertyName, finalValue);
                dbDriver.updateData("properties", updateMap, "uuid = ? AND player_name = ?", uuid, playerName);
            }
        }, uuid);

        MessageUtils.sendMiniMessageTransformed(player, "messages.propertiesmsg.confirm_sending.property_set",
                msg -> msg
                        .replace("<property>", propertyName)
                        .replace("<value>", String.valueOf(finalValue))
        );

        return Command.SINGLE_SUCCESS;
    }
}
