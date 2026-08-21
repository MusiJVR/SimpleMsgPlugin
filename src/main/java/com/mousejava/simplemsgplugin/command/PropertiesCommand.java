package com.mousejava.simplemsgplugin.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mousejava.simplemsgplugin.property.PropertyValueArgumentType;
import com.mousejava.simplemsgplugin.command.api.Cmd;
import com.mousejava.simplemsgplugin.command.api.ICommand;
import com.mousejava.simplemsgplugin.property.PropertyDefinitions;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import com.mousejava.simplemsgplugin.repository.PropertiesRepository;
import com.mousejava.simplemsgplugin.utils.MessageUtils;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class PropertiesCommand implements ICommand {
    private final PropertiesRepository properties;

    public PropertiesCommand(PropertiesRepository properties) {
        this.properties = properties;
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
                                        Cmd.argument("value", PropertyValueArgumentType.propertyValue(PropertyDefinitions.asMap())),
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
            PropertyDefinitions.keySet().forEach(value -> {
                if (value.toLowerCase(Locale.ROOT).startsWith(remaining))
                    builder.suggest(value);
            });
            return builder.buildFuture();
        };
    }

    private int executeProperties(CommandContext<CommandSourceStack> ctx, Player player) {
        UUID uuid = player.getUniqueId();
        String propertyName = StringArgumentType.getString(ctx, "property").toLowerCase(Locale.ROOT);

        if (!PropertyDefinitions.contains(propertyName)) {
            MessageUtils.sendMiniMessageIfPresent(player, "messages.propertiesmsg.unknown_property");
            return Command.SINGLE_SUCCESS;
        }

        Object value = PropertyValueArgumentType.getValue(ctx, "value");

        if (value == null) {
            MessageUtils.sendMiniMessageIfPresent(player, "messages.propertiesmsg.invalid_value");
            return Command.SINGLE_SUCCESS;
        }

        properties.set(uuid, propertyName, value);

        MessageUtils.sendMiniMessageTransformed(player, "messages.propertiesmsg.property_set",
                msg -> msg
                        .replace("<property>", propertyName)
                        .replace("<value>", String.valueOf(value))
        );

        return Command.SINGLE_SUCCESS;
    }
}
