package io.github.qianmo0721.crossAnywhere.config;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class PluginConfig {
    public final List<String> worlds;
    public final boolean easyTp;
    public final int waypointNameMaxLength;
    public final boolean allowUnicodeNames;
    public final int personalMaxWaypoints;
    public final int globalMaxWaypoints;
    public final int saveIntervalSeconds;
    public final boolean backOnDeath;
    public final int tpaTimeoutSeconds;
    public final DuplicatePolicy tpaDuplicatePolicy;
    public final int confirmTimeoutSeconds;
    public final String defaultLocale;
    public final CooldownConfig cooldown;
    public final CostConfig cost;
    public final SafetyConfig safety;
    public final CommandsConfig commands;

    private PluginConfig(List<String> worlds,
                         boolean easyTp,
                         int waypointNameMaxLength,
                         boolean allowUnicodeNames,
                         int personalMaxWaypoints,
                         int globalMaxWaypoints,
                         int saveIntervalSeconds,
                         boolean backOnDeath,
                         int tpaTimeoutSeconds,
                         DuplicatePolicy tpaDuplicatePolicy,
                         int confirmTimeoutSeconds,
                         String defaultLocale,
                         CooldownConfig cooldown,
                         CostConfig cost,
                         SafetyConfig safety,
                         CommandsConfig commands) {
        this.worlds = worlds;
        this.easyTp = easyTp;
        this.waypointNameMaxLength = waypointNameMaxLength;
        this.allowUnicodeNames = allowUnicodeNames;
        this.personalMaxWaypoints = personalMaxWaypoints;
        this.globalMaxWaypoints = globalMaxWaypoints;
        this.saveIntervalSeconds = saveIntervalSeconds;
        this.backOnDeath = backOnDeath;
        this.tpaTimeoutSeconds = tpaTimeoutSeconds;
        this.tpaDuplicatePolicy = tpaDuplicatePolicy;
        this.confirmTimeoutSeconds = confirmTimeoutSeconds;
        this.defaultLocale = defaultLocale;
        this.cooldown = cooldown;
        this.cost = cost;
        this.safety = safety;
        this.commands = commands;
    }

    public static PluginConfig load(FileConfiguration config) {
        List<String> worlds = new ArrayList<>(config.getStringList("worlds"));
        boolean easyTp = config.getBoolean("easy_tp", true);
        int waypointNameMaxLength = config.getInt("waypoint_name_max_length", 24);
        boolean allowUnicodeNames = config.getBoolean("allow_unicode_names", false);
        int personalMaxWaypoints = config.getInt("personal_max_waypoints", 10);
        int globalMaxWaypoints = config.getInt("global_max_waypoints", 100);
        int saveIntervalSeconds = config.getInt("save_interval_seconds", 120);
        boolean backOnDeath = config.getBoolean("back_on_death", true);
        int tpaTimeoutSeconds = config.getInt("tpa_timeout_seconds", 60);
        DuplicatePolicy duplicatePolicy = DuplicatePolicy.from(config.getString("tpa_duplicate_policy", "REJECT"));
        int confirmTimeoutSeconds = config.getInt("confirm_timeout_seconds", 15);
        String defaultLocale = config.getString("default_locale", "en_US");

        CooldownConfig cooldown = CooldownConfig.load(config.getConfigurationSection("cooldown"));
        CostConfig cost = CostConfig.load(config.getConfigurationSection("cost"));
        SafetyConfig safety = SafetyConfig.load(config.getConfigurationSection("safety_check"));
        CommandsConfig commands = CommandsConfig.load(config.getConfigurationSection("commands"));

        return new PluginConfig(worlds, easyTp, waypointNameMaxLength, allowUnicodeNames,
                personalMaxWaypoints, globalMaxWaypoints, saveIntervalSeconds, backOnDeath,
                tpaTimeoutSeconds, duplicatePolicy, confirmTimeoutSeconds, defaultLocale,
                cooldown, cost, safety, commands);
    }

    public boolean isWorldAllowed(String worldName) {
        if (worlds == null || worlds.isEmpty()) {
            return true;
        }
        for (String world : worlds) {
            if (world.equalsIgnoreCase(worldName)) {
                return true;
            }
        }
        return false;
    }

    public enum DuplicatePolicy {
        REJECT,
        REPLACE;

        public static DuplicatePolicy from(String raw) {
            if (raw == null) {
                return REJECT;
            }
            try {
                return DuplicatePolicy.valueOf(raw.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                return REJECT;
            }
        }
    }

    public static final class CooldownConfig {
        public final int waypointSeconds;
        public final int tpSeconds;
        public final int backSeconds;

        private CooldownConfig(int waypointSeconds, int tpSeconds, int backSeconds) {
            this.waypointSeconds = waypointSeconds;
            this.tpSeconds = tpSeconds;
            this.backSeconds = backSeconds;
        }

        public static CooldownConfig load(ConfigurationSection section) {
            if (section == null) {
                return new CooldownConfig(0, 0, 0);
            }
            return new CooldownConfig(
                    section.getInt("waypoint", 0),
                    section.getInt("tp", 0),
                    section.getInt("back", 0)
            );
        }
    }

    public static final class CostConfig {
        public final ExpCost exp;
        public final ItemCost item;
        public final CrossworldCost crossworld;
        public final Rounding rounding;

        private CostConfig(ExpCost exp, ItemCost item, CrossworldCost crossworld, Rounding rounding) {
            this.exp = exp;
            this.item = item;
            this.crossworld = crossworld;
            this.rounding = rounding;
        }

        public static CostConfig load(ConfigurationSection section) {
            if (section == null) {
                return new CostConfig(new ExpCost(false, 0, 0.0),
                        new ItemCost(false, Material.DIAMOND, -1, 0, 0.0),
                        new CrossworldCost(CrossworldMode.FIXED_DISTANCE, 1000.0, 0),
                        Rounding.CEIL);
            }
            ExpCost exp = ExpCost.load(section.getConfigurationSection("exp"));
            ItemCost item = ItemCost.load(section.getConfigurationSection("item"));
            CrossworldCost crossworld = CrossworldCost.load(section.getConfigurationSection("crossworld"));
            Rounding rounding = Rounding.from(section.getString("rounding", "CEIL"));
            return new CostConfig(exp, item, crossworld, rounding);
        }
    }

    public enum CrossworldMode {
        FIXED_DISTANCE,
        EXTRA_COST;

        public static CrossworldMode from(String raw) {
            if (raw == null) {
                return FIXED_DISTANCE;
            }
            try {
                return CrossworldMode.valueOf(raw.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                return FIXED_DISTANCE;
            }
        }
    }

    public static final class CrossworldCost {
        public final CrossworldMode mode;
        public final double distance;
        public final int extraCost;

        private CrossworldCost(CrossworldMode mode, double distance, int extraCost) {
            this.mode = mode;
            this.distance = distance;
            this.extraCost = extraCost;
        }

        public static CrossworldCost load(ConfigurationSection section) {
            if (section == null) {
                return new CrossworldCost(CrossworldMode.FIXED_DISTANCE, 1000.0, 0);
            }
            CrossworldMode mode = CrossworldMode.from(section.getString("mode", "FIXED_DISTANCE"));
            double distance = section.getDouble("distance", 1000.0);
            int extraCost = section.getInt("extra_cost", 0);
            return new CrossworldCost(mode, distance, extraCost);
        }
    }

    public enum Rounding {
        CEIL,
        FLOOR,
        ROUND;

        public static Rounding from(String raw) {
            if (raw == null) {
                return CEIL;
            }
            try {
                return Rounding.valueOf(raw.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                return CEIL;
            }
        }
    }

    public static final class ExpCost {
        public final boolean enabled;
        public final int base;
        public final double perBlock;

        private ExpCost(boolean enabled, int base, double perBlock) {
            this.enabled = enabled;
            this.base = base;
            this.perBlock = perBlock;
        }

        public static ExpCost load(ConfigurationSection section) {
            if (section == null) {
                return new ExpCost(false, 0, 0.0);
            }
            return new ExpCost(
                    section.getBoolean("enabled", false),
                    section.getInt("base", 0),
                    section.getDouble("per_block", 0.0)
            );
        }
    }

    public static final class ItemCost {
        public final boolean enabled;
        public final Material material;
        public final int customModelData;
        public final int base;
        public final double perBlock;

        private ItemCost(boolean enabled, Material material, int customModelData, int base, double perBlock) {
            this.enabled = enabled;
            this.material = material;
            this.customModelData = customModelData;
            this.base = base;
            this.perBlock = perBlock;
        }

        public static ItemCost load(ConfigurationSection section) {
            if (section == null) {
                return new ItemCost(false, Material.DIAMOND, -1, 0, 0.0);
            }
            String materialName = section.getString("material", "DIAMOND");
            Material material = Material.matchMaterial(materialName);
            if (material == null) {
                material = Material.DIAMOND;
            }
            int customModelData = section.getInt("custom_model_data", -1);
            return new ItemCost(
                    section.getBoolean("enabled", false),
                    material,
                    customModelData,
                    section.getInt("base", 0),
                    section.getDouble("per_block", 0.0)
            );
        }
    }

    public static final class SafetyConfig {
        public final boolean enabled;
        public final SafetyMode mode;
        public final int searchRadius;
        public final int searchVertical;
        public final NearbyFallback nearbyFallback;
        public final boolean disallowWater;
        public final int minY;

        private SafetyConfig(boolean enabled, SafetyMode mode, int searchRadius,
                             int searchVertical, NearbyFallback nearbyFallback,
                             boolean disallowWater, int minY) {
            this.enabled = enabled;
            this.mode = mode;
            this.searchRadius = searchRadius;
            this.searchVertical = searchVertical;
            this.nearbyFallback = nearbyFallback;
            this.disallowWater = disallowWater;
            this.minY = minY;
        }

        public static SafetyConfig load(ConfigurationSection section) {
            if (section == null) {
                return new SafetyConfig(true, SafetyMode.CONFIRM, 8, 3, NearbyFallback.CONFIRM, true, -64);
            }
            return new SafetyConfig(
                    section.getBoolean("enabled", true),
                    SafetyMode.from(section.getString("mode", "CONFIRM")),
                    section.getInt("safety_search_radius", 8),
                    section.getInt("safety_search_vertical", 3),
                    NearbyFallback.from(section.getString("nearby_fallback", "CONFIRM")),
                    section.getBoolean("disallow_water", true),
                    section.getInt("min_y", -64)
            );
        }
    }

    public enum SafetyMode {
        CONFIRM,
        NEARBY_SAFE;

        public static SafetyMode from(String raw) {
            if (raw == null) {
                return CONFIRM;
            }
            try {
                return SafetyMode.valueOf(raw.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                return CONFIRM;
            }
        }
    }

    public enum NearbyFallback {
        CONFIRM,
        DENY;

        public static NearbyFallback from(String raw) {
            if (raw == null) {
                return CONFIRM;
            }
            try {
                return NearbyFallback.valueOf(raw.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                return CONFIRM;
            }
        }
    }

    public static final class CommandsConfig {
        public final boolean overrideTp;

        private CommandsConfig(boolean overrideTp) {
            this.overrideTp = overrideTp;
        }

        public static CommandsConfig load(ConfigurationSection section) {
            if (section == null) {
                return new CommandsConfig(false);
            }
            return new CommandsConfig(section.getBoolean("override_tp", false));
        }
    }
}
