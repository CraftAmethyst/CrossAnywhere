package io.github.qianmo0721.crossAnywhere.safety;

import org.bukkit.Location;
import org.bukkit.World;

public final class NearbySafeFinder {
    public Location find(Location origin, int radius, int vertical, SafetyChecker checker) {
        if (origin == null || origin.getWorld() == null) {
            return null;
        }
        World world = origin.getWorld();
        int originX = origin.getBlockX();
        int originY = origin.getBlockY();
        int originZ = origin.getBlockZ();

        for (int r = 0; r <= radius; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.abs(dx) != r && Math.abs(dz) != r) {
                        continue;
                    }
                    for (int dy = -vertical; dy <= vertical; dy++) {
                        int y = originY + dy;
                        if (y <= world.getMinHeight() || y >= world.getMaxHeight()) {
                            continue;
                        }
                        Location candidate = new Location(world,
                                originX + dx + 0.5,
                                y,
                                originZ + dz + 0.5,
                                origin.getYaw(),
                                origin.getPitch());
                        if (checker.check(candidate).isSafe()) {
                            return candidate;
                        }
                    }
                }
            }
        }
        return null;
    }
}
