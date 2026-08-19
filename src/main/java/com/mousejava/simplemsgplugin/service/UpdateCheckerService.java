package com.mousejava.simplemsgplugin.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mousejava.simplemsgplugin.utils.Scheduler;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

public final class UpdateCheckerService {
    private static final String API_URL_TEMPLATE = "https://api.modrinth.com/v2/project/%s/version";
    private static final String DOWNLOAD_URL_TEMPLATE = "https://modrinth.com/plugin/%s/version/%s";

    private static final AtomicReference<String> latestVersion = new AtomicReference<>(null);
    private static final AtomicReference<String> downloadUrl = new AtomicReference<>(null);

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static JavaPlugin plugin;
    private static String projectId;
    private static String versionsApiUrl;

    public static void init(JavaPlugin plugin, String projectId) {
        UpdateCheckerService.plugin = plugin;
        UpdateCheckerService.projectId = projectId;
        UpdateCheckerService.versionsApiUrl = API_URL_TEMPLATE.formatted(projectId);

        Scheduler.runTimer(UpdateCheckerService::checkForUpdate, 5 * 20, 6 * 60 * 60 * 20);
    }

    private static void checkForUpdate() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(versionsApiUrl))
                    .header("User-Agent", "plugin-update-checker")
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                plugin.getLogger().warning("Unable to check for updates on Modrinth (code " + response.statusCode() + ")");
                return;
            }

            JsonArray versions = JsonParser.parseString(response.body()).getAsJsonArray();
            if (versions.isEmpty())
                return;

            JsonObject newest = versions.get(0).getAsJsonObject();
            String versionNumber = newest.get("version_number").getAsString();
            String versionId = newest.get("id").getAsString();
            String currentVersion = plugin.getPluginMeta().getVersion();

            if (!versionNumber.equalsIgnoreCase(currentVersion)) {
                latestVersion.set(versionNumber);
                downloadUrl.set(DOWNLOAD_URL_TEMPLATE.formatted(projectId, versionId));
                plugin.getLogger().info("An update is available: %s (current: %s)".formatted(versionNumber, currentVersion));
            } else {
                latestVersion.set(null);
                downloadUrl.set(null);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Error checking for updates on Modrinth", e);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Unable to process Modrinth API response", e);
        }
    }

    public static boolean isUpdateAvailable() {
        return latestVersion.get() != null;
    }

    public static String getLatestVersion() {
        return latestVersion.get();
    }

    public static String getDownloadUrl() {
        return downloadUrl.get();
    }
}
