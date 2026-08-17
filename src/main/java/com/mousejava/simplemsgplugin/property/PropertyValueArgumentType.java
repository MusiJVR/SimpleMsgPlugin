package com.mousejava.simplemsgplugin.property;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PropertyValueArgumentType implements CustomArgumentType<Object, String> {
    private final Map<String, PropertyType> properties;

    private PropertyValueArgumentType(Map<String, PropertyType> properties) {
        this.properties = properties;
    }

    public static PropertyValueArgumentType propertyValue(Map<String, PropertyType> properties) {
        return new PropertyValueArgumentType(properties);
    }

    public static Object getValue(final CommandContext<?> context, final String name) {
        return context.getArgument(name, Object.class);
    }

    @Override
    public Object parse(final StringReader reader) throws CommandSyntaxException {
        PropertyType type = getPropertyType(reader);
        return switch (type) {
            case BOOLEAN -> reader.readBoolean();
            case INTEGER -> reader.readInt();
            case DOUBLE -> reader.readDouble();
            case FLOAT -> reader.readFloat();
            case STRING -> reader.readString();
        };
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        PropertyType type = getPropertyType(context);
        switch (type) {
            case BOOLEAN -> {
                suggestIfMatches(builder, "true");
                suggestIfMatches(builder, "false");
            }
            case INTEGER, DOUBLE, FLOAT, STRING -> { }
        }

        return builder.buildFuture();
    }

    private static void suggestIfMatches(SuggestionsBuilder builder, String value) {
        if (value.startsWith(builder.getRemainingLowerCase())) {
            builder.suggest(value);
        }
    }

    private PropertyType getPropertyType(StringReader reader) throws CommandSyntaxException {
        String input = reader.getString();
        int cursor = reader.getCursor();

        int end = cursor - 1;
        while (end >= 0 && Character.isWhitespace(input.charAt(end))) {
            end--;
        }

        int start = end;
        while (start >= 0 && !Character.isWhitespace(input.charAt(start))) {
            start--;
        }

        String propertyName = input
                .substring(start + 1, end + 1)
                .toLowerCase(Locale.ROOT);

        PropertyType type = properties.get(propertyName);
        if (type == null) {
            throw new CommandSyntaxException(
                    CommandSyntaxException.BUILT_IN_EXCEPTIONS
                            .dispatcherUnknownArgument(),
                    new LiteralMessage("Unknown property: " + propertyName)
            );
        }

        return type;
    }

    private PropertyType getPropertyType(CommandContext<?> context) {
        String propertyName = StringArgumentType
                .getString(context, "property")
                .toLowerCase(Locale.ROOT);

        return properties.get(propertyName);
    }
}
