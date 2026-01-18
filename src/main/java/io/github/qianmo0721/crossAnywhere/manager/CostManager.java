package io.github.qianmo0721.crossAnywhere.manager;

import io.github.qianmo0721.crossAnywhere.config.PluginConfig;
import io.github.qianmo0721.crossAnywhere.util.ExpUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class CostManager {
    private final PluginConfig.CostConfig config;

    public CostManager(PluginConfig.CostConfig config) {
        this.config = config;
    }

    public CostResult calculate(Player player, Location from, Location to) {
        boolean crossworld = from != null && to != null
                && from.getWorld() != null
                && to.getWorld() != null
                && !from.getWorld().equals(to.getWorld());
        double distance = 0.0;
        if (from != null && to != null) {
            if (crossworld && config.crossworld.mode == PluginConfig.CrossworldMode.FIXED_DISTANCE) {
                distance = config.crossworld.distance;
            } else if (!crossworld && from.getWorld() != null && to.getWorld() != null) {
                distance = from.distance(to);
            }
        }

        int expCost = 0;
        if (config.exp.enabled) {
            expCost = config.exp.base + round(distance * config.exp.perBlock);
            if (crossworld && config.crossworld.mode == PluginConfig.CrossworldMode.EXTRA_COST) {
                expCost += config.crossworld.extraCost;
            }
        }

        int itemCost = 0;
        if (config.item.enabled) {
            itemCost = config.item.base + round(distance * config.item.perBlock);
            if (crossworld && config.crossworld.mode == PluginConfig.CrossworldMode.EXTRA_COST) {
                itemCost += config.crossworld.extraCost;
            }
        }

        int totalExp = player == null ? 0 : ExpUtil.getTotalExp(player);
        int itemCount = player == null ? 0 : countMatchingItems(player);

        boolean hasExp = !config.exp.enabled || totalExp >= expCost;
        boolean hasItems = !config.item.enabled || itemCount >= itemCost;

        return new CostResult(expCost, itemCost, hasExp && hasItems);
    }

    public void apply(Player player, CostResult result) {
        if (player == null || result == null) {
            return;
        }
        if (config.exp.enabled && result.getExpCost() > 0) {
            ExpUtil.removeExp(player, result.getExpCost());
        }
        if (config.item.enabled && result.getItemCost() > 0) {
            removeItems(player, result.getItemCost());
        }
    }

    private int round(double value) {
        return switch (config.rounding) {
            case FLOOR -> (int) Math.floor(value);
            case ROUND -> (int) Math.round(value);
            case CEIL -> (int) Math.ceil(value);
        };
    }

    private int countMatchingItems(Player player) {
        int count = 0;
        for (ItemStack stack : player.getInventory().getStorageContents()) {
            if (stack == null) {
                continue;
            }
            if (matchesItem(stack)) {
                count += stack.getAmount();
            }
        }
        return count;
    }

    private void removeItems(Player player, int amount) {
        int remaining = amount;
        ItemStack[] contents = player.getInventory().getStorageContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack stack = contents[i];
            if (stack == null) {
                continue;
            }
            if (!matchesItem(stack)) {
                continue;
            }
            int take = Math.min(stack.getAmount(), remaining);
            stack.setAmount(stack.getAmount() - take);
            remaining -= take;
            if (stack.getAmount() <= 0) {
                contents[i] = null;
            }
            if (remaining <= 0) {
                break;
            }
        }
        player.getInventory().setStorageContents(contents);
    }

    private boolean matchesItem(ItemStack stack) {
        if (stack.getType() != config.item.material) {
            return false;
        }
        if (config.item.customModelData < 0) {
            return true;
        }
        ItemMeta meta = stack.getItemMeta();
        if (meta == null || !meta.hasCustomModelData()) {
            return false;
        }
        return meta.getCustomModelData() == config.item.customModelData;
    }

    public static final class CostResult {
        private final int expCost;
        private final int itemCost;
        private final boolean affordable;

        public CostResult(int expCost, int itemCost, boolean affordable) {
            this.expCost = expCost;
            this.itemCost = itemCost;
            this.affordable = affordable;
        }

        public int getExpCost() {
            return expCost;
        }

        public int getItemCost() {
            return itemCost;
        }

        public boolean isAffordable() {
            return affordable;
        }
    }
}
