package com.scypter.sservermessages;

import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

final class Advancements {

    private static final String COMPONENT = "net.kyori.adventure.text.Component";
    private static final String PLAIN = "net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer";
    private static final String SERIALIZER = "net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer";

    private final Method getter;
    private final Method setter;
    private final Object serializer;
    private final Method deserialize;

    private Advancements(Method getter, Method setter, Object serializer, Method deserialize) {
        this.getter = getter;
        this.setter = setter;
        this.serializer = serializer;
        this.deserialize = deserialize;
    }

    static Advancements create(Plugin plugin, Class<?> eventType) {
        try {
            Method getter = eventType.getMethod("message");
            Class<?> component = getter.getReturnType();
            Method setter = eventType.getMethod("message", component);
            Class<?> type = Class.forName(SERIALIZER, false, plugin.getClass().getClassLoader());
            Object serializer = type.getMethod("legacySection").invoke(null);
            Method deserialize = type.getMethod("deserialize", String.class);
            return new Advancements(getter, setter, serializer, deserialize);
        } catch (Throwable ignored) {
            return null;
        }
    }

    boolean announced(Event event) {
        try {
            return getter.invoke(event) != null;
        } catch (Throwable ignored) {
            return false;
        }
    }

    boolean set(Event event, String legacyText) {
        try {
            Object component = legacyText == null ? null : deserialize.invoke(serializer, legacyText);
            setter.invoke(event, component);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static String displayTitle(Plugin plugin, Object display) throws Exception {
        try {
            Object title = display.getClass().getMethod("getTitle").invoke(display);
            if (title instanceof String) {
                return (String) title;
            }
        } catch (NoSuchMethodException ignored) {
        }
        Object component = display.getClass().getMethod("title").invoke(display);
        Class<?> type = Class.forName(PLAIN, false, plugin.getClass().getClassLoader());
        Object plain = type.getMethod("plainText").invoke(null);
        Class<?> componentType = Class.forName(COMPONENT, false, plugin.getClass().getClassLoader());
        return String.valueOf(type.getMethod("serialize", componentType).invoke(plain, component));
    }

    static String titleOf(Plugin plugin, Event event) {
        try {
            Object advancement = event.getClass().getMethod("getAdvancement").invoke(event);
            try {
                Object display = advancement.getClass().getMethod("getDisplay").invoke(advancement);
                if (display != null) {
                    String title = displayTitle(plugin, display);
                    if (!title.isEmpty()) {
                        return title;
                    }
                }
            } catch (Throwable ignored) {
            }
            Object key = advancement.getClass().getMethod("getKey").invoke(advancement);
            String path = String.valueOf(key.getClass().getMethod("getKey").invoke(key));
            return Text.prettify(path.substring(path.lastIndexOf('/') + 1));
        } catch (Throwable ignored) {
            return "";
        }
    }
}
