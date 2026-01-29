package io.github.qianmo0721.crossAnywhere.importer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.qianmo0721.crossAnywhere.model.Waypoint;
import io.github.qianmo0721.crossAnywhere.util.NameValidator;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class StpImporter {
    private static final Map<String, String> VANILLA_DIMENSION_WORLD = Map.of(
            "minecraft:overworld", "world",
            "minecraft:the_nether", "world_nether",
            "minecraft:the_end", "world_the_end"
    );

    private StpImporter() {
    }

    public static Result load(Path file,
                              UuidMode uuidMode,
                              boolean includeBack,
                              Map<String, String> uuidMap,
                              Map<String, String> worldMap) throws IOException {
        String raw = Files.readString(file, StandardCharsets.UTF_8);
        JsonObject root = JsonParser.parseString(raw).getAsJsonObject();
        JsonObject personalRoot = asObject(root.get("personal_waypoints"));
        JsonObject globalRoot = asObject(root.get("global_waypoints"));
        JsonObject dimRoot = asObject(root.get("dimension_str2sid"));

        Map<Integer, String> sidToDim = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : dimRoot.entrySet()) {
            JsonElement value = entry.getValue();
            if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()) {
                sidToDim.put(value.getAsInt(), entry.getKey());
            }
        }

        Map<String, String> uuidLookup = normalizeMap(uuidMap);
        Map<String, String> worldLookup = normalizeMap(worldMap);

        List<String> warnings = new java.util.ArrayList<>();
        Set<String> dimensions = new HashSet<>();

        Map<String, Map<String, Waypoint>> personal = new HashMap<>();
        Map<String, Waypoint> global = new HashMap<>();

        int personalPlayers = 0;
        int personalWaypoints = 0;
        int globalWaypoints = 0;
        int skipped = 0;

        long now = java.time.Instant.now().getEpochSecond();

        for (Map.Entry<String, JsonElement> entry : personalRoot.entrySet()) {
            String playerKey = entry.getKey();
            JsonObject waypoints = asObject(entry.getValue());
            if (waypoints.isEmpty()) {
                continue;
            }
            UUID owner = resolveUuid(playerKey, uuidMode, uuidLookup, warnings);
            if (owner == null) {
                warnings.add("Invalid player key, skipped: " + playerKey);
                skipped++;
                continue;
            }
            String ownerKey = owner.toString();
            Map<String, Waypoint> playerMap = new HashMap<>();

            for (Map.Entry<String, JsonElement> wpEntry : waypoints.entrySet()) {
                String name = wpEntry.getKey();
                if (!includeBack && "__back__".equalsIgnoreCase(name)) {
                    skipped++;
                    continue;
                }
                JsonArray loc = asArray(wpEntry.getValue());
                if (loc.size() < 4) {
                    warnings.add("Invalid waypoint data for " + playerKey + ":" + name);
                    skipped++;
                    continue;
                }
                Waypoint waypoint = buildWaypoint(name, loc, owner, now, sidToDim, worldLookup, warnings, dimensions);
                if (waypoint == null) {
                    skipped++;
                    continue;
                }
                playerMap.put(NameValidator.key(name), waypoint);
                personalWaypoints++;
            }

            if (!playerMap.isEmpty()) {
                personal.put(ownerKey, playerMap);
                personalPlayers++;
            }
        }

        for (Map.Entry<String, JsonElement> entry : globalRoot.entrySet()) {
            String name = entry.getKey();
            JsonArray loc = asArray(entry.getValue());
            if (loc.size() < 4) {
                warnings.add("Invalid global waypoint data: " + name);
                skipped++;
                continue;
            }
            Waypoint waypoint = buildWaypoint(name, loc, null, now, sidToDim, worldLookup, warnings, dimensions);
            if (waypoint == null) {
                skipped++;
                continue;
            }
            global.put(NameValidator.key(name), waypoint);
            globalWaypoints++;
        }

        return new Result(personal, global, personalPlayers, personalWaypoints,
                globalWaypoints, skipped, warnings, dimensions);
    }

    public static Map<String, String> loadStringMap(Path file, List<String> warnings) {
        if (file == null || !Files.exists(file)) {
            return Map.of();
        }
        try {
            String raw = Files.readString(file, StandardCharsets.UTF_8);
            JsonObject root = JsonParser.parseString(raw).getAsJsonObject();
            Map<String, String> map = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                JsonElement value = entry.getValue();
                if (value.isJsonPrimitive()) {
                    map.put(entry.getKey(), value.getAsString());
                }
            }
            return map;
        } catch (Exception ex) {
            if (warnings != null) {
                warnings.add("Failed to load map file: " + file.getFileName());
            }
            return Map.of();
        }
    }

    private static JsonObject asObject(JsonElement element) {
        if (element != null && element.isJsonObject()) {
            return element.getAsJsonObject();
        }
        return new JsonObject();
    }

    private static JsonArray asArray(JsonElement element) {
        if (element != null && element.isJsonArray()) {
            return element.getAsJsonArray();
        }
        return new JsonArray();
    }

    private static Map<String, String> normalizeMap(Map<String, String> input) {
        Map<String, String> normalized = new HashMap<>();
        if (input == null) {
            return normalized;
        }
        for (Map.Entry<String, String> entry : input.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            normalized.put(entry.getKey(), entry.getValue());
            normalized.put(entry.getKey().toLowerCase(Locale.ROOT), entry.getValue());
        }
        return normalized;
    }

    private static UUID resolveUuid(String playerKey,
                                    UuidMode uuidMode,
                                    Map<String, String> uuidMap,
                                    List<String> warnings) {
        String lookup = playerKey == null ? "" : playerKey.trim();
        if (lookup.isEmpty()) {
            return null;
        }
        String mapped = uuidMap.get(lookup);
        if (mapped == null) {
            mapped = uuidMap.get(lookup.toLowerCase(Locale.ROOT));
        }
        if (mapped != null) {
            UUID parsed = parseUuid(mapped);
            if (parsed != null) {
                return parsed;
            }
            warnings.add("Invalid UUID mapping for player: " + lookup);
        }

        return switch (uuidMode) {
            case RAW -> parseUuid(lookup);
            case OFFLINE -> offlineUuid(lookup);
            case AUTO -> {
                UUID parsed = parseUuid(lookup);
                yield parsed != null ? parsed : offlineUuid(lookup);
            }
            case BUKKIT -> {
                OfflinePlayer player = Bukkit.getOfflinePlayer(lookup);
                yield player.getUniqueId();
            }
        };
    }

    private static UUID parseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (Exception ex) {
            return null;
        }
    }

    private static UUID offlineUuid(String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
    }

    private static Waypoint buildWaypoint(String name,
                                          JsonArray loc,
                                          UUID owner,
                                          long timestamp,
                                          Map<Integer, String> sidToDim,
                                          Map<String, String> worldLookup,
                                          List<String> warnings,
                                          Set<String> dimensions) {
        double x;
        double y;
        double z;
        int sid;
        try {
            x = loc.get(0).getAsDouble();
            y = loc.get(1).getAsDouble();
            z = loc.get(2).getAsDouble();
            sid = loc.get(3).getAsInt();
        } catch (Exception ex) {
            warnings.add("Invalid location data for waypoint: " + name);
            return null;
        }

        float yaw = 0.0f;
        float pitch = 0.0f;
        if (loc.size() >= 6) {
            try {
                yaw = loc.get(4).getAsFloat();
                pitch = loc.get(5).getAsFloat();
            } catch (Exception ex) {
                warnings.add("Invalid yaw/pitch for waypoint: " + name);
            }
        }

        String dim = sidToDim.get(sid);
        if (dim == null) {
            warnings.add("Missing dimension mapping for sid=" + sid + ", waypoint=" + name);
            dim = String.valueOf(sid);
        } else {
            dimensions.add(dim);
        }

        String world = resolveWorld(dim, worldLookup);
        return new Waypoint(name, world, x, y, z, yaw, pitch, "", owner, timestamp, timestamp);
    }

    private static String resolveWorld(String dim, Map<String, String> worldLookup) {
        String mapped = worldLookup.get(dim);
        if (mapped == null) {
            mapped = worldLookup.get(dim.toLowerCase(Locale.ROOT));
        }
        if (mapped != null) {
            return mapped;
        }
        String vanilla = VANILLA_DIMENSION_WORLD.get(dim);
        if (vanilla != null) {
            return vanilla;
        }
        return dim;
    }

    public enum UuidMode {
        BUKKIT,
        OFFLINE,
        RAW,
        AUTO
    }

    public record Result(
            Map<String, Map<String, Waypoint>> personal,
            Map<String, Waypoint> global,
            int personalPlayers,
            int personalWaypoints,
            int globalWaypoints,
            int skipped,
            List<String> warnings,
            Set<String> dimensions
    ) {
    }
}
