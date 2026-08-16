package com.mousejava.simplemsgplugin.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mousejava.simplemsgplugin.command.api.Cmd;
import com.mousejava.simplemsgplugin.command.api.ICommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.entity.Player;
import com.mousejava.simplemsgplugin.utils.DatabaseDriver;
import com.mousejava.simplemsgplugin.utils.MessageUtils;

import java.util.Map;
import java.util.Set;

public class MailCommand implements ICommand {
    private final DatabaseDriver dbDriver;

    public MailCommand(DatabaseDriver dbDriver) {
        this.dbDriver = dbDriver;
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> create() {
        return Cmd.playerCommand("mailmsg", "simplemsgplugin.mailmsg", this::executeMail)
                .build();
    }

    @Override
    public String description() {
        return "This command allows you to view unread messages";
    }

    @Override
    public Set<String> aliases() {
        return Set.of("msgmail", "pmmail", "mailpm", "mail");
    }

    private int executeMail(CommandContext<CommandSourceStack> ctx, Player player) {
        String playerName = player.getName();
        dbDriver.selectData("sender, message", "offline_msg", "WHERE LOWER(receiver) = LOWER(?)", rs -> {
            if (!rs.isEmpty()) {
                MessageUtils.sendMiniMessageIfPresent(player, "messages.mailmsg.your_unread");

                for (Map<String, Object> i : rs) {
                    String senderName = (String) i.get("sender");
                    String messageText = (String) i.get("message");

                    MessageUtils.sendMiniMessageIfPresent(player, "messages.mailmsg.offline_pattern",
                            raw -> raw
                                    .replace("<sender>", senderName)
                                    .replace("<receiver>", playerName)
                                    .replace("<message>", messageText),
                            component -> component
                                    .hoverEvent(HoverEvent.showText(MessageUtils.safeText("messages.playermsg.click_send_reply")))
                                    .clickEvent(ClickEvent.suggestCommand("/msg " + senderName + " "))
                    );
                }

                dbDriver.deleteData("offline_msg", "LOWER(receiver) = LOWER(?)", playerName);
            } else {
                MessageUtils.sendMiniMessageIfPresent(player, "messages.mailmsg.no_unread");
            }
        }, playerName);

        return Command.SINGLE_SUCCESS;
    }
}
