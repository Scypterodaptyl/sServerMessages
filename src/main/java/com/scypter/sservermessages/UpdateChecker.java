package com.scypter.sservermessages;

import org.bukkit.plugin.Plugin;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class UpdateChecker {

    private static final String PROJECT = "sservermessages";
    private static final String API = "https://api.modrinth.com/v2/project/" + PROJECT + "/version";
    private static final String PAGE = "https://modrinth.com/plugin/" + PROJECT;
    private static final Pattern VERSION = Pattern.compile(
            "\"version_number\"\\s*:\\s*\"([^\"]+)\"[^{}]*?\"version_type\"\\s*:\\s*\"release\"");
    private static final Pattern NUMBER = Pattern.compile("\\d+");

    private UpdateChecker() {
    }

    static void check(final Plugin plugin) {
        final String current = plugin.getDescription().getVersion();
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    String latest = latestRelease(current);
                    if (latest == null) {
                        return;
                    }
                    if (compare(latest, current) > 0) {
                        plugin.getLogger().warning("Доступна новая версия " + latest + " (у вас " + current + "). "
                                + "Скачать: " + PAGE);
                    } else {
                        plugin.getLogger().info("Установлена последняя версия (" + current + ").");
                    }
                } catch (Throwable error) {
                    plugin.getLogger().info("Не удалось проверить обновления: " + error.getClass().getSimpleName()
                            + (error.getMessage() == null ? "" : ": " + error.getMessage()));
                }
            }
        }, "sServerMessages-UpdateChecker");
        thread.setDaemon(true);
        thread.start();
    }

    private static String latestRelease(String current) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(API).openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setRequestProperty("User-Agent", "Scypterodaptyl/sServerMessages/" + current);
        connection.setRequestProperty("Accept", "application/json");
        try {
            if (connection.getResponseCode() != 200) {
                throw new IllegalStateException("HTTP " + connection.getResponseCode());
            }
            InputStream in = connection.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            Matcher matcher = VERSION.matcher(new String(out.toByteArray(), "UTF-8"));
            return matcher.find() ? matcher.group(1) : null;
        } finally {
            connection.disconnect();
        }
    }

    static int compare(String a, String b) {
        Matcher left = NUMBER.matcher(a);
        Matcher right = NUMBER.matcher(b);
        while (true) {
            boolean hasLeft = left.find();
            boolean hasRight = right.find();
            if (!hasLeft && !hasRight) {
                return 0;
            }
            long x = hasLeft ? Long.parseLong(left.group()) : 0;
            long y = hasRight ? Long.parseLong(right.group()) : 0;
            if (x != y) {
                return x < y ? -1 : 1;
            }
        }
    }
}
