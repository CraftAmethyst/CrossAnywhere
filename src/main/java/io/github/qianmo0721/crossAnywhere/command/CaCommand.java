package io.github.qianmo0721.crossAnywhere.command;

import io.github.qianmo0721.crossAnywhere.CrossAnywhere;
import io.github.qianmo0721.crossAnywhere.config.PluginConfig;
import io.github.qianmo0721.crossAnywhere.i18n.MessageService;
import io.github.qianmo0721.crossAnywhere.importer.StpImporter;
import io.github.qianmo0721.crossAnywhere.manager.BackManager;
import io.github.qianmo0721.crossAnywhere.manager.ConfirmManager;
import io.github.qianmo0721.crossAnywhere.manager.TpaManager;
import io.github.qianmo0721.crossAnywhere.manager.TpaRequest;
import io.github.qianmo0721.crossAnywhere.model.Waypoint;
import io.github.qianmo0721.crossAnywhere.repository.WaypointRepository;
import io.github.qianmo0721.crossAnywhere.teleport.PendingTeleport;
import io.github.qianmo0721.crossAnywhere.teleport.TeleportResult;
import io.github.qianmo0721.crossAnywhere.teleport.TeleportService;
import io.github.qianmo0721.crossAnywhere.teleport.TeleportType;
import io.github.qianmo0721.crossAnywhere.util.NameValidator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public final class CaCommand implements CommandExecutor, TabCompleter {
    private static final List<String> SUB_COMMANDS = List.of(
            "help",
            "setp", "setpersonal",
            "tpp", "tpersonal",
            "delp", "delpersonal",
            "listp", "listpersonal",
            "descp",
            "setg", "setglobal",
            "tpg", "tglobal",
            "delg", "delglobal",
            "listg", "listglobal",
            "descg",
            "list",
            "tp",
            "tphere",
            "tpa",
            "tpahere",
            "cancel",
            "accept", "allow",
            "deny", "reject",
            "back",
            "confirm",
            "cancelconfirm",
            "reload",
            "importstp"
    );

    private final CrossAnywhere plugin;
    private PluginConfig config;
    private MessageService messages;
    private final WaypointRepository repository;
    private TeleportService teleports;
    private final TpaManager tpaManager;
    private final ConfirmManager confirmManager;
    private final BackManager backManager;

    public CaCommand(CrossAnywhere plugin,
                     PluginConfig config,
                     MessageService messages,
                     WaypointRepository repository,
                     TeleportService teleports,
                     TpaManager tpaManager,
                     ConfirmManager confirmManager,
                     BackManager backManager) {
        this.plugin = plugin;
        this.config = config;
        this.messages = messages;
        this.repository = repository;
        this.teleports = teleports;
        this.tpaManager = tpaManager;
        this.confirmManager = confirmManager;
        this.backManager = backManager;
    }

    public void updateContext(PluginConfig config, MessageService messages, TeleportService teleports) {
        this.config = config;
        this.messages = messages;
        this.teleports = teleports;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            messages.send(sender, "help.header");
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);

        if (!sub.equals("help") && sender instanceof Player player) {
            if (!config.isWorldAllowed(player.getWorld().getName())) {
                messages.send(sender, "world.not_allowed");
                return true;
            }
        }

        switch (sub) {
            case "help" -> messages.send(sender, "help.header");
            case "setp", "setpersonal" -> handleSetWaypoint(sender, args, true);
            case "setg", "setglobal" -> handleSetWaypoint(sender, args, false);
            case "tpp", "tpersonal" -> handleTeleportWaypoint(sender, args, true);
            case "tpg", "tglobal" -> handleTeleportWaypoint(sender, args, false);
            case "delp", "delpersonal" -> handleDeleteWaypoint(sender, args, true);
            case "delg", "delglobal" -> handleDeleteWaypoint(sender, args, false);
            case "listp", "listpersonal" -> handleList(sender, true, false);
            case "listg", "listglobal" -> handleList(sender, false, true);
            case "list" -> handleList(sender, true, true);
            case "descp" -> handleDesc(sender, args, true);
            case "descg" -> handleDesc(sender, args, false);
            case "tp" -> handleDirectTeleport(sender, args, true);
            case "tphere" -> handleDirectTeleport(sender, args, false);
            case "tpa" -> handleTpaRequest(sender, args, true);
            case "tpahere" -> handleTpaRequest(sender, args, false);
            case "cancel" -> handleCancel(sender);
            case "accept", "allow" -> handleAccept(sender, args);
            case "deny", "reject" -> handleDeny(sender, args);
            case "back" -> handleBack(sender);
            case "confirm" -> handleConfirm(sender);
            case "cancelconfirm" -> handleCancelConfirm(sender);
            case "reload" -> handleReload(sender);
            case "importstp" -> handleImportStp(sender, args);
            default -> {
                if (config.easyTp && args.length == 1) {
                    handleEasyTeleport(sender, sub);
                } else {
                    messages.send(sender, "help.header");
                }
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            List<String> suggestions = new ArrayList<>();
            for (String sub : SUB_COMMANDS) {
                if (sub.startsWith(prefix)) {
                    suggestions.add(sub);
                }
            }
            if (sender instanceof Player player && config.easyTp) {
                suggestions.addAll(suggestWaypointNames(player.getUniqueId(), prefix));
                suggestions.addAll(suggestPlayerNames(prefix));
            }
            return suggestions;
        }
        if (args.length >= 2) {
            String sub = args[0].toLowerCase(Locale.ROOT);
            String prefix = args[args.length - 1].toLowerCase(Locale.ROOT);
            if (sender instanceof Player player) {
                switch (sub) {
                    case "tpp", "tpersonal", "delp", "delpersonal", "descp" -> {
                        return suggestWaypointNames(player.getUniqueId(), prefix);
                    }
                    case "tpg", "tglobal", "delg", "delglobal", "descg" -> {
                        return suggestGlobalNames(prefix);
                    }
                    case "tp", "tpa", "tpahere", "tphere" -> {
                        return suggestPlayerNames(prefix);
                    }
                    case "accept", "allow", "deny", "reject" -> {
                        return suggestPendingSenderNames(player.getUniqueId(), prefix);
                    }
                    case "importstp" -> {
                        return suggestImportFlags(prefix);
                    }
                    default -> {
                        return List.of();
                    }
                }
            }
        }
        return List.of();
    }

    private void handleSetWaypoint(CommandSender sender, String[] args, boolean personal) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "player_only");
            return;
        }
        if (!hasPermission(player, personal ? "crossanywhere.personal" : "crossanywhere.global")) {
            messages.send(sender, "no_permission");
            return;
        }
        int index = 1;
        boolean force = false;
        if (args.length > 1 && "-f".equalsIgnoreCase(args[1])) {
            force = true;
            index++;
        }
        if (args.length <= index) {
            messages.send(sender, "usage.set");
            return;
        }
        String name = NameValidator.normalize(args[index]);
        if (!NameValidator.isValid(name, config.waypointNameMaxLength, config.allowUnicodeNames)) {
            messages.send(sender, "waypoint.invalid_name",
                    messages.placeholder("max", String.valueOf(config.waypointNameMaxLength)));
            return;
        }
        String desc = args.length > index + 1 ? String.join(" ", Arrays.copyOfRange(args, index + 1, args.length)) : "";
        UUID owner = personal ? player.getUniqueId() : null;
        if (personal) {
            Waypoint existing = repository.getPersonal(player.getUniqueId(), name);
            boolean exists = existing != null;
            if (!exists && repository.countPersonal(player.getUniqueId()) >= config.personalMaxWaypoints) {
                messages.send(sender, "waypoint.limit", messages.placeholder("max", String.valueOf(config.personalMaxWaypoints)));
                return;
            }
            if (exists && !force) {
                messages.send(sender, "waypoint.exists");
                return;
            }
            repository.setPersonal(player.getUniqueId(), buildWaypoint(player, name, desc, owner, existing));
            messages.send(sender, exists ? "waypoint.updated" : "waypoint.created",
                    messages.placeholder("name", name));
        } else {
            Waypoint existing = repository.getGlobal(name);
            boolean exists = existing != null;
            if (!exists && repository.countGlobal() >= config.globalMaxWaypoints) {
                messages.send(sender, "waypoint.limit", messages.placeholder("max", String.valueOf(config.globalMaxWaypoints)));
                return;
            }
            if (exists && !force) {
                messages.send(sender, "waypoint.exists");
                return;
            }
            repository.setGlobal(buildWaypoint(player, name, desc, owner, existing));
            messages.send(sender, exists ? "waypoint.updated" : "waypoint.created",
                    messages.placeholder("name", name));
        }
    }

    private Waypoint buildWaypoint(Player player, String name, String desc, UUID owner, Waypoint existing) {
        long now = Instant.now().getEpochSecond();
        long createdAt = existing == null ? now : existing.getCreatedAt();
        Location loc = player.getLocation();
        return new Waypoint(name,
                Objects.requireNonNull(loc.getWorld()).getName(),
                loc.getX(), loc.getY(), loc.getZ(),
                loc.getYaw(), loc.getPitch(),
                desc,
                owner,
                createdAt,
                now);
    }

    private void handleTeleportWaypoint(CommandSender sender, String[] args, boolean personal) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "player_only");
            return;
        }
        if (!hasPermission(player, personal ? "crossanywhere.personal.tp" : "crossanywhere.global.tp")) {
            messages.send(sender, "no_permission");
            return;
        }
        if (args.length < 2) {
            messages.send(sender, "usage.tp_waypoint");
            return;
        }
        String name = NameValidator.normalize(args[1]);
        Waypoint waypoint = personal ? repository.getPersonal(player.getUniqueId(), name) : repository.getGlobal(name);
        if (waypoint == null) {
            messages.send(sender, "waypoint.not_found", messages.placeholder("name", name));
            return;
        }
        World world = Bukkit.getWorld(waypoint.getWorld());
        if (world == null) {
            messages.send(sender, "world.missing", messages.placeholder("world", waypoint.getWorld()));
            return;
        }
        Location destination = waypoint.toLocation(world);
        TeleportType type = personal ? TeleportType.WAYPOINT_PERSONAL : TeleportType.WAYPOINT_GLOBAL;
        TeleportResult result = teleports.teleport(player, destination, type, false);
        if (result == TeleportResult.SUCCESS) {
            messages.send(sender, "teleport.success_waypoint", messages.placeholder("name", waypoint.getName()));
        }
    }

    private void handleDeleteWaypoint(CommandSender sender, String[] args, boolean personal) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "player_only");
            return;
        }
        if (!hasPermission(player, personal ? "crossanywhere.personal" : "crossanywhere.global")) {
            messages.send(sender, "no_permission");
            return;
        }
        if (args.length < 2) {
            messages.send(sender, "usage.del");
            return;
        }
        String name = NameValidator.normalize(args[1]);
        boolean removed = personal
                ? repository.deletePersonal(player.getUniqueId(), name)
                : repository.deleteGlobal(name);
        if (removed) {
            messages.send(sender, "waypoint.deleted", messages.placeholder("name", name));
        } else {
            messages.send(sender, "waypoint.not_found", messages.placeholder("name", name));
        }
    }

    private void handleDesc(CommandSender sender, String[] args, boolean personal) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "player_only");
            return;
        }
        if (!hasPermission(player, personal ? "crossanywhere.personal" : "crossanywhere.global")) {
            messages.send(sender, "no_permission");
            return;
        }
        if (args.length < 3) {
            messages.send(sender, "usage.desc");
            return;
        }
        String name = NameValidator.normalize(args[1]);
        String desc = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        Waypoint waypoint = personal ? repository.getPersonal(player.getUniqueId(), name) : repository.getGlobal(name);
        if (waypoint == null) {
            messages.send(sender, "waypoint.not_found", messages.placeholder("name", name));
            return;
        }
        waypoint.setDescription(desc);
        waypoint.setUpdatedAt(Instant.now().getEpochSecond());
        if (personal) {
            repository.setPersonal(player.getUniqueId(), waypoint);
        } else {
            repository.setGlobal(waypoint);
        }
        messages.send(sender, "waypoint.desc_updated", messages.placeholder("name", waypoint.getName()));
    }

    private void handleList(CommandSender sender, boolean showPersonal, boolean showGlobal) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "player_only");
            return;
        }
        if (!hasPermission(player, "crossanywhere.list")) {
            messages.send(sender, "no_permission");
            return;
        }
        List<Waypoint> personalList = showPersonal ? repository.listPersonal(player.getUniqueId()) : List.of();
        List<Waypoint> globalList = showGlobal ? repository.listGlobal() : List.of();
        messages.send(sender, "list.header");
        if (showPersonal) {
            sendListSection(player, "list.personal", personalList, true);
        }
        if (showGlobal) {
            sendListSection(player, "list.global", globalList, false);
        }
    }

    private void sendListSection(Player player, String headerKey, List<Waypoint> waypoints, boolean personal) {
        messages.send(player, headerKey,
                messages.placeholder("count", String.valueOf(waypoints.size())));
        waypoints.stream()
                .sorted(Comparator.comparing(Waypoint::getName, String.CASE_INSENSITIVE_ORDER))
                .forEach(waypoint -> player.sendMessage(buildWaypointEntry(player, waypoint, personal)));
    }

    private Component buildWaypointEntry(Player player, Waypoint waypoint, boolean personal) {
        String teleportCommand = personal
                ? "/ca tpp " + waypoint.getName()
                : "/ca tpg " + waypoint.getName();
        Component teleportButton = button(player, "list.button.teleport.label", teleportCommand, "list.button.teleport.hover");

        Component deleteButton = Component.empty();
        if (hasPermission(player, personal ? "crossanywhere.personal" : "crossanywhere.global")) {
            String deleteCommand = personal
                    ? "/ca delp " + waypoint.getName()
                    : "/ca delg " + waypoint.getName();
            deleteButton = button(player, "list.button.delete.label", deleteCommand, "list.button.delete.hover");
        }

        Component descButton = Component.empty();
        if (hasPermission(player, personal ? "crossanywhere.personal" : "crossanywhere.global")) {
            String suggestCommand = personal
                    ? "/ca descp " + waypoint.getName() + " "
                    : "/ca descg " + waypoint.getName() + " ";
            Component label = messages.component(player, "list.button.desc.label");
            descButton = label.clickEvent(ClickEvent.suggestCommand(suggestCommand))
                    .hoverEvent(HoverEvent.showText(messages.component(player, "list.button.desc.hover")));
        }

        Component descComponent = Component.text(waypoint.getDescription() == null ? "" : waypoint.getDescription());
        if (waypoint.getDescription() != null && waypoint.getDescription().length() > 30) {
            String shortDesc = waypoint.getDescription().substring(0, 30) + "...";
            descComponent = Component.text(shortDesc)
                    .hoverEvent(HoverEvent.showText(Component.text(waypoint.getDescription())));
        }

        TagResolver resolver = TagResolver.resolver(
                messages.placeholder("name", waypoint.getName()),
                messages.placeholder("world", waypoint.getWorld()),
                messages.placeholder("x", String.format(Locale.US, "%.1f", waypoint.getX())),
                messages.placeholder("y", String.format(Locale.US, "%.1f", waypoint.getY())),
                messages.placeholder("z", String.format(Locale.US, "%.1f", waypoint.getZ())),
                messages.placeholder("yaw", String.format(Locale.US, "%.1f", waypoint.getYaw())),
                messages.placeholder("pitch", String.format(Locale.US, "%.1f", waypoint.getPitch())),
                messages.placeholder("desc", descComponent),
                messages.placeholder("teleport", teleportButton),
                messages.placeholder("delete", deleteButton),
                messages.placeholder("edit", descButton)
        );
        return messages.component(player, "list.entry", resolver);
    }

    private Component button(Player player, String labelKey, String command, String hoverKey) {
        Component label = messages.component(player, labelKey);
        Component hover = messages.component(player, hoverKey);
        return label.clickEvent(ClickEvent.runCommand(command)).hoverEvent(HoverEvent.showText(hover));
    }

    private void handleDirectTeleport(CommandSender sender, String[] args, boolean toTarget) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "player_only");
            return;
        }
        if (!hasPermission(player, toTarget ? "crossanywhere.tp" : "crossanywhere.tphere")) {
            messages.send(sender, "no_permission");
            return;
        }
        if (args.length < 2) {
            messages.send(sender, "usage.tp_player");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            messages.send(sender, "player.not_found", messages.placeholder("player", args[1]));
            return;
        }
        Player teleported = toTarget ? player : target;
        Location destination = toTarget ? target.getLocation() : player.getLocation();
        TeleportType type = toTarget ? TeleportType.TP : TeleportType.TPHERE;
        TeleportResult result = teleports.teleport(teleported, destination, type, false);
        if (result == TeleportResult.SUCCESS) {
            if (toTarget) {
                messages.send(sender, "tp.success", messages.placeholder("player", target.getName()));
            } else {
                messages.send(sender, "tphere.success", messages.placeholder("player", target.getName()));
            }
        }
    }

    private void handleTpaRequest(CommandSender sender, String[] args, boolean toTarget) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "player_only");
            return;
        }
        if (!hasPermission(player, toTarget ? "crossanywhere.tpa" : "crossanywhere.tpahere")) {
            messages.send(sender, "no_permission");
            return;
        }
        if (args.length < 2) {
            messages.send(sender, "usage.tpa");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            messages.send(sender, "player.not_found", messages.placeholder("player", args[1]));
            return;
        }
        if (target.getUniqueId().equals(player.getUniqueId())) {
            messages.send(sender, "tpa.self");
            return;
        }
        if (!config.isWorldAllowed(target.getWorld().getName())) {
            messages.send(sender, "world.not_allowed_target", messages.placeholder("world", target.getWorld().getName()));
            return;
        }
        if (!player.getWorld().equals(target.getWorld())
                && !player.hasPermission("crossanywhere.crossworld")) {
            messages.send(sender, "world.crossworld_denied", messages.placeholder("world", target.getWorld().getName()));
            return;
        }

        long now = Instant.now().getEpochSecond();
        TpaRequest.Type type = toTarget ? TpaRequest.Type.TO_TARGET : TpaRequest.Type.HERE;
        TpaRequest request = new TpaRequest(player.getUniqueId(), target.getUniqueId(), type, now,
                now + config.tpaTimeoutSeconds);

        boolean added = tpaManager.addRequest(request, config.tpaDuplicatePolicy);
        if (!added) {
            messages.send(sender, "tpa.duplicate");
            return;
        }

        Component accept = button(target, "tpa.accept.label", "/ca accept " + player.getName(), "tpa.accept.hover");
        Component deny = button(target, "tpa.deny.label", "/ca deny " + player.getName(), "tpa.deny.hover");
        messages.send(target, toTarget ? "tpa.request.to_target" : "tpa.request.here",
                messages.placeholder("player", player.getName()),
                messages.placeholder("accept", accept),
                messages.placeholder("deny", deny));
        messages.send(sender, "tpa.sent", messages.placeholder("player", target.getName()));
    }

    private void handleCancel(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "player_only");
            return;
        }
        List<TpaRequest> removed = tpaManager.removeAllFromSenderWithList(player.getUniqueId());
        if (removed.isEmpty()) {
            messages.send(sender, "tpa.none");
            return;
        }
        messages.send(sender, "tpa.cancelled");
        for (TpaRequest request : removed) {
            Player target = request.getTarget();
            if (target != null && target.isOnline()) {
                messages.send(target, "tpa.cancelled_target", messages.placeholder("player", player.getName()));
            }
        }
    }

    private void handleAccept(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "player_only");
            return;
        }
        TpaRequest request = findRequestForTarget(player, args);
        if (request == null) {
            messages.send(sender, "tpa.none");
            return;
        }
        tpaManager.remove(request);
        Player requester = request.getSender();
        if (requester == null) {
            messages.send(sender, "player.not_found", messages.placeholder("player", "?"));
            return;
        }
        messages.send(sender, "tpa.accepted", messages.placeholder("player", requester.getName()));
        messages.send(requester, "tpa.accepted_sender", messages.placeholder("player", player.getName()));
        if (request.getType() == TpaRequest.Type.TO_TARGET) {
            teleports.teleport(requester, player.getLocation(), TeleportType.TPA, false);
        } else {
            teleports.teleport(player, requester.getLocation(), TeleportType.TPAHERE, false);
        }
    }

    private void handleDeny(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "player_only");
            return;
        }
        TpaRequest request = findRequestForTarget(player, args);
        if (request == null) {
            messages.send(sender, "tpa.none");
            return;
        }
        tpaManager.remove(request);
        Player requester = request.getSender();
        if (requester != null && requester.isOnline()) {
            messages.send(requester, "tpa.denied", messages.placeholder("player", player.getName()));
        }
        messages.send(sender, "tpa.denied_target", messages.placeholder("player", requester == null ? "?" : requester.getName()));
    }

    private TpaRequest findRequestForTarget(Player player, String[] args) {
        if (args.length >= 2) {
            Player sender = Bukkit.getPlayerExact(args[1]);
            if (sender == null) {
                return null;
            }
            return tpaManager.getRequest(sender.getUniqueId(), player.getUniqueId());
        }
        return tpaManager.getLatestForTarget(player.getUniqueId());
    }

    private void handleBack(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "player_only");
            return;
        }
        if (!hasPermission(player, "crossanywhere.back")) {
            messages.send(sender, "no_permission");
            return;
        }
        Location back = backManager.getBack(player.getUniqueId());
        if (back == null) {
            messages.send(sender, "back.none");
            return;
        }
        TeleportResult result = teleports.teleport(player, back, TeleportType.BACK, false);
        if (result == TeleportResult.SUCCESS) {
            messages.send(sender, "back.success");
        }
    }

    private void handleConfirm(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "player_only");
            return;
        }
        PendingTeleport pending = confirmManager.get(player.getUniqueId());
        if (pending == null) {
            messages.send(sender, "confirm.none");
            return;
        }
        confirmManager.clear(player.getUniqueId());
        TeleportResult result = teleports.teleport(player, pending.getDestination(), pending.getType(), true);
        if (result == TeleportResult.SUCCESS) {
            messages.send(sender, "confirm.success");
        }
    }

    private void handleCancelConfirm(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "player_only");
            return;
        }
        confirmManager.clear(player.getUniqueId());
        messages.send(sender, "confirm.cancelled");
    }

    private void handleReload(CommandSender sender) {
        if (sender instanceof Player player) {
            if (!player.hasPermission("crossanywhere.admin")) {
                messages.send(sender, "no_permission");
                return;
            }
        }
        plugin.reloadAll();
        messages.send(sender, "reload.success");
    }

    private void handleImportStp(CommandSender sender, String[] args) {
        if (sender instanceof Player player && !player.hasPermission("crossanywhere.admin")) {
            messages.send(sender, "no_permission");
            return;
        }

        ImportOptions options = ImportOptions.parse(args);
        if (options == null) {
            messages.send(sender, "usage.importstp");
            return;
        }

        Path dataFolder = plugin.getDataFolder().toPath();
        Path inputFile = dataFolder.resolve(options.fileName);
        if (!Files.exists(inputFile)) {
            messages.send(sender, "importstp.missing", messages.placeholder("file", options.fileName));
            return;
        }

        messages.send(sender, "importstp.start", messages.placeholder("file", options.fileName));

        List<String> warnings = new ArrayList<>();
        Map<String, String> uuidMap = StpImporter.loadStringMap(dataFolder.resolve("stp_uuid_map.json"), warnings);
        Map<String, String> worldMap = StpImporter.loadStringMap(dataFolder.resolve("stp_world_map.json"), warnings);

        StpImporter.Result result;
        try {
            result = StpImporter.load(inputFile, options.uuidMode, options.includeBack, uuidMap, worldMap);
        } catch (Exception ex) {
            String message = ex.getMessage() == null ? "unknown error" : ex.getMessage();
            messages.send(sender, "importstp.failed", messages.placeholder("error", message));
            plugin.getLogger().warning("STP import failed: " + message);
            return;
        }

        if (options.clear) {
            repository.replaceAll(result.personal(), result.global());
        } else {
            for (Map.Entry<String, Map<String, Waypoint>> entry : result.personal().entrySet()) {
                UUID owner;
                try {
                    owner = UUID.fromString(entry.getKey());
                } catch (Exception ex) {
                    warnings.add("Invalid UUID key in import data: " + entry.getKey());
                    continue;
                }
                for (Waypoint waypoint : entry.getValue().values()) {
                    repository.setPersonal(owner, waypoint);
                }
            }
            for (Waypoint waypoint : result.global().values()) {
                repository.setGlobal(waypoint);
            }
        }

        repository.save();

        messages.send(sender, "importstp.done",
                messages.placeholder("players", String.valueOf(result.personalPlayers())),
                messages.placeholder("personal", String.valueOf(result.personalWaypoints())),
                messages.placeholder("global", String.valueOf(result.globalWaypoints())),
                messages.placeholder("skipped", String.valueOf(result.skipped())));

        if (!result.warnings().isEmpty()) {
            warnings.addAll(result.warnings());
        }
        if (!warnings.isEmpty()) {
            messages.send(sender, "importstp.warnings",
                    messages.placeholder("count", String.valueOf(warnings.size())));
            for (String warning : warnings) {
                plugin.getLogger().warning("[STP Import] " + warning);
            }
        }
    }

    private void handleEasyTeleport(CommandSender sender, String name) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "player_only");
            return;
        }
        if (!hasPermission(player, "crossanywhere.easy")) {
            messages.send(sender, "no_permission");
            return;
        }
        if (!NameValidator.isValid(name, config.waypointNameMaxLength, config.allowUnicodeNames)) {
            messages.send(sender, "waypoint.invalid_name",
                    messages.placeholder("max", String.valueOf(config.waypointNameMaxLength)));
            return;
        }
        Waypoint personal = repository.getPersonal(player.getUniqueId(), name);
        if (personal != null) {
            handleTeleportWaypoint(sender, new String[]{"tpp", name}, true);
            return;
        }
        Waypoint global = repository.getGlobal(name);
        if (global != null) {
            handleTeleportWaypoint(sender, new String[]{"tpg", name}, false);
            return;
        }
        Player target = Bukkit.getPlayerExact(name);
        if (target != null) {
            if (hasPermission(player, "crossanywhere.tp")) {
                handleDirectTeleport(sender, new String[]{"tp", name}, true);
                return;
            }
            if (hasPermission(player, "crossanywhere.tpa")) {
                handleTpaRequest(sender, new String[]{"tpa", name}, true);
                return;
            }
            messages.send(sender, "no_permission");
            return;
        }
        messages.send(sender, "waypoint.not_found", messages.placeholder("name", name));
    }

    private boolean hasPermission(Player player, String permission) {
        return player.hasPermission("crossanywhere.admin") || player.hasPermission(permission);
    }

    private List<String> suggestImportFlags(String prefix) {
        List<String> flags = List.of(
                "--include-back",
                "--clear",
                "--offline-uuid",
                "--raw-uuid",
                "--auto-uuid"
        );
        return flags.stream()
                .filter(flag -> flag.startsWith(prefix))
                .collect(Collectors.toList());
    }

    private List<String> suggestWaypointNames(UUID uuid, String prefix) {
        return repository.listPersonal(uuid).stream()
                .map(Waypoint::getName)
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(prefix))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

    private List<String> suggestGlobalNames(String prefix) {
        return repository.listGlobal().stream()
                .map(Waypoint::getName)
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(prefix))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

    private List<String> suggestPlayerNames(String prefix) {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(prefix))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

    private List<String> suggestPendingSenderNames(UUID target, String prefix) {
        return tpaManager.getRequestsForTarget(target).stream()
                .map(TpaRequest::getSender)
                .filter(Objects::nonNull)
                .map(Player::getName)
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(prefix))
                .collect(Collectors.toList());
    }

    private record ImportOptions(String fileName, boolean includeBack, boolean clear, StpImporter.UuidMode uuidMode) {

        private static ImportOptions parse(String[] args) {
                String fileName = "example_data.json";
                boolean includeBack = false;
                boolean clear = false;
                StpImporter.UuidMode uuidMode = StpImporter.UuidMode.BUKKIT;
                boolean fileSet = false;

                for (int i = 1; i < args.length; i++) {
                    String arg = args[i];
                    String lower = arg.toLowerCase(Locale.ROOT);
                    if (lower.startsWith("-")) {
                        switch (lower) {
                            case "--include-back", "-b" -> includeBack = true;
                            case "--clear", "-c" -> clear = true;
                            case "--offline-uuid", "--offline" -> uuidMode = StpImporter.UuidMode.OFFLINE;
                            case "--raw-uuid", "--raw" -> uuidMode = StpImporter.UuidMode.RAW;
                            case "--auto-uuid", "--auto" -> uuidMode = StpImporter.UuidMode.AUTO;
                            default -> {
                                return null;
                            }
                        }
                    } else if (!fileSet) {
                        fileName = arg;
                        fileSet = true;
                    } else {
                        return null;
                    }
                }

                return new ImportOptions(fileName, includeBack, clear, uuidMode);
            }
        }
}
