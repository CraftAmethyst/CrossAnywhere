package io.github.qianmo0721.crossAnywhere.util;

import org.bukkit.entity.Player;

public final class ExpUtil {
    private ExpUtil() {
    }

    public static int getTotalExp(Player player) {
        int level = player.getLevel();
        float progress = player.getExp();
        int expForLevel = getTotalExpForLevel(level);
        int expToNext = getExpToNext(level);
        return expForLevel + Math.round(progress * expToNext);
    }

    public static void removeExp(Player player, int amount) {
        if (amount <= 0) {
            return;
        }
        player.giveExp(-amount);
    }

    private static int getExpToNext(int level) {
        if (level >= 31) {
            return 9 * level - 158;
        }
        if (level >= 16) {
            return 5 * level - 38;
        }
        return 2 * level + 7;
    }

    public static int getTotalExpForLevel(int level) {
        if (level >= 32) {
            return (int) Math.round(4.5 * level * level - 162.5 * level + 2220);
        }
        if (level >= 17) {
            return (int) Math.round(2.5 * level * level - 40.5 * level + 360);
        }
        return level * level + 6 * level;
    }
}
