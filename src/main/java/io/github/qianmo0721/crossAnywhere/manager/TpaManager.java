package io.github.qianmo0721.crossAnywhere.manager;

import io.github.qianmo0721.crossAnywhere.config.PluginConfig;
import io.github.qianmo0721.crossAnywhere.i18n.MessageService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TpaManager {
    private final Plugin plugin;
    private MessageService messages;
    private final Map<String, TpaRequest> requests = new ConcurrentHashMap<>();

    public TpaManager(Plugin plugin, MessageService messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    public void setMessages(MessageService messages) {
        this.messages = messages;
    }

    private String key(UUID sender, UUID target) {
        return sender + ":" + target;
    }

    public synchronized TpaRequest getRequest(UUID sender, UUID target) {
        return requests.get(key(sender, target));
    }

    public synchronized List<TpaRequest> getRequestsForTarget(UUID target) {
        List<TpaRequest> result = new ArrayList<>();
        for (TpaRequest request : requests.values()) {
            if (request.getTargetId().equals(target)) {
                result.add(request);
            }
        }
        result.sort(Comparator.comparingLong(TpaRequest::getCreatedAt).reversed());
        return result;
    }

    public synchronized TpaRequest getLatestForTarget(UUID target) {
        return getRequestsForTarget(target).stream().findFirst().orElse(null);
    }

    public synchronized boolean addRequest(TpaRequest request, PluginConfig.DuplicatePolicy policy) {
        String key = key(request.getSenderId(), request.getTargetId());
        TpaRequest existing = requests.get(key);
        if (existing != null) {
            if (policy == PluginConfig.DuplicatePolicy.REJECT) {
                return false;
            }
            remove(existing);
        }
        requests.put(key, request);
        long delayTicks = Math.max(1, (request.getExpiresAt() - Instant.now().getEpochSecond()) * 20L);
        request.setTimeoutTask(Bukkit.getScheduler().runTaskLater(plugin, () -> expire(request), delayTicks));
        return true;
    }

    private void expire(TpaRequest request) {
        synchronized (this) {
            TpaRequest current = requests.get(key(request.getSenderId(), request.getTargetId()));
            if (current != request) {
                return;
            }
            requests.remove(key(request.getSenderId(), request.getTargetId()));
        }
        Player sender = request.getSender();
        Player target = request.getTarget();
        if (sender != null && sender.isOnline()) {
            messages.send(sender, "tpa.timeout.sender", messages.placeholder("player", target == null ? "?" : target.getName()));
        }
        if (target != null && target.isOnline()) {
            messages.send(target, "tpa.timeout.target", messages.placeholder("player", sender == null ? "?" : sender.getName()));
        }
    }

    public synchronized void remove(TpaRequest request) {
        request.cancelTimeout();
        requests.remove(key(request.getSenderId(), request.getTargetId()));
    }

    public synchronized int removeAllFromSender(UUID sender) {
        return removeAllFromSenderWithList(sender).size();
    }

    public synchronized List<TpaRequest> removeAllFromSenderWithList(UUID sender) {
        List<TpaRequest> toRemove = new ArrayList<>();
        for (TpaRequest request : requests.values()) {
            if (request.getSenderId().equals(sender)) {
                toRemove.add(request);
            }
        }
        for (TpaRequest request : toRemove) {
            remove(request);
        }
        return toRemove;
    }

    public synchronized List<TpaRequest> getRequestsFromSender(UUID sender) {
        List<TpaRequest> result = new ArrayList<>();
        for (TpaRequest request : requests.values()) {
            if (request.getSenderId().equals(sender)) {
                result.add(request);
            }
        }
        return result;
    }
}
