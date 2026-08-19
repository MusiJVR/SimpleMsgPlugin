package com.mousejava.simplemsgplugin.utils;

import org.bukkit.Bukkit;

public final class ServerVersionUtils {
    private static final int[] MIN_VERSION = {1, 21, 9};
    private static Boolean supported;

    public static boolean supportsPlayerHeadComponent() {
        if (supported == null)
            supported = compare(currentVersion(), MIN_VERSION) >= 0;

        return supported;
    }

    private static int[] currentVersion() {
        String mcPart = Bukkit.getBukkitVersion().split("-")[0];
        String[] parts = mcPart.split("\\.");
        int[] version = new int[3];
        for (int i = 0; i < Math.min(3, parts.length); i++) {
            version[i] = parseIntSafe(parts[i]);
        }
        return version;
    }

    private static int parseIntSafe(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static int compare(int[] a, int[] b) {
        for (int i = 0; i < 3; i++) {
            int cmp = Integer.compare(a[i], b[i]);
            if (cmp != 0) return cmp;
        }
        return 0;
    }
}
