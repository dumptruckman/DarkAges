package com.dumptruckman.minecraft.darkages.util;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class EntityTools {

    public static LivingEntity getTargetedLivingEntity(final LivingEntity entity, final int maxDistance) {
        // Shitty hack to get the looked at entity.
        Location loc = entity.getLocation();
        Vector n1 = loc.toVector();
        Vector dir = loc.getDirection();
        LivingEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Entity e : entity.getNearbyEntities(maxDistance, maxDistance, maxDistance)) {
            if (e instanceof LivingEntity) {
                Vector n0 = e.getLocation().toVector();
                double distance = -n1.clone().subtract(n0).dot(dir) / dir.lengthSquared();
                if (distance > 0 && distance <= maxDistance) { // In front of the player, not behind.
                    double lineDist = Math.sqrt(n1.distanceSquared(n0) - distance * distance);
                    if (lineDist < 1) { // Tolerance value of within 3 blocks of the line.
                        if (distance < nearestDistance) {
                            nearestDistance = distance;
                            nearest = (LivingEntity) e;
                        }
                    }
                }
            }
        }
        return nearest;
    }
}
