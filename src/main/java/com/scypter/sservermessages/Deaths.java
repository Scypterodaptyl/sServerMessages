package com.scypter.sservermessages;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Locale;

final class Deaths {

    private Deaths() {
    }

    static String causeKey(Player victim) {
        EntityDamageEvent damage = victim.getLastDamageCause();
        if (damage == null || damage.getCause() == null) {
            return "unknown";
        }
        return damage.getCause().name().toLowerCase(Locale.ROOT);
    }

    static Entity killer(Player victim) {
        Player player = victim.getKiller();
        if (player != null) {
            return player;
        }
        EntityDamageEvent damage = victim.getLastDamageCause();
        if (!(damage instanceof EntityDamageByEntityEvent)) {
            return null;
        }
        Entity damager = ((EntityDamageByEntityEvent) damage).getDamager();
        if (damager instanceof Projectile) {
            Object shooter = ((Projectile) damager).getShooter();
            if (shooter instanceof Entity) {
                return (Entity) shooter;
            }
        }
        return damager;
    }

    static String nameOf(Entity entity) {
        if (entity == null) {
            return "";
        }
        if (entity instanceof Player) {
            return ((Player) entity).getName();
        }
        if (entity instanceof LivingEntity) {
            String custom = ((LivingEntity) entity).getCustomName();
            if (custom != null && !custom.isEmpty()) {
                return custom;
            }
        }
        return Text.prettify(entity.getType().name());
    }

    static String groupKey(Entity killer) {
        if (killer == null) {
            return null;
        }
        return killer instanceof Player ? "player" : "mob";
    }
}
