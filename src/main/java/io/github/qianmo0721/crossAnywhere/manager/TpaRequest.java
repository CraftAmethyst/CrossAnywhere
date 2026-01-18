package io.github.qianmo0721.crossAnywhere.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public final class TpaRequest {
    public enum Type {
        TO_TARGET,
        HERE
    }

    private final UUID senderId;
    private final UUID targetId;
    private final Type type;
    private final long createdAt;
    private final long expiresAt;
    private BukkitTask timeoutTask;

    public TpaRequest(UUID senderId, UUID targetId, Type type, long createdAt, long expiresAt) {
        this.senderId = senderId;
        this.targetId = targetId;
        this.type = type;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public Type getType() {
        return type;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public void setTimeoutTask(BukkitTask timeoutTask) {
        this.timeoutTask = timeoutTask;
    }

    public void cancelTimeout() {
        if (timeoutTask != null) {
            timeoutTask.cancel();
        }
    }

    public Player getSender() {
        return Bukkit.getPlayer(senderId);
    }

    public Player getTarget() {
        return Bukkit.getPlayer(targetId);
    }
}
