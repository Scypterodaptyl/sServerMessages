package com.scypter.sservermessages;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

final class Placeholders {

    private static volatile Method papi;

    private Placeholders() {
    }

    static boolean hookPlaceholderApi(Plugin plugin) {
        papi = null;
        Plugin other = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI");
        if (other == null || !other.isEnabled()) {
            return false;
        }
        try {
            Class<?> type = Class.forName("me.clip.placeholderapi.PlaceholderAPI", true,
                    other.getClass().getClassLoader());
            papi = type.getMethod("setPlaceholders", OfflinePlayer.class, String.class);
            return true;
        } catch (Throwable error) {
            plugin.getLogger().warning("PlaceholderAPI найден, но подключить не удалось: " + error);
            return false;
        }
    }

    static String external(Player player, String template) {
        Method method = papi;
        if (method == null || player == null || template.indexOf('%') < 0) {
            return template;
        }
        try {
            return Text.color(String.valueOf(method.invoke(null, player, template)));
        } catch (Throwable ignored) {
            return template;
        }
    }

    static String[] of(Player player, int onlineOffset, String... extra) {
        String world = player.getWorld() == null ? "" : player.getWorld().getName();
        int online = Math.max(0, player.getServer().getOnlinePlayers().size() + onlineOffset);
        String[] base = {
                "{player}", player.getName(),
                "{displayname}", player.getDisplayName(),
                "{world}", world,
                "{online}", String.valueOf(online),
                "{max}", String.valueOf(player.getServer().getMaxPlayers())
        };
        String[] result = new String[base.length + extra.length];
        System.arraycopy(base, 0, result, 0, base.length);
        System.arraycopy(extra, 0, result, base.length, extra.length);
        return result;
    }

    static boolean vanished(Player player) {
        try {
            for (MetadataValue value : player.getMetadata("vanished")) {
                if (value.asBoolean()) {
                    return true;
                }
            }
        } catch (Throwable ignored) {
        }
        return false;
    }
}
