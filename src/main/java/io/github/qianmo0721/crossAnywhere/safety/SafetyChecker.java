package io.github.qianmo0721.crossAnywhere.safety;

import io.github.qianmo0721.crossAnywhere.config.PluginConfig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public final class SafetyChecker {
    private final PluginConfig.SafetyConfig config;

    public SafetyChecker(PluginConfig.SafetyConfig config) {
        this.config = config;
    }

    public SafetyResult check(Location location) {
        if (location == null || location.getWorld() == null) {
            return new SafetyResult(false, "invalid_world");
        }
        World world = location.getWorld();
        int minY = Math.max(config.minY, world.getMinHeight());
        if (location.getY() <= minY) {
            return new SafetyResult(false, "void");
        }

        Block feet = world.getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        Block head = world.getBlockAt(location.getBlockX(), location.getBlockY() + 1, location.getBlockZ());
        Block below = world.getBlockAt(location.getBlockX(), location.getBlockY() - 1, location.getBlockZ());

        if (!feet.isPassable() || !head.isPassable()) {
            return new SafetyResult(false, "blocked");
        }
        if (!below.getType().isSolid()) {
            return new SafetyResult(false, "no_floor");
        }
        if (isDangerous(feet.getType()) || isDangerous(below.getType())) {
            return new SafetyResult(false, "danger");
        }
        if (config.disallowWater && (isWaterLike(feet.getType()) || isWaterLike(head.getType()))) {
            return new SafetyResult(false, "water");
        }
        return new SafetyResult(true, "safe");
    }

    private boolean isDangerous(Material material) {
        return material == Material.LAVA
                || material == Material.FIRE
                || material == Material.SOUL_FIRE
                || material == Material.CAMPFIRE
                || material == Material.SOUL_CAMPFIRE
                || material == Material.CACTUS
                || material == Material.MAGMA_BLOCK
                || material == Material.WITHER_ROSE;
    }

    private boolean isWaterLike(Material material) {
        return material == Material.WATER
                || material == Material.BUBBLE_COLUMN
                || material == Material.KELP
                || material == Material.KELP_PLANT
                || material == Material.SEAGRASS
                || material == Material.TALL_SEAGRASS;
    }
}
