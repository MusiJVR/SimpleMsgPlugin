package com.mousejava.simplemsgplugin.property;

import java.util.Map;
import java.util.Set;

public final class PropertyDefinitions {
    private static final Map<String, PropertyType> PROPERTIES = Map.of(
            "confirm_sending", PropertyType.BOOLEAN,
            "sound", PropertyType.STRING,
            "volume", PropertyType.INTEGER
    );

    public static boolean contains(String propertyName) {
        return PROPERTIES.containsKey(propertyName);
    }

    public static PropertyType getType(String propertyName) {
        return PROPERTIES.get(propertyName);
    }

    public static Set<String> keySet() {
        return PROPERTIES.keySet();
    }

    public static Map<String, PropertyType> asMap() {
        return PROPERTIES;
    }
}
