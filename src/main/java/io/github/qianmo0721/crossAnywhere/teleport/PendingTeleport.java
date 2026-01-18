package io.github.qianmo0721.crossAnywhere.teleport;

import org.bukkit.Location;

import java.util.UUID;

public final class PendingTeleport {
    private final UUID playerId;
    private final Location destination;
    private final TeleportType type;
    private final long expiresAt;

    public PendingTeleport(UUID playerId, Location destination, TeleportType type, long expiresAt) {
        this.playerId = playerId;
        this.destination = destination.clone();
        this.type = type;
        this.expiresAt = expiresAt;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public Location getDestination() {
        return destination.clone();
    }

    public TeleportType getType() {
        return type;
    }

    public long getExpiresAt() {
        return expiresAt;
    }
}
