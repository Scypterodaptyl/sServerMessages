package com.scypter.sservermessages;

import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class Hooks implements Listener {

    private static final String JOIN_EVENT = "org.bukkit.event.player.PlayerJoinEvent";
    private static final String QUIT_EVENT = "org.bukkit.event.player.PlayerQuitEvent";
    private static final String KICK_EVENT = "org.bukkit.event.player.PlayerKickEvent";
    private static final String DEATH_EVENT = "org.bukkit.event.entity.PlayerDeathEvent";
    private static final String ADVANCEMENT_EVENT = "org.bukkit.event.player.PlayerAdvancementDoneEvent";

    private Hooks() {
    }

    public static void register(final SServerMessages plugin) {
        Listener listener = new Hooks();

        subscribe(plugin, listener, JOIN_EVENT, new Events.Handler() {
            public void handle(Event event) {
                plugin.handleJoin((PlayerJoinEvent) event);
            }
        });
        subscribe(plugin, listener, QUIT_EVENT, new Events.Handler() {
            public void handle(Event event) {
                plugin.handleQuit((PlayerQuitEvent) event);
            }
        });
        subscribe(plugin, listener, KICK_EVENT, new Events.Handler() {
            public void handle(Event event) {
                plugin.handleKick((PlayerKickEvent) event);
            }
        });
        subscribe(plugin, listener, DEATH_EVENT, new Events.Handler() {
            public void handle(Event event) {
                plugin.handleDeath((PlayerDeathEvent) event);
            }
        });

        Class<?> advancement = Events.find(plugin, ADVANCEMENT_EVENT);
        if (advancement == null) {
            plugin.getLogger().info("Событие достижений недоступно на этой версии, сообщения о достижениях не меняются.");
            return;
        }
        final Advancements access = Advancements.create(plugin, advancement);
        if (access == null) {
            plugin.getLogger().warning("На этом ядре сообщение о достижении заменить или скрыть нельзя, "
                    + "оно остаётся ванильным.");
            return;
        }
        Events.register(plugin, listener, advancement, EventPriority.HIGHEST, new Events.Handler() {
            public void handle(Event event) {
                plugin.handleAdvancement(event, access);
            }
        });
    }

    private static void subscribe(SServerMessages plugin, Listener listener, String name, Events.Handler handler) {
        Class<?> type = Events.find(plugin, name);
        if (!Events.register(plugin, listener, type, EventPriority.HIGHEST, handler)) {
            plugin.getLogger().severe("Не найдено событие " + name + ", это сообщение не меняется.");
        }
    }
}
