package io.github.qianmo0721.crossAnywhere.teleport;

import io.github.qianmo0721.crossAnywhere.config.PluginConfig;
import io.github.qianmo0721.crossAnywhere.i18n.MessageService;
import io.github.qianmo0721.crossAnywhere.manager.BackManager;
import io.github.qianmo0721.crossAnywhere.manager.ConfirmManager;
import io.github.qianmo0721.crossAnywhere.manager.CooldownManager;
import io.github.qianmo0721.crossAnywhere.manager.CostManager;
import io.github.qianmo0721.crossAnywhere.safety.NearbySafeFinder;
import io.github.qianmo0721.crossAnywhere.safety.SafetyChecker;
import io.github.qianmo0721.crossAnywhere.safety.SafetyResult;
import io.github.qianmo0721.crossAnywhere.util.TimeUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.time.Instant;

public final class TeleportService {
    private final PluginConfig config;
    private final MessageService messages;
    private final CooldownManager cooldowns;
    private final CostManager costs;
    private final SafetyChecker safetyChecker;
    private final NearbySafeFinder safeFinder;
    private final ConfirmManager confirmManager;
    private final BackManager backManager;

    public TeleportService(PluginConfig config,
                           MessageService messages,
                           CooldownManager cooldowns,
                           CostManager costs,
                           SafetyChecker safetyChecker,
                           NearbySafeFinder safeFinder,
                           ConfirmManager confirmManager,
                           BackManager backManager) {
        this.config = config;
        this.messages = messages;
        this.cooldowns = cooldowns;
        this.costs = costs;
        this.safetyChecker = safetyChecker;
        this.safeFinder = safeFinder;
        this.confirmManager = confirmManager;
        this.backManager = backManager;
    }

    public TeleportResult teleport(Player player, Location destination, TeleportType type, boolean skipSafety) {
        if (player == null || destination == null || destination.getWorld() == null) {
            return TeleportResult.FAILED;
        }
        World destinationWorld = destination.getWorld();
        if (!config.isWorldAllowed(player.getWorld().getName())) {
            messages.send(player, "world.not_allowed");
            return TeleportResult.FAILED;
        }
        if (!config.isWorldAllowed(destinationWorld.getName())) {
            messages.send(player, "world.not_allowed_target",
                    messages.placeholder("world", destinationWorld.getName()));
            return TeleportResult.FAILED;
        }

        if (!player.getWorld().equals(destinationWorld)
                && !player.hasPermission("crossanywhere.crossworld")) {
            messages.send(player, "world.crossworld_denied",
                    messages.placeholder("world", destinationWorld.getName()));
            return TeleportResult.FAILED;
        }

        if (!player.hasPermission("crossanywhere.cooldown.bypass")) {
            long remaining = cooldowns.getRemainingSeconds(player.getUniqueId(), type.getCooldownType());
            if (remaining > 0) {
                messages.send(player, "cooldown.wait",
                        messages.placeholder("time", TimeUtil.formatSeconds(remaining)));
                return TeleportResult.FAILED;
            }
        }

        CostManager.CostResult costResult = null;
        if (!player.hasPermission("crossanywhere.cost.bypass")
                && (config.cost.exp.enabled || config.cost.item.enabled)) {
            costResult = costs.calculate(player, player.getLocation(), destination);
            if (!costResult.isAffordable()) {
                messages.send(player, "cost.not_enough",
                        messages.placeholder("exp", String.valueOf(costResult.getExpCost())),
                        messages.placeholder("items", String.valueOf(costResult.getItemCost())));
                return TeleportResult.FAILED;
            }
        }

        if (config.safety.enabled && !skipSafety && !player.hasPermission("crossanywhere.safety.bypass")) {
            SafetyResult result = safetyChecker.check(destination);
            if (!result.isSafe()) {
                if (config.safety.mode == PluginConfig.SafetyMode.NEARBY_SAFE) {
                    Location safe = safeFinder.find(destination, config.safety.searchRadius,
                            config.safety.searchVertical, safetyChecker);
                    if (safe != null) {
                        messages.send(player, "safety.moved");
                        destination = safe;
                    } else if (config.safety.nearbyFallback == PluginConfig.NearbyFallback.CONFIRM) {
                        return requestConfirm(player, destination, type, reasonMessage(player, result.getReason()));
                    } else {
                        messages.send(player, "safety.denied");
                        return TeleportResult.FAILED;
                    }
                } else {
                    return requestConfirm(player, destination, type, reasonMessage(player, result.getReason()));
                }
            }
        }

        Location before = player.getLocation().clone();
        boolean success = player.teleport(destination);
        if (!success) {
            messages.send(player, "teleport.failed");
            return TeleportResult.FAILED;
        }

        backManager.setBack(player.getUniqueId(), before);
        if (!player.hasPermission("crossanywhere.cooldown.bypass")) {
            int seconds = switch (type.getCooldownType()) {
                case WAYPOINT -> config.cooldown.waypointSeconds;
                case TP -> config.cooldown.tpSeconds;
                case BACK -> config.cooldown.backSeconds;
            };
            cooldowns.setCooldown(player.getUniqueId(), type.getCooldownType(), seconds);
        }
        if (!player.hasPermission("crossanywhere.cost.bypass") && costResult != null) {
            costs.apply(player, costResult);
        }

        Component backButton = messages.component(player, "back.button");
        messages.send(player, "back.saved", TagResolver.resolver(messages.placeholder("button", backButton)));
        return TeleportResult.SUCCESS;
    }

    private TeleportResult requestConfirm(Player player, Location destination, TeleportType type, String reason) {
        long expiresAt = Instant.now().getEpochSecond() + config.confirmTimeoutSeconds;
        confirmManager.register(new PendingTeleport(player.getUniqueId(), destination, type, expiresAt));
        Component confirm = messages.component(player, "safety.button.confirm");
        Component cancel = messages.component(player, "safety.button.cancel");
        messages.send(player, "safety.confirm",
                messages.placeholder("reason", reason == null ? "" : reason),
                messages.placeholder("confirm", confirm),
                messages.placeholder("cancel", cancel));
        return TeleportResult.PENDING_CONFIRM;
    }

    private String reasonMessage(Player player, String reasonKey) {
        if (reasonKey == null || reasonKey.isEmpty()) {
            return "";
        }
        String message = messages.resolveMessage(player, "safety.reason." + reasonKey);
        if (message == null || message.startsWith("[")) {
            return reasonKey;
        }
        return message;
    }
}
