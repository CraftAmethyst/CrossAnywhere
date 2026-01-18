package io.github.qianmo0721.crossAnywhere.manager;

import io.github.qianmo0721.crossAnywhere.i18n.MessageService;
import io.github.qianmo0721.crossAnywhere.teleport.PendingTeleport;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ConfirmManager {
    private final Plugin plugin;
    private MessageService messages;
    private final Map<UUID, PendingTeleport> pending = new ConcurrentHashMap<>();

    public ConfirmManager(Plugin plugin, MessageService messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    public void setMessages(MessageService messages) {
        this.messages = messages;
    }

    public void register(PendingTeleport teleport) {
        pending.put(teleport.getPlayerId(), teleport);
        long delayTicks = Math.max(1, (teleport.getExpiresAt() - Instant.now().getEpochSecond()) * 20L);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            PendingTeleport current = pending.get(teleport.getPlayerId());
            if (current != null && current.getExpiresAt() <= Instant.now().getEpochSecond()) {
                pending.remove(teleport.getPlayerId());
                Player player = Bukkit.getPlayer(teleport.getPlayerId());
                if (player != null && player.isOnline()) {
                    messages.send(player, "confirm.timeout");
                }
            }
        }, delayTicks);
    }

    public PendingTeleport get(UUID uuid) {
        PendingTeleport teleport = pending.get(uuid);
        if (teleport == null) {
            return null;
        }
        if (teleport.getExpiresAt() <= Instant.now().getEpochSecond()) {
            pending.remove(uuid);
            return null;
        }
        return teleport;
    }

    public void clear(UUID uuid) {
        pending.remove(uuid);
    }
}
