package com.mousejava.simplemsgplugin.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mousejava.simplemsgplugin.utils.*;
import com.mousejava.simplemsgplugin.command.api.Cmd;
import com.mousejava.simplemsgplugin.command.api.ICommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.mousejava.simplemsgplugin.SimpleMsgPlugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class PlayerMsgCommand implements ICommand {
    private final SimpleMsgPlugin plugin;
    private final DatabaseDriver dbDriver;
    private final DatabaseCacheManager cacheManager;

    public PlayerMsgCommand(JavaPlugin plugin, DatabaseDriver dbDriver, DatabaseCacheManager cacheManager) {
        this.plugin = (SimpleMsgPlugin) plugin;
        this.dbDriver = dbDriver;
        this.cacheManager = cacheManager;
        this.cacheManager.createCache("player_names", "player_name", "sounds", null);
        this.cacheManager.scheduleAutoRefresh("player_names", "player_name", "sounds", null, 5 * 60 * 20);
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> create() {
        return Cmd.senderCommand("playermsg", "simplemsgplugin.playermsg", "messages.playermsg.usage")
                .then(
                        Cmd.executesSender(
                                Cmd.argument("player", StringArgumentType.word(), playersSuggestions()),
                                (ctx, sender) -> Cmd.usage(sender, "messages.playermsg.usage")
                        ).then(
                                Cmd.executesSender(
                                        Cmd.argument("message", StringArgumentType.greedyString()),
                                        this::executePlayerMsg
                                )
                        )
                )
                .build();
    }

    @Override
    public String description() {
        return "This command allows you to send private messages to the player";
    }

    @Override
    public Set<String> aliases() {
        return Set.of("msg", "pm", "message", "tell", "w");
    }

    private SuggestionProvider<CommandSourceStack> playersSuggestions() {
        return (ctx, builder) -> {
            String remaining = builder.getRemainingLowerCase();
            StringRange range = StringRange.between(builder.getStart(), builder.getInput().length());

            Set<String> onlineNames = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(remaining))
                    .collect(Collectors.toCollection(() -> new TreeSet<>(String.CASE_INSENSITIVE_ORDER)));

            List<String> offlineNames = new ArrayList<>();
            List<Map<String, Object>> cache = cacheManager.getCache("player_names");
            if (cache != null) {
                for (Map<String, Object> row : cache) {
                    Object nameObj = row.get("player_name");
                    if (nameObj == null) continue;

                    String name = nameObj.toString();
                    if (onlineNames.contains(name)) continue;

                    if (name.toLowerCase(Locale.ROOT).startsWith(remaining)) {
                        offlineNames.add(name);
                    }
                }
                offlineNames.sort(String.CASE_INSENSITIVE_ORDER);
            }

            List<Suggestion> suggestions = new ArrayList<>();
            onlineNames.forEach(name -> suggestions.add(new Suggestion(range, name)));
            offlineNames.forEach(name -> suggestions.add(new Suggestion(range, name)));

            return CompletableFuture.completedFuture(new Suggestions(range, suggestions));
        };
    }

    private int executePlayerMsg(CommandContext<CommandSourceStack> ctx, CommandSender sender) {
        String playerNameInput = StringArgumentType.getString(ctx, "player");
        String message = StringArgumentType.getString(ctx, "message").trim();

        Player targetOnline = Cmd.resolveOnlinePlayer(playerNameInput).orElse(null);

        if (targetOnline == null) {
            if (sender instanceof Player player)
                handleOfflineTarget(player, playerNameInput, message);
            else
                MessageUtils.sendMiniMessageIfPresent(sender, "messages.playermsg.not_send_offline_from_console");

            return Command.SINGLE_SUCCESS;
        }

        UUID senderUuid = sender instanceof Player player ? player.getUniqueId() : null;

        dbDriver.selectData("uuid", "sounds", "WHERE LOWER(player_name) = LOWER(?)", rsArgPlayer -> {
            if (rsArgPlayer.isEmpty()) {
                notifySender(sender, "messages.playermsg.missing");
                return;
            }
            String uuidArgPlayer = (String) rsArgPlayer.get(0).get("uuid");

            if (senderUuid == null) {
                deliverMessage(sender, targetOnline, message);
                return;
            }

            if (Objects.equals(senderUuid.toString(), uuidArgPlayer) && !plugin.getConfig().getBoolean("send_msg_yourself")) {
                notifySender(sender, "messages.playermsg.not_send_youself");
                return;
            }

            dbDriver.selectData("uuid", "blacklist", "WHERE blocked_uuid = ?", rsBlockFirst -> {
                for (Map<String, Object> row : rsBlockFirst) {
                    if (Objects.equals(row.get("uuid"), uuidArgPlayer)) {
                        notifySender(sender, "messages.blacklist.you_cannot_send");
                        return;
                    }
                }

                dbDriver.selectData("blocked_uuid", "blacklist", "WHERE uuid = ?", rsBlockSecond -> {
                    for (Map<String, Object> row : rsBlockSecond) {
                        if (Objects.equals(row.get("blocked_uuid"), uuidArgPlayer)) {
                            notifySender(sender, "messages.blacklist.you_have_blocked");
                            return;
                        }
                    }
                    deliverMessage(sender, targetOnline, message);
                }, senderUuid.toString());
            }, senderUuid.toString());
        }, targetOnline.getName());

        return Command.SINGLE_SUCCESS;
    }

    private void deliverMessage(CommandSender sender, Player target, String message) {
        String senderName = sender.getName();
        String targetName = target.getName();

        MessageUtils.sendMiniMessageIfPresent(sender, "messages.playermsg.sender_pattern",
                raw -> raw
                        .replace("<sender>", senderName)
                        .replace("<receiver>", targetName)
                        .replace("<message>", message),
                component -> component
                        .hoverEvent(HoverEvent.showText(MessageUtils.safeText("messages.playermsg.click_send_reply")))
                        .clickEvent(ClickEvent.suggestCommand("/msg " + targetName + " "))
        );

        MessageUtils.sendMiniMessageIfPresent(target, "messages.playermsg.receiver_pattern",
                raw -> raw
                        .replace("<sender>", senderName)
                        .replace("<receiver>", targetName)
                        .replace("<message>", message),
                component -> component
                        .hoverEvent(HoverEvent.showText(MessageUtils.safeText("messages.playermsg.click_send_reply")))
                        .clickEvent(ClickEvent.suggestCommand("/msg " + senderName + " "))
        );

        Utils.msgPlaySound(dbDriver, target);

        if (sender instanceof Player) {
            plugin.latestRecipients.put(senderName, targetName);
            plugin.latestRecipients.put(targetName, senderName);
        }
    }

    private void handleOfflineTarget(Player sender, String playerNameInput, String message) {
        Optional<String> resolvedOffline = resolveOfflinePlayerName(playerNameInput);
        if (resolvedOffline.isEmpty()) {
            MessageUtils.sendMiniMessageIfPresent(sender, "messages.playermsg.missing");
            return;
        }

        sendOfflineMessage(sender, sender.getUniqueId(), resolvedOffline.get(), message);
    }

    private Optional<String> resolveOfflinePlayerName(String input) {
        List<Map<String, Object>> cache = cacheManager.getCache("player_names");
        if (cache == null) return Optional.empty();

        return cache.stream()
                .map(row -> String.valueOf(row.get("player_name")))
                .filter(name -> name.equalsIgnoreCase(input))
                .findFirst();
    }

    private void notifySender(CommandSender sender, String message) {
        if (sender instanceof Player) {
            MessageUtils.sendMiniMessageIfPresent(sender, message);
        }
    }

    private void sendOfflineMessage(Player sender, UUID uuid, String playerName, String message) {
        plugin.offlineReceiver.put(uuid, playerName);
        plugin.offlineMessages.put(uuid, message);

        MessageUtils.sendMiniMessageIfPresent(sender, "messages.playermsg.player_missing");

        dbDriver.selectData("confirm_sending", "properties", "WHERE uuid = ?", rs -> {
            boolean confirmSending;

            if (!rs.isEmpty()) {
                Object valueObj = rs.get(0).get("confirm_sending");
                if (valueObj instanceof Boolean b) confirmSending = b;
                else if (valueObj instanceof Number n) confirmSending = n.intValue() != 0;
                else if (valueObj instanceof String s) confirmSending = Boolean.parseBoolean(s);
                else confirmSending = plugin.getConfig().getBoolean("confirm_sending");
            } else {
                confirmSending = plugin.getConfig().getBoolean("confirm_sending");
            }

            if (confirmSending) {
                MessageUtils.sendMiniMessageIfPresent(sender, "messages.playermsg.send_offline");
                MessageUtils.sendMiniMessageComponent(sender, "messages.playermsg.accept_send",
                        component -> component
                                .hoverEvent(HoverEvent.showText(MessageUtils.safeText("messages.playermsg.click_send_offline")))
                                .clickEvent(ClickEvent.runCommand("acceptsend"))
                );
            } else if (plugin.offlineReceiver.containsKey(uuid) && plugin.offlineMessages.containsKey(uuid)) {
                String playerReceiver = plugin.offlineReceiver.get(uuid);
                String msgOffline = plugin.offlineMessages.get(uuid);

                Map<String, Object> insertMap = new HashMap<>();
                insertMap.put("sender", sender.getName());
                insertMap.put("receiver", playerReceiver);
                insertMap.put("message", msgOffline);
                dbDriver.insertData("offline_msg", insertMap);

                MessageUtils.sendMiniMessageIfPresent(sender, "messages.playermsg.send_offline_successfully");
                Utils.msgPlaySound(dbDriver, sender);

                plugin.offlineReceiver.remove(uuid, playerReceiver);
                plugin.offlineMessages.remove(uuid, msgOffline);
            }

            Scheduler.runLater(() -> {
                if (plugin.offlineReceiver.containsKey(uuid) && plugin.offlineMessages.containsKey(uuid)) {
                    plugin.offlineReceiver.remove(uuid, playerName);
                    plugin.offlineMessages.remove(uuid, message);
                }
            }, 1200);
        }, uuid);
    }
}
