package simplemsgplugin.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import simplemsgplugin.SimpleMsgPlugin;
import simplemsgplugin.chatgroups.Group;
import simplemsgplugin.chatgroups.GroupManager;
import simplemsgplugin.utils.MessageUtils;

import java.util.UUID;
import java.util.stream.Collectors;

public class PrivateChatCommand implements CommandExecutor {
    private final JavaPlugin plugin;

    public PrivateChatCommand() {
        this.plugin = SimpleMsgPlugin.getInstance();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player player)) return true;

        if (args.length == 0) {
            MessageUtils.sendMiniMessageIfPresent(sender, "messages.privatechat.usage.default");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                handleCreate(player, args);
                break;
            case "delete":
                handleDelete(player, args);
                break;
            case "invite":
                handleInvite(player, args);
                break;
            case "join":
                handleJoin(player, args);
                break;
            case "leave":
                handleLeave(player, args);
                break;
            case "kick":
                handleKick(player, args);
                break;
            case "info":
                handleInfo(player, args);
                break;
            default:
                MessageUtils.sendMiniMessageIfPresent(sender, "messages.privatechat.usage.default");
                break;
        }

        return true;
    }

    private void handleCreate(Player sender, String[] args) {
        if (args.length != 2) {
            MessageUtils.sendMiniMessageComponent(sender, "messages.privatechat.usage.create", component ->
                    component.hoverEvent(HoverEvent.showText(Component.text("/privatechat create ")))
                            .clickEvent(ClickEvent.suggestCommand("/privatechat create "))
            );
            return;
        }

        Group group = GroupManager.findGroupByPlayer(sender.getUniqueId());

        if (group == null) {
            String groupName = args[1];

            simplemsgplugin.chatgroups.Player player = new simplemsgplugin.chatgroups.Player(sender);
            group = GroupManager.createGroup(groupName, player);
            group.addPlayer(player);

            MessageUtils.sendMiniMessageIfPresent(
                    sender,
                    "messages.privatechat.create_successfully",
                    raw -> raw.replace("%group%", groupName),
                    component -> component
                            .hoverEvent(HoverEvent.showText(MessageUtils.safeText("messages.privatechat.chat_info")))
                            .clickEvent(ClickEvent.runCommand("/privatechat info"))
            );
        } else {
            MessageUtils.sendColoredIfPresent(sender, "messages.privatechat.you_already_in_private_chat");
        }
    }

    private void handleDelete(Player sender, String[] args) {
        if (args.length != 1) {
            MessageUtils.sendMiniMessageComponent(sender, "messages.privatechat.usage.delete",
                    component -> component
                            .hoverEvent(HoverEvent.showText(Component.text("/privatechat delete")))
                            .clickEvent(ClickEvent.suggestCommand("/privatechat delete"))
            );
            return;
        }

        Group group = GroupManager.findGroupByPlayer(sender.getUniqueId());

        if (group == null || !GroupManager.deleteGroup(sender.getUniqueId())) {
            MessageUtils.sendColoredIfPresent(sender, "messages.privatechat.you_not_owner");
        }
    }

    public void handleInvite(Player sender, String[] args) {
        if (args.length != 2) {
            MessageUtils.sendMiniMessageComponent(sender, "messages.privatechat.usage.invite",
                    component -> component
                            .hoverEvent(HoverEvent.showText(Component.text("/privatechat invite ")))
                            .clickEvent(ClickEvent.suggestCommand("/privatechat invite "))
            );
            return;
        }

        String playerName = args[1];
        if (sender.getName().equals(playerName)) {
            MessageUtils.sendColoredIfPresent(sender, "messages.privatechat.you_can_not_invite_yourself");
            return;
        }

        Group group = GroupManager.findGroupByPlayer(sender.getUniqueId());

        if (group != null) {
            if (group.getOwner().getId().equals(sender.getUniqueId())) {
                Player player = plugin.getServer().getPlayer(playerName);
                if (player != null) {
                    MessageUtils.sendMiniMessageTransformed(sender, "messages.privatechat.invite_successfully",
                            raw -> raw.replace("%player%", group.getName()));

                    MessageUtils.sendMiniMessageIfPresent(player, "messages.privatechat.invite.template",
                            raw -> raw
                                    .replace("%player%", sender.getName())
                                    .replace("%group%", group.getName()),
                            component -> component
                                    .replaceText(builder -> builder
                                    .match("%accept_text%")
                                    .replacement(MessageUtils.safeText("messages.privatechat.invite.accept_text")
                                            .hoverEvent(HoverEvent.showText(MessageUtils.safeText("messages.privatechat.invite.accept_hover_text")))
                                            .clickEvent(ClickEvent.runCommand("/privatechat join " + group.getId())))
                            )
                    );
                } else {
                    MessageUtils.sendColoredIfPresent(sender, "messages.playermissing");
                }
            } else {
                MessageUtils.sendColoredIfPresent(sender, "messages.privatechat.you_not_owner");
            }
        } else {
            MessageUtils.sendColoredIfPresent(sender, "messages.privatechat.you_not_in_private_chat");
        }
    }

    private void handleJoin(Player sender, String[] args) {
        if (args.length != 2) {
            MessageUtils.sendMiniMessageComponent(sender, "messages.privatechat.usage.join",
                    component -> component
                            .hoverEvent(HoverEvent.showText(Component.text("/privatechat join ")))
                            .clickEvent(ClickEvent.suggestCommand("/privatechat join "))
            );
            return;
        }

        Group group = GroupManager.findGroupByPlayer(sender.getUniqueId());

        if (group == null) {
            UUID groupId = null;
            boolean parseSuccess = false;

            try {
                groupId = UUID.fromString(args[1]);
            } catch (IllegalArgumentException e) {
                parseSuccess = true;
            }

            Group newGroup = (groupId != null) ? GroupManager.getGroup(groupId) : null;
            if (!parseSuccess && newGroup != null) {
                newGroup.addPlayer(new simplemsgplugin.chatgroups.Player(sender));

                MessageUtils.sendMiniMessageIfPresent(sender, "messages.privatechat.join_successfully",
                        raw -> raw.replace("%group%", newGroup.getName()),
                        component -> component
                                .hoverEvent(HoverEvent.showText(MessageUtils.safeText("messages.privatechat.chat_info")))
                                .clickEvent(ClickEvent.runCommand("/privatechat info"))
                );
            } else {
                MessageUtils.sendMiniMessageIfPresent(sender, "messages.privatechat.join_failed");
            }
        } else {
            MessageUtils.sendColoredIfPresent(sender, "messages.privatechat.you_already_in_private_chat");
        }
    }

    private void handleLeave(Player sender, String[] args) {
        if (args.length != 1) {
            MessageUtils.sendMiniMessageComponent(sender, "messages.privatechat.usage.leave",
                    component -> component
                            .hoverEvent(HoverEvent.showText(Component.text("/privatechat leave")))
                            .clickEvent(ClickEvent.suggestCommand("/privatechat leave"))
            );
            return;
        }

        Group group = GroupManager.findGroupByPlayer(sender.getUniqueId());

        if (group != null && group.removePlayer(sender.getUniqueId())) {
            MessageUtils.sendMiniMessageTransformed(sender, "messages.privatechat.leave_successfully",
                    raw -> raw.replace("%group%", group.getName()));
        } else {
            MessageUtils.sendColoredIfPresent(sender, "messages.privatechat.you_not_in_private_chat");
        }
    }

    private void handleKick(Player sender, String[] args) {
        if (args.length != 2) {
            MessageUtils.sendMiniMessageComponent(sender, "messages.privatechat.usage.kick",
                    component -> component
                            .hoverEvent(HoverEvent.showText(Component.text("/privatechat kick ")))
                            .clickEvent(ClickEvent.suggestCommand("/privatechat kick "))
            );
            return;
        }

        String playerName = args[1];
        if (sender.getName().equals(playerName)) {
            MessageUtils.sendColoredIfPresent(sender, "messages.privatechat.you_can_not_kick_yourself");
            return;
        }

        Group group = GroupManager.findGroupByPlayer(sender.getUniqueId());

        if (group != null) {
            if (group.getOwner().getId().equals(sender.getUniqueId())) {
                simplemsgplugin.chatgroups.Player player = group.getPlayerByName(playerName);
                if (player != null && group.removePlayer(player.getId())) {
                    MessageUtils.sendMiniMessageTransformed(sender, "messages.privatechat.kick_successfully",
                            raw -> raw.replace("%player%", playerName));

                    Player argPlayer = plugin.getServer().getPlayer(playerName);
                    if (argPlayer != null && argPlayer.isOnline()) {
                        MessageUtils.sendMiniMessageTransformed(argPlayer, "messages.privatechat.kick_notification",
                                raw -> raw.replace("%group%", group.getName()));
                    }
                } else {
                    MessageUtils.sendMiniMessageTransformed(sender, "messages.privatechat.kick_failed",
                            raw -> raw.replace("%player%", playerName));
                }
            } else {
                MessageUtils.sendColoredIfPresent(sender, "messages.privatechat.you_not_owner");
            }
        } else {
            MessageUtils.sendColoredIfPresent(sender, "messages.privatechat.you_not_in_private_chat");
        }
    }

    private void handleInfo(Player sender, String[] args) {
        if (args.length != 1) {
            MessageUtils.sendMiniMessageComponent(sender, "messages.privatechat.usage.info",
                    component -> component
                            .hoverEvent(HoverEvent.showText(Component.text("/privatechat info")))
                            .clickEvent(ClickEvent.suggestCommand("/privatechat info"))
            );
            return;
        }

        Group group = GroupManager.findGroupByPlayer(sender.getUniqueId());

        if (group != null) {
            MessageUtils.sendMiniMessageIfPresent(sender, "messages.privatechat.info.template",
                    raw -> raw
                            .replace("%group%", group.getName())
                            .replace("%owner%", group.getOwner().getName()),
                    component -> component
                            .replaceText(builder -> builder.match("%count_members%").replacement(
                                    Component.text(group.getPlayers().size())
                                            .hoverEvent(HoverEvent.showText(
                                                    Component.text(group.getPlayers().stream()
                                                            .map(simplemsgplugin.chatgroups.Player::getName)
                                                            .collect(Collectors.joining(", ")))
                                            ))
                            ))
                            .replaceText(builder -> builder.match("%leave_text%").replacement(
                                    MessageUtils.safeText("messages.privatechat.info.leave_text")
                                            .hoverEvent(HoverEvent.showText(MessageUtils.safeText("messages.privatechat.info.leave_hover_text")))
                                            .clickEvent(ClickEvent.runCommand("/privatechat leave"))
                            ))
                            .replaceText(builder -> builder.match("%invite_text%").replacement(
                                    MessageUtils.safeText("messages.privatechat.info.invite_text")
                                            .hoverEvent(HoverEvent.showText(MessageUtils.safeText("messages.privatechat.info.invite_hover_text")))
                                            .clickEvent(ClickEvent.suggestCommand("/privatechat invite "))
                            ))
            );
        } else {
            MessageUtils.sendColoredIfPresent(sender, "messages.privatechat.you_not_in_private_chat");
        }
    }
}
