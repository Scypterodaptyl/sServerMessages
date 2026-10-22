package com.scypter.sservermessages;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

public final class Settings {

    public static final class Rule {
        private final boolean vanilla;
        private final String text;

        private Rule(boolean vanilla, String text) {
            this.vanilla = vanilla;
            this.text = text;
        }

        public boolean isVanilla() {
            return vanilla;
        }

        public boolean isHidden() {
            return !vanilla && text.isEmpty();
        }

        public String getText() {
            return text;
        }
    }

    private static final Rule VANILLA = new Rule(true, "");
    private static final Rule HIDDEN = new Rule(false, "");

    private volatile Rule join = VANILLA;
    private volatile Rule quit = VANILLA;
    private volatile Rule kick = VANILLA;
    private volatile Rule death = VANILLA;
    private volatile Rule advancement = VANILLA;
    private volatile Map<String, Rule> deathCauses = Collections.emptyMap();
    private volatile Map<String, String> causeNames = Collections.emptyMap();
    private volatile List<String> reloadMessage = Collections.emptyList();
    private volatile List<String> errorMessage = Collections.emptyList();
    private volatile List<String> noPermissionMessage = Collections.emptyList();

    public void load(FileConfiguration config, Logger log) {
        join = rule(config.getConfigurationSection("join"), "join", log);
        quit = rule(config.getConfigurationSection("quit"), "quit", log);
        kick = rule(config.getConfigurationSection("kick"), "kick", log);
        advancement = rule(config.getConfigurationSection("advancement"), "advancement", log);

        ConfigurationSection deathSection = config.getConfigurationSection("death");
        death = rule(deathSection, "death", log);

        Map<String, Rule> causes = new HashMap<String, Rule>();
        Map<String, String> names = new HashMap<String, String>();
        if (deathSection != null && !"hide".equalsIgnoreCase(deathSection.getString("mode", "").trim())) {
            ConfigurationSection causeSection = deathSection.getConfigurationSection("causes");
            if (causeSection != null) {
                for (String key : causeSection.getKeys(false)) {
                    Object raw = causeSection.get(key);
                    if (raw != null) {
                        causes.put(key.toLowerCase(Locale.ROOT), text(String.valueOf(raw)));
                    }
                }
            }
            ConfigurationSection nameSection = deathSection.getConfigurationSection("names");
            if (nameSection != null) {
                for (String key : nameSection.getKeys(false)) {
                    Object raw = nameSection.get(key);
                    if (raw != null) {
                        names.put(key.toLowerCase(Locale.ROOT), String.valueOf(raw));
                    }
                }
            }
        }
        deathCauses = Collections.unmodifiableMap(causes);
        causeNames = Collections.unmodifiableMap(names);

        reloadMessage = Text.lines(config, "reload-message", "&aКонфигурация sServerMessages перезагружена.");
        errorMessage = Text.lines(config, "error-message",
                "&cОшибка в config.yml, настройки не изменены. Подробности в консоли.");
        noPermissionMessage = Text.lines(config, "no-permission-message", "&cУ вас нет прав на эту команду.");
    }

    private static Rule rule(ConfigurationSection section, String name, Logger log) {
        if (section == null) {
            log.warning("В config.yml нет секции " + name + ", сообщение остаётся ванильным.");
            return VANILLA;
        }
        String mode = section.getString("mode", "vanilla").trim().toLowerCase(Locale.ROOT);
        if (mode.equals("vanilla")) {
            return VANILLA;
        }
        if (mode.equals("hide")) {
            return HIDDEN;
        }
        if (!mode.equals("replace")) {
            log.warning("В секции " + name + " неизвестный mode '" + mode + "', сообщение остаётся ванильным.");
            return VANILLA;
        }
        Object raw = section.get("message");
        return text(raw == null ? "" : String.valueOf(raw));
    }

    private static Rule text(String raw) {
        String colored = Text.color(raw);
        return colored.isEmpty() ? HIDDEN : new Rule(false, colored);
    }

    public Rule getJoin() {
        return join;
    }

    public Rule getQuit() {
        return quit;
    }

    public Rule getKick() {
        return kick;
    }

    public Rule getAdvancement() {
        return advancement;
    }

    public Rule getDeath(String... causeKeys) {
        Rule general = death;
        if (general.isVanilla()) {
            return general;
        }
        for (String key : causeKeys) {
            Rule specific = key == null ? null : deathCauses.get(key);
            if (specific != null) {
                return specific;
            }
        }
        return general;
    }

    public String getCauseName(String key) {
        String name = causeNames.get(key);
        return name != null ? name : Text.prettify(key);
    }

    public List<String> getReloadMessage() {
        return reloadMessage;
    }

    public List<String> getErrorMessage() {
        return errorMessage;
    }

    public List<String> getNoPermissionMessage() {
        return noPermissionMessage;
    }

    public String describe() {
        return "вход " + mode(join) + ", выход " + mode(quit) + ", кик " + mode(kick)
                + ", смерть " + mode(death) + ", достижения " + mode(advancement);
    }

    private static String mode(Rule rule) {
        return rule.isVanilla() ? "vanilla" : rule.isHidden() ? "hide" : "replace";
    }
}
