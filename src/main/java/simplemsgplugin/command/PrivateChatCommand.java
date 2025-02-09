package simplemsgplugin.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import simplemsgplugin.SimpleMsgPlugin;
import simplemsgplugin.chatgroups.Group;
import simplemsgplugin.chatgroups.GroupManager;
import simplemsgplugin.utils.ColorUtils;

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
            sender.sendMessage(MiniMessage.builder().build()
                    .deserialize(SimpleMsgPlugin.getInstance().getConfig().getString("messages.privatechat.usage.default")));
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
                sender.sendMessage(MiniMessage.builder().build()
                        .deserialize(SimpleMsgPlugin.getInstance().getConfig().getString("messages.privatechat.usage.default")));
                break;
        }

        return true;
    }

    private void handleCreate(Player sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(MiniMessage.builder().build()
                    .deserialize(SimpleMsgPlugin.getInstance().getConfig().getString("messages.privatechat.usage.create"))
                    .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("/privatechat create ")))
                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/privatechat create ")));
            return;
        }
        String groupName = args[1];

        simplemsgplugin.chatgroups.Player player = new simplemsgplugin.chatgroups.Player(sender);
        Group group = GroupManager.createGroup(groupName, player);
        group.addPlayer(player);

        sender.sendMessage(MiniMessage.builder().build()
                .deserialize(SimpleMsgPlugin.getInstance().getConfig().getString("messages.privatechat.create_successfully").replace("%group%", groupName))
                .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text(SimpleMsgPlugin.getInstance().getConfig().getString("messages.privatechat.chat_info"))))
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/privatechat info")));
    }

    private void handleDelete(Player sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(MiniMessage.builder().build()
                    .deserialize(SimpleMsgPlugin.getInstance().getConfig().getString("messages.privatechat.usage.delete"))
                    .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("/privatechat delete")))
                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/privatechat delete")));
            return;
        }

        Group group = GroupManager.findGroupByPlayer(sender.getUniqueId());

        if (group == null || !GroupManager.deleteGroup(sender.getUniqueId())) {
            sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.privatechat.you_not_owner")));
        }
    }

    public void handleInvite(Player sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(MiniMessage.builder().build()
                    .deserialize(SimpleMsgPlugin.getInstance().getConfig().getString("messages.privatechat.usage.invite"))
                    .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("/privatechat invite ")))
                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/privatechat invite ")));
            return;
        }

        String playerName = args[1];
        if (sender.getName().equals(playerName)) {
            sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.privatechat.you_can_not_invite_yourself")));
            return;
        }

        Group group = GroupManager.findGroupByPlayer(sender.getUniqueId());

        if (group != null) {
            if (group.getOwner().getId().equals(sender.getUniqueId())) {
                Player player = plugin.getServer().getPlayer(playerName);
                if (player != null) {
                    sender.sendMessage(MiniMessage.builder().build()
                            .deserialize(SimpleMsgPlugin.getInstance().getConfig().getString("messages.privatechat.invite_successfully")
                                    .replace("%player%", group.getName())));

                    player.sendMessage(MiniMessage.builder().build()
                            .deserialize(SimpleMsgPlugin.getInstance().getConfig().getString("messages.privatechat.invite.template")
                                    .replace("%player%", sender.getName())
                                    .replace("%group%", group.getName()))
                            .replaceText(builder -> builder.match("%accept_text%").replacement(Component.text(SimpleMsgPlugin.getInstance().getConfig().getString("messages.privatechat.invite.accept_text"))
                                    .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text(SimpleMsgPlugin.getInstance().getConfig().getString("messages.privatechat.invite.accept_hover_text"))))
                                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/privatechat join " + group.getId())))));
                } else {
                    sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.playermissing")));
                }
            } else {
                sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.privatechat.you_not_owner")));
            }
        } else {
            sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.privatechat.you_not_in_private_chat")));
        }
    }

    private void handleJoin(Player sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(MiniMessage.builder().build()
                    .deserialize(SimpleMsgPlugin.getInstance().getConfig().getString("messages.privatechat.usage.join"))
                    .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("/privatechat join ")))
                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/privatechat join ")));
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

                sender.sendMessage(MiniMessage.builder().build()
                        .deserialize(SimpleMsgPlugin.getInstance().getConfig().getString("messages.privatechat.join_successfully")
                                .replace("%group%", newGroup.getName()))
                        .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text(SimpleMsgPlugin.getInstance().getConfig().getString("messages.privatechat.chat_info"))))
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/privatechat info")));
            } else {
                sender.sendMessage(MiniMessage.builder().build()
                        .deserialize(SimpleMsgPlugin.getInstance().getConfig().getString("messages.privatechat.join_failed")));
            }
        } else {
            sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.privatechat.you_already_in_private_chat")));
        }
    }

    private void handleLeave(Player sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(MiniMessage.builder().build()
                    .deserialize(SimpleMsgPlugin.getInstance().getConfig().getString("messages.privatechat.usage.leave"))
                    .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("/privatechat leave")))
                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/privatechat leave")));
            return;
        }

        Group group = GroupManager.findGroupByPlayer(sender.getUniqueId());

        if (group != null && group.removePlayer(sender.getUniqueId())) {
            sender.sendMessage(MiniMessage.builder().build()
                    .deserialize(SimpleMsgPlugin.getInstance().getConfig().getString("messages.privatechat.leave_successfully")
                            .replace("%group%", group.getName())));
        } else {
            sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.privatechat.you_not_in_private_chat")));
        }
    }

    private void handleKick(Player sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(MiniMessage.builder().build()
                    .deserialize(SimpleMsgPlugin.getInstance().getConfig().getString("messages.privatechat.usage.kick"))
                    .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("/privatechat kick ")))
                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/privatechat kick ")));
            return;
        }

        String playerName = args[1];
        if (sender.getName().equals(playerName)) {
            sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.privatechat.you_can_not_kick_yourself")));
            return;
        }

        Group group = GroupManager.findGroupByPlayer(sender.getUniqueId());

        if (group != null) {
            if (group.getOwner().getId().equals(sender.getUniqueId())) {
                simplemsgplugin.chatgroups.Player player = group.getPlayerByName(playerName);
                if (player != null && group.removePlayer(player.getId())) {
                    sender.sendMessage(MiniMessage.builder().build()
                            .deserialize(SimpleMsgPlugin.getInstance().getConfig().getString("messages.privatechat.kick_successfully")
                                    .replace("%player%", playerName)));

                    Player argPlayer = plugin.getServer().getPlayer(playerName);
                    if (argPlayer != null && argPlayer.isOnline()) {
                        argPlayer.sendMessage(MiniMessage.builder().build()
                                .deserialize(SimpleMsgPlugin.getInstance().getConfig().getString("messages.privatechat.kick_notification")
                                        .replace("%group%", group.getName())));
                    }
                } else {
                    sender.sendMessage(MiniMessage.builder().build()
                            .deserialize(SimpleMsgPlugin.getInstance().getConfig().getString("messages.privatechat.kick_failed")
                                    .replace("%player%", playerName)));
                }
            } else {
                sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.privatechat.you_not_owner")));
            }
        } else {
            sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.privatechat.you_not_in_private_chat")));
        }
    }

    private void handleInfo(Player sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(MiniMessage.builder().build()
                    .deserialize(SimpleMsgPlugin.getInstance().getConfig().getString("messages.privatechat.usage.info"))
                    .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("/privatechat info")))
                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/privatechat info")));
            return;
        }

        Group group = GroupManager.findGroupByPlayer(sender.getUniqueId());

        if (group != null) {
            sender.sendMessage(MiniMessage.builder().build()
                    .deserialize(SimpleMsgPlugin.getInstance().getConfig().getString("messages.privatechat.info.template")
                            .replace("%group%", group.getName())
                            .replace("%owner%", group.getOwner().getName()))
                    .replaceText(builder -> builder.match("%count_members%").replacement(Component.text(group.getPlayers().size())
                            .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text(group.getPlayers().stream()
                                    .map(simplemsgplugin.chatgroups.Player::getName)
                                    .collect(Collectors.joining(", ")))))))
                    .replaceText(builder -> builder.match("%leave_text%").replacement(Component.text(SimpleMsgPlugin.getInstance().getConfig().getString("messages.privatechat.info.leave_text"))
                            .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text(SimpleMsgPlugin.getInstance().getConfig().getString("messages.privatechat.info.leave_hover_text"))))
                            .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/privatechat leave"))))
                    .replaceText(builder -> builder.match("%invite_text%").replacement(Component.text(SimpleMsgPlugin.getInstance().getConfig().getString("messages.privatechat.info.invite_text"))
                            .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text(SimpleMsgPlugin.getInstance().getConfig().getString("messages.privatechat.info.invite_hover_text"))))
                            .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/privatechat invite ")))));
        } else {
            sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.privatechat.you_not_in_private_chat")));
        }
    }
}
