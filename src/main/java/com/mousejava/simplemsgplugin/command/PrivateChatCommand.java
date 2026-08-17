package com.mousejava.simplemsgplugin.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mousejava.simplemsgplugin.chatgroups.GroupPlayer;
import com.mousejava.simplemsgplugin.command.api.Cmd;
import com.mousejava.simplemsgplugin.command.api.ICommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.entity.Player;
import com.mousejava.simplemsgplugin.chatgroups.Group;
import com.mousejava.simplemsgplugin.chatgroups.GroupManager;
import com.mousejava.simplemsgplugin.utils.MessageUtils;

import java.util.*;
import java.util.stream.Collectors;

public class PrivateChatCommand implements ICommand {
    public PrivateChatCommand() {

    }

    @Override
    public LiteralCommandNode<CommandSourceStack> create() {
        return Cmd.playerCommand("privatechat", "simplemsgplugin.privatechat", "messages.privatechat.usage.default")
                .then(buildCreate())
                .then(buildDelete())
                .then(buildInvite())
                .then(buildJoin())
                .then(buildLeave())
                .then(buildKick())
                .then(buildInfo())
                .build();
    }

    @Override
    public String description() {
        return "This command allows you to interact with private chats";
    }

    @Override
    public Set<String> aliases() {
        return Set.of("pvch", "pc");
    }

    private static boolean isInGroup(CommandSourceStack source) {
        return source.getSender() instanceof Player player
                && GroupManager.findGroupByPlayer(player.getUniqueId()) != null;
    }

    private static boolean isGroupOwner(CommandSourceStack source) {
        if (!(source.getSender() instanceof Player player)) return false;
        Group group = GroupManager.findGroupByPlayer(player.getUniqueId());
        return group != null && group.getOwner().getId().equals(player.getUniqueId());
    }

    private LiteralArgumentBuilder<CommandSourceStack> buildCreate() {
        return Cmd.playerCommand("create",
                (ctx, player) -> {
                    MessageUtils.sendMiniMessageComponent(player, "messages.privatechat.usage.create", component ->
                            component.hoverEvent(HoverEvent.showText(Component.text("/privatechat create ")))
                                    .clickEvent(ClickEvent.suggestCommand("/privatechat create ")));
                    return Command.SINGLE_SUCCESS;
                }
        ).then(
                Cmd.executesPlayer(
                        Cmd.argument("group_name", StringArgumentType.word()),
                        this::executeCreate
                )
        );
    }

    private int executeCreate(CommandContext<CommandSourceStack> ctx, Player player) {
        String groupName = StringArgumentType.getString(ctx, "group_name");
        Group existing = GroupManager.findGroupByPlayer(player.getUniqueId());

        if (existing != null) {
            MessageUtils.sendMiniMessageIfPresent(player, "messages.privatechat.you_already_in_private_chat");
            return Command.SINGLE_SUCCESS;
        }

        GroupPlayer groupPlayer = new GroupPlayer(player);
        Group group = GroupManager.createGroup(groupName, groupPlayer);
        group.addPlayer(groupPlayer);

        MessageUtils.sendMiniMessageIfPresent(player, "messages.privatechat.create_successfully",
                raw -> raw.replace("<group>", groupName),
                component -> component
                        .hoverEvent(HoverEvent.showText(MessageUtils.safeText("messages.privatechat.chat_info")))
                        .clickEvent(ClickEvent.runCommand("/privatechat info")));
        return Command.SINGLE_SUCCESS;
    }

    private LiteralArgumentBuilder<CommandSourceStack> buildDelete() {
        return Cmd.executesPlayer(
                Cmd.literal("delete")
                        .requires(PrivateChatCommand::isGroupOwner),
                this::executeDelete
        );
    }

    private int executeDelete(CommandContext<CommandSourceStack> ctx, Player player) {
        if (!GroupManager.deleteGroup(player.getUniqueId())) {
            MessageUtils.sendMiniMessageIfPresent(player, "messages.privatechat.you_not_owner");
        }
        return Command.SINGLE_SUCCESS;
    }

    private LiteralArgumentBuilder<CommandSourceStack> buildInvite() {
        return Cmd.executesPlayer(
                Cmd.literal("invite")
                        .requires(PrivateChatCommand::isGroupOwner),
                (ctx, player) -> {
                    MessageUtils.sendMiniMessageComponent(player, "messages.privatechat.usage.invite",
                            component -> component
                                    .hoverEvent(HoverEvent.showText(Component.text("/privatechat invite ")))
                                    .clickEvent(ClickEvent.suggestCommand("/privatechat invite ")));
                    return Command.SINGLE_SUCCESS;
                }
        ).then(
                Cmd.executesPlayer(
                        Cmd.argument("player", StringArgumentType.word(), Cmd.ONLINE_PLAYERS),
                        this::executeInvite
                )
        );
    }

    private int executeInvite(CommandContext<CommandSourceStack> ctx, Player player) {
        String playerName = StringArgumentType.getString(ctx, "player");

        if (player.getName().equalsIgnoreCase(playerName)) {
            MessageUtils.sendMiniMessageIfPresent(player, "messages.privatechat.you_can_not_invite_yourself");
            return Command.SINGLE_SUCCESS;
        }

        Group group = GroupManager.findGroupByPlayer(player.getUniqueId());
        if (group == null) {
            MessageUtils.sendMiniMessageIfPresent(player, "messages.privatechat.you_not_in_private_chat");
            return Command.SINGLE_SUCCESS;
        }

        Player target = Cmd.resolveOnlinePlayer(playerName).orElse(null);
        if (target == null) {
            MessageUtils.sendMiniMessageIfPresent(player, "messages.playermissing");
            return Command.SINGLE_SUCCESS;
        }

        MessageUtils.sendMiniMessageTransformed(player, "messages.privatechat.invite_successfully",
                raw -> raw.replace("<player>", group.getName()));

        MessageUtils.sendMiniMessageIfPresent(target, "messages.privatechat.invite.template",
                raw -> raw
                        .replace("<player>", player.getName())
                        .replace("<group>", group.getName()),
                component -> component.replaceText(builder -> builder
                        .match("<accept_text>")
                        .replacement(MessageUtils.safeText("messages.privatechat.invite.accept_text")
                                .hoverEvent(HoverEvent.showText(MessageUtils.safeText("messages.privatechat.invite.accept_hover_text")))
                                .clickEvent(ClickEvent.runCommand("/privatechat join " + group.getId())))));

        return Command.SINGLE_SUCCESS;
    }

    private LiteralArgumentBuilder<CommandSourceStack> buildJoin() {
        return Cmd.playerCommand("join",
                (ctx, player) -> {
                    MessageUtils.sendMiniMessageComponent(player, "messages.privatechat.usage.join",
                            component -> component
                                    .hoverEvent(HoverEvent.showText(Component.text("/privatechat join ")))
                                    .clickEvent(ClickEvent.suggestCommand("/privatechat join ")));
                    return Command.SINGLE_SUCCESS;
                }
        ).then(
                Cmd.executesPlayer(
                        Cmd.argument("group_id", StringArgumentType.word()),
                        this::executeJoin
                )
        );
    }

    private int executeJoin(CommandContext<CommandSourceStack> ctx, Player player) {
        Group existing = GroupManager.findGroupByPlayer(player.getUniqueId());
        if (existing != null) {
            MessageUtils.sendMiniMessageIfPresent(player, "messages.privatechat.you_already_in_private_chat");
            return Command.SINGLE_SUCCESS;
        }

        String groupIdInput = StringArgumentType.getString(ctx, "group_id");
        UUID groupId;
        try {
            groupId = UUID.fromString(groupIdInput);
        } catch (IllegalArgumentException e) {
            MessageUtils.sendMiniMessageIfPresent(player, "messages.privatechat.join_failed");
            return Command.SINGLE_SUCCESS;
        }

        Group targetGroup = GroupManager.getGroup(groupId);
        if (targetGroup == null) {
            MessageUtils.sendMiniMessageIfPresent(player, "messages.privatechat.join_failed");
            return Command.SINGLE_SUCCESS;
        }

        targetGroup.addPlayer(new GroupPlayer(player));

        MessageUtils.sendMiniMessageIfPresent(player, "messages.privatechat.join_successfully",
                raw -> raw.replace("<group>", targetGroup.getName()),
                component -> component
                        .hoverEvent(HoverEvent.showText(MessageUtils.safeText("messages.privatechat.chat_info")))
                        .clickEvent(ClickEvent.runCommand("/privatechat info")));
        return Command.SINGLE_SUCCESS;
    }

    private LiteralArgumentBuilder<CommandSourceStack> buildLeave() {
        return Cmd.executesPlayer(
                Cmd.literal("leave")
                        .requires(PrivateChatCommand::isInGroup),
                this::executeLeave
        );
    }

    private int executeLeave(CommandContext<CommandSourceStack> ctx, Player player) {
        Group group = GroupManager.findGroupByPlayer(player.getUniqueId());
        if (group != null && group.removePlayer(player.getUniqueId())) {
            MessageUtils.sendMiniMessageTransformed(player, "messages.privatechat.leave_successfully",
                    raw -> raw.replace("<group>", group.getName()));
        } else {
            MessageUtils.sendMiniMessageIfPresent(player, "messages.privatechat.you_not_in_private_chat");
        }
        return Command.SINGLE_SUCCESS;
    }

    private SuggestionProvider<CommandSourceStack> groupMemberSuggestions() {
        return (ctx, builder) -> {
            String remaining = builder.getRemainingLowerCase();
            if (ctx.getSource().getSender() instanceof Player player) {
                Group group = GroupManager.findGroupByPlayer(player.getUniqueId());
                if (group != null) {
                    group.getPlayers().stream()
                            .map(GroupPlayer::getName)
                            .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(remaining))
                            .forEach(builder::suggest);
                }
            }
            return builder.buildFuture();
        };
    }

    private LiteralArgumentBuilder<CommandSourceStack> buildKick() {
        return Cmd.executesPlayer(
                Cmd.literal("kick")
                        .requires(PrivateChatCommand::isGroupOwner),
                (ctx, player) -> {
                    MessageUtils.sendMiniMessageComponent(player, "messages.privatechat.usage.kick",
                            component -> component
                                    .hoverEvent(HoverEvent.showText(Component.text("/privatechat kick ")))
                                    .clickEvent(ClickEvent.suggestCommand("/privatechat kick ")));
                    return Command.SINGLE_SUCCESS;
                }
        ).then(
                Cmd.executesPlayer(
                        Cmd.argument("player", StringArgumentType.word(), groupMemberSuggestions()),
                        this::executeKick
                )
        );
    }

    private int executeKick(CommandContext<CommandSourceStack> ctx, Player player) {
        String playerName = StringArgumentType.getString(ctx, "player");

        if (player.getName().equalsIgnoreCase(playerName)) {
            MessageUtils.sendMiniMessageIfPresent(player, "messages.privatechat.you_can_not_kick_yourself");
            return Command.SINGLE_SUCCESS;
        }

        Group group = GroupManager.findGroupByPlayer(player.getUniqueId());
        if (group == null) {
            MessageUtils.sendMiniMessageIfPresent(player, "messages.privatechat.you_not_in_private_chat");
            return Command.SINGLE_SUCCESS;
        }

        GroupPlayer target = group.getPlayerByName(playerName);
        if (target != null && group.removePlayer(target.getId())) {
            MessageUtils.sendMiniMessageTransformed(player, "messages.privatechat.kick_successfully",
                    raw -> raw.replace("<player>", playerName));

            Cmd.resolveOnlinePlayer(playerName).ifPresent(online ->
                    MessageUtils.sendMiniMessageTransformed(online, "messages.privatechat.kick_notification",
                            raw -> raw.replace("<group>", group.getName())));
        } else {
            MessageUtils.sendMiniMessageTransformed(player, "messages.privatechat.kick_failed",
                    raw -> raw.replace("<player>", playerName));
        }

        return Command.SINGLE_SUCCESS;
    }

    private LiteralArgumentBuilder<CommandSourceStack> buildInfo() {
        return Cmd.executesPlayer(
                Cmd.literal("info")
                        .requires(PrivateChatCommand::isInGroup),
                this::executeInfo
        );
    }

    private int executeInfo(CommandContext<CommandSourceStack> ctx, Player player) {
        Group group = GroupManager.findGroupByPlayer(player.getUniqueId());
        if (group == null) {
            MessageUtils.sendMiniMessageIfPresent(player, "messages.privatechat.you_not_in_private_chat");
            return Command.SINGLE_SUCCESS;
        }

        MessageUtils.sendMiniMessageIfPresent(player, "messages.privatechat.info.template",
                raw -> raw
                        .replace("<group>", group.getName())
                        .replace("<owner>", group.getOwner().getName()),
                component -> component
                        .replaceText(builder -> builder.match("<count_members>").replacement(
                                Component.text(group.getPlayers().size())
                                        .hoverEvent(HoverEvent.showText(
                                                Component.text(group.getPlayers().stream()
                                                        .map(GroupPlayer::getName)
                                                        .collect(Collectors.joining(", ")))))))
                        .replaceText(builder -> builder.match("<leave_text>").replacement(
                                MessageUtils.safeText("messages.privatechat.info.leave_text")
                                        .hoverEvent(HoverEvent.showText(MessageUtils.safeText("messages.privatechat.info.leave_hover_text")))
                                        .clickEvent(ClickEvent.runCommand("/privatechat leave"))))
                        .replaceText(builder -> builder.match("<invite_text>").replacement(
                                MessageUtils.safeText("messages.privatechat.info.invite_text")
                                        .hoverEvent(HoverEvent.showText(MessageUtils.safeText("messages.privatechat.info.invite_hover_text")))
                                        .clickEvent(ClickEvent.suggestCommand("/privatechat invite ")))));
        return Command.SINGLE_SUCCESS;
    }
}
