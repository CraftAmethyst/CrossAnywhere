package io.github.qianmo0721.crossAnywhere.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class TpaAllowlistRepository {
    private final Path file;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Map<String, Set<String>> allowlist = new HashMap<>();
    private boolean dirty;

    public TpaAllowlistRepository(Path dataFolder) {
        this.file = dataFolder.resolve("tpa_allowlist.json");
    }

    public synchronized void load() {
        allowlist.clear();
        if (!Files.exists(file)) {
            dirty = false;
            return;
        }
        try {
            String json = Files.readString(file, StandardCharsets.UTF_8);
            Type type = new TypeToken<Map<String, Set<String>>>() {
            }.getType();
            Map<String, Set<String>> loaded = gson.fromJson(json, type);
            if (loaded != null) {
                for (Map.Entry<String, Set<String>> entry : loaded.entrySet()) {
                    if (entry.getValue() == null) {
                        allowlist.put(entry.getKey(), new HashSet<>());
                    } else {
                        allowlist.put(entry.getKey(), new HashSet<>(entry.getValue()));
                    }
                }
            }
            dirty = false;
        } catch (IOException ignored) {
            dirty = false;
        }
    }

    public synchronized void saveIfDirty() {
        if (!dirty) {
            return;
        }
        save();
    }

    public synchronized void save() {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, gson.toJson(allowlist), StandardCharsets.UTF_8);
            dirty = false;
        } catch (IOException ignored) {
        }
    }

    public synchronized boolean isAllowed(UUID target, UUID sender) {
        Set<String> list = allowlist.get(target.toString());
        return list != null && list.contains(sender.toString());
    }

    public synchronized boolean add(UUID target, UUID allowed) {
        String targetKey = target.toString();
        Set<String> list = allowlist.computeIfAbsent(targetKey, ignored -> new HashSet<>());
        boolean added = list.add(allowed.toString());
        if (added) {
            dirty = true;
        }
        return added;
    }

    public synchronized boolean remove(UUID target, UUID allowed) {
        String targetKey = target.toString();
        Set<String> list = allowlist.get(targetKey);
        if (list == null) {
            return false;
        }
        boolean removed = list.remove(allowed.toString());
        if (removed) {
            if (list.isEmpty()) {
                allowlist.remove(targetKey);
            }
            dirty = true;
        }
        return removed;
    }

    public synchronized List<UUID> list(UUID target) {
        Set<String> list = allowlist.get(target.toString());
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        List<UUID> result = new ArrayList<>();
        for (String value : list) {
            try {
                result.add(UUID.fromString(value));
            } catch (Exception ignored) {
            }
        }
        return result;
    }
}
