package simplemsgplugin.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;
import simplemsgplugin.SimpleMsgPlugin;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class MessageUtils {
    private static final MiniMessage MINI = MiniMessage.builder().build();

    public static @Nullable String getPlain(String path) {
        return getPlain(path, null);
    }

    public static @Nullable String getPlain(String path, String defaultMessage) {
        String raw = SimpleMsgPlugin.getInstance().getConfig().getString(path);
        if (raw == null || raw.trim().isEmpty()) return defaultMessage;
        return raw;
    }

    public static Optional<String> optionalPlain(String path) {
        return optionalPlain(path, null);
    }

    public static Optional<String> optionalPlain(String path, String defaultMessage) {
        String raw = getPlain(path);
        if (raw == null || raw.trim().isEmpty()) return
                defaultMessage == null || defaultMessage.trim().isEmpty()
                        ? Optional.empty()
                        : Optional.of(defaultMessage);
        return Optional.of(raw);
    }

    public static Component safeText(String path) {
        String raw = getPlain(path);
        return raw == null ? Component.empty() : Component.text(raw);
    }

    public static void sendPlainIfPresent(CommandSender sender, String path) {
        optionalPlain(path).ifPresent(sender::sendMessage);
    }

    public static void sendPlainIfPresent(CommandSender sender, String path, String defaultMessage) {
        optionalPlain(path, defaultMessage).ifPresent(sender::sendMessage);
    }

    public static Optional<String> optionalColored(String path) {
        return optionalColored(path, null);
    }

    public static Optional<String> optionalColored(String path, String defaultMessage) {
        return optionalPlain(path, defaultMessage)
                .map(ColorUtils::translateColorCodes);
    }

    public static void sendColoredIfPresent(CommandSender sender, String path) {
        optionalColored(path).ifPresent(sender::sendMessage);
    }

    public static void sendColoredIfPresent(CommandSender sender, String path, String defaultMessage) {
        optionalColored(path, defaultMessage).ifPresent(sender::sendMessage);
    }

    public static void sendColoredTransformed(CommandSender sender, String path, Function<String, String> transformer) {
        optionalColored(path)
                .map(transformer)
                .ifPresent(sender::sendMessage);
    }

    public static Optional<Component> optionalMiniMessage(String path) {
        return optionalMiniMessage(path, null);
    }

    public static Optional<Component> optionalMiniMessage(String path, String defaultMessage) {
        return optionalPlain(path, defaultMessage)
                .map(MINI::deserialize);
    }

    public static void sendMiniMessageIfPresent(CommandSender sender, String path) {
        optionalMiniMessage(path).ifPresent(sender::sendMessage);
    }

    public static void sendMiniMessageIfPresent(CommandSender sender, String path, String defaultMessage) {
        optionalMiniMessage(path, defaultMessage).ifPresent(sender::sendMessage);
    }

    public static void sendMiniMessageIfPresent(CommandSender sender, String path, Function<String, String> transformer, UnaryOperator<Component> componentModifier) {
        optionalPlain(path)
                .map(transformer)
                .map(MINI::deserialize)
                .map(componentModifier)
                .ifPresent(sender::sendMessage);
    }

    public static void sendMiniMessageComponent(CommandSender sender, String path, UnaryOperator<Component> componentModifier) {
        optionalMiniMessage(path)
                .map(componentModifier)
                .ifPresent(sender::sendMessage);
    }

    public static void sendMiniMessageTransformed(CommandSender sender, String path, Function<String, String> transformer) {
        optionalPlain(path)
                .map(transformer)
                .map(MINI::deserialize)
                .ifPresent(sender::sendMessage);
    }
}
