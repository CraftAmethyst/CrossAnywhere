package io.github.qianmo0721.crossAnywhere.manager;

import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CooldownManager {
    private final Map<UUID, EnumMap<CooldownType, Long>> cooldowns = new ConcurrentHashMap<>();

    public long getRemainingSeconds(UUID uuid, CooldownType type) {
        long now = Instant.now().getEpochSecond();
        long until = cooldowns.getOrDefault(uuid, new EnumMap<>(CooldownType.class))
                .getOrDefault(type, 0L);
        long remaining = until - now;
        return Math.max(0, remaining);
    }

    public void setCooldown(UUID uuid, CooldownType type, int seconds) {
        if (seconds <= 0) {
            return;
        }
        long until = Instant.now().getEpochSecond() + seconds;
        cooldowns.computeIfAbsent(uuid, ignored -> new EnumMap<>(CooldownType.class))
                .put(type, until);
    }

    public enum CooldownType {
        WAYPOINT,
        TP,
        BACK
    }
}
