# sServerMessages

A lightweight Bukkit/Spigot/Paper/Purpur/Folia plugin that controls the server's broadcast messages:
join, quit, kick, death and advancement. For each one you pick a mode in `config.yml`:
leave it **vanilla**, **hide** it, or **replace** it with your own text. Everyone on the server sees the
replaced text. No dependencies, no GUI, no per-player or per-group settings.

One jar covers every server version from **1.8.8 to 26.x**.

## Features

- Three modes per message: `vanilla`, `hide`, `replace`. An empty `message` also hides.
- Separate death texts per cause (fall, fire, mob, player...), with a general fallback.
- Placeholders: `{player}` everywhere, `{killer}`/`{cause}` for deaths, `{reason}` for kicks,
  `{advancement}` for advancements.
- `&` color codes everywhere, plus `&#RRGGBB` hex colors on 1.16+.
- Hot reload with `/sservermessages reload`.

## Installation

1. Drop `sServerMessages-1.0.0.jar` into the server's `plugins` folder.
2. Start the server once to generate `plugins/sServerMessages/config.yml`.
3. Edit the config and run `/sservermessages reload`.

## Configuration

```yaml
join:
  mode: replace            # vanilla | hide | replace
  message: "&e{player} &7зашёл на сервер"

quit:
  mode: replace
  message: "&e{player} &7вышел с сервера"

kick:
  mode: replace
  message: "&e{player} &7был кикнут с сервера: &c{reason}"

death:
  mode: replace
  message: "&c{player} &7погиб"
  causes:
    player: "&c{player} &7был убит игроком &c{killer}"
    mob: "&c{player} &7был убит существом &c{killer}"
    fall: "&c{player} &7разбился"
  names:
    fall: "падение"

advancement:
  mode: replace
  message: "&e{player} &7получил достижение &a{advancement}"
```

### Placeholders

| Message | Placeholders |
| --- | --- |
| join, quit | `{player}` |
| kick | `{player}`, `{reason}` |
| death | `{player}`, `{killer}`, `{cause}` |
| advancement | `{player}`, `{advancement}` |

### Deaths

The cause is taken from `getLastDamageCause()`, which exists on every supported version. The text is chosen in
this order:

1. `player` if the killer is a player, or `mob` if it is any other entity (the shooter, for projectiles).
2. The damage cause name in lowercase, such as `fall`, `fire_tick`, `lava`, `drowning`, `void`,
   `explosion`, `entity_attack`.
3. The general `message`.

Cause names that do not exist on an older version are simply never matched. An empty string for a cause hides
the message for that cause. `{cause}` is the readable name from `names`, falling back to the prettified cause
name. `{killer}` is empty when there is no killer.

### Kicks

`kick` is the message **other players** see when someone is kicked. The reason shown to the kicked player
on their disconnect screen is not changed. A kick is followed by a quit event on most versions; the plugin
makes the kick message win, so the player is not announced twice.

## Advancements

| Platform | Replace / hide |
| --- | --- |
| Paper, Purpur, Folia (1.16+) | Yes |
| Spigot, and Paper before the message API (such as 1.12.2) | No, the vanilla message stays |
| Servers older than 1.12 | No such event |

This was checked with a spike rather than from memory. The Spigot API (26.1.2 and 1.12 era) has
`PlayerAdvancementDoneEvent` with only `getAdvancement()`, with no way to change or cancel the announcement.
Paper adds `message(Component)` (confirmed on 1.16.5, 26.1.2 and Folia 26.1.2; absent on Paper 1.12.2).

The plugin reaches the Paper method by reflection and builds the Adventure component from the
legacy-colored text, so it still compiles against spigot-api 1.8.8. On a core without the method the plugin
logs one warning on startup and leaves the vanilla message alone. It deliberately does not toggle the
`announceAdvancements` gamerule, because the plugin must not change server state.

`{advancement}` is the advancement title where the API exposes it (26.x), otherwise a prettified key such as
"Mine stone". Recipe advancements are never announced and are ignored.

## Commands

| Command | Description |
| --- | --- |
| `/sservermessages reload` | Reloads `config.yml`. Alias: `/ssm`. |

Permission: `sservermessages.reload` (default: op). The plugin uses a plain Bukkit permission, so LuckPerms and
other permission plugins work with it without any integration. The console can always reload.

If `config.yml` is broken, the previous settings stay active and the error is printed to the console.

## Compatibility

Spigot, Paper, Purpur and Folia (`folia-supported: true`). The plugin uses no scheduler, and the settings
fields are `volatile`, so event handlers on Folia's region threads read them safely.

## Building

```
./gradlew build
```

Requires JDK 21 to build, since JDK 25 can no longer target Java 8. The `verifyModernApi` task additionally
recompiles the sources against `paper-api 26.3` and needs a JDK 25 installed for its toolchain; it runs as part
of `check`. bStats is shaded and relocated to `com.scypter.sservermessages.libs.bstats`.

## License

See [LICENSE](LICENSE).
