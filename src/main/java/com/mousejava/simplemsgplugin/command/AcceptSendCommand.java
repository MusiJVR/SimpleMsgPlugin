package com.mousejava.simplemsgplugin.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mousejava.simplemsgplugin.command.api.Cmd;
import com.mousejava.simplemsgplugin.command.api.ICommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import com.mousejava.simplemsgplugin.SimpleMsgPlugin;
import com.mousejava.simplemsgplugin.utils.DatabaseDriver;
import com.mousejava.simplemsgplugin.utils.MessageUtils;
import com.mousejava.simplemsgplugin.utils.Utils;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AcceptSendCommand implements ICommand {
    private final SimpleMsgPlugin plugin;
    private final DatabaseDriver dbDriver;

    public AcceptSendCommand(JavaPlugin plugin, DatabaseDriver dbDriver) {
        this.plugin = (SimpleMsgPlugin) plugin;
        this.dbDriver = dbDriver;
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> create() {
        return Cmd.playerCommand("acceptsend", "simplemsgplugin.acceptsend", this::executeAcceptSend)
                .build();
    }

    @Override
    public String description() {
        return "This command allows you to accept sending an offline message";
    }

    @Override
    public Set<String> aliases() {
        return Set.of("accsend");
    }

    private int executeAcceptSend(CommandContext<CommandSourceStack> ctx, Player player) {
        UUID uuid = player.getUniqueId();

        if (plugin.offlineReceiver.containsKey(uuid) && plugin.offlineMessages.containsKey(uuid)) {
            String playerReceiver = plugin.offlineReceiver.get(uuid);
            String msgOffline = plugin.offlineMessages.get(uuid);

            Map<String, Object> insertMap = new HashMap<>();
            insertMap.put("sender", player.getName());
            insertMap.put("receiver", playerReceiver);
            insertMap.put("message", msgOffline);
            dbDriver.insertData("offline_msg", insertMap);

            MessageUtils.sendMiniMessageIfPresent(player, "messages.acceptsend.send_offline_successfully");
            Utils.msgPlaySound(dbDriver, player);

            plugin.offlineReceiver.remove(uuid, playerReceiver);
            plugin.offlineMessages.remove(uuid, msgOffline);
        }

        return Command.SINGLE_SUCCESS;
    }
}
