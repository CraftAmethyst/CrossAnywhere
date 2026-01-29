package io.github.qianmo0721.crossAnywhere.repository;

import io.github.qianmo0721.crossAnywhere.model.Waypoint;
import io.github.qianmo0721.crossAnywhere.util.NameValidator;

import java.nio.file.Path;
import java.util.*;

public final class WaypointRepository {
    private final Path personalFile;
    private final Path globalFile;
    private final Map<String, Map<String, Waypoint>> personal = new HashMap<>();
    private final Map<String, Waypoint> global = new HashMap<>();
    private final WaypointSerializer serializer = new WaypointSerializer();
    private boolean dirty;

    public WaypointRepository(Path dataFolder) {
        this.personalFile = dataFolder.resolve("personal_waypoints.json");
        this.globalFile = dataFolder.resolve("global_waypoints.json");
    }

    public synchronized void load() {
        loadPersonal();
        loadGlobal();
        dirty = false;
    }

    private void loadPersonal() {
        personal.clear();
        personal.putAll(serializer.loadPersonal(personalFile));
    }

    private void loadGlobal() {
        global.clear();
        global.putAll(serializer.loadGlobal(globalFile));
    }

    public synchronized void saveIfDirty() {
        if (!dirty) {
            return;
        }
        save();
    }

    public synchronized void save() {
        try {
            serializer.savePersonal(personalFile, personal);
            serializer.saveGlobal(globalFile, global);
            dirty = false;
        } catch (Exception ignored) {
        }
    }

    public synchronized void replaceAll(Map<String, Map<String, Waypoint>> personalData,
                                        Map<String, Waypoint> globalData) {
        personal.clear();
        global.clear();
        if (personalData != null) {
            personal.putAll(personalData);
        }
        if (globalData != null) {
            global.putAll(globalData);
        }
        dirty = true;
    }

    public synchronized Waypoint getPersonal(UUID uuid, String name) {
        Map<String, Waypoint> map = personal.get(uuid.toString());
        if (map == null) {
            return null;
        }
        return map.get(NameValidator.key(name));
    }

    public synchronized Waypoint getGlobal(String name) {
        return global.get(NameValidator.key(name));
    }

    public synchronized boolean setPersonal(UUID uuid, Waypoint waypoint) {
        String key = NameValidator.key(waypoint.getName());
        Map<String, Waypoint> map = personal.computeIfAbsent(uuid.toString(), ignored -> new HashMap<>());
        boolean existed = map.containsKey(key);
        map.put(key, waypoint);
        dirty = true;
        return existed;
    }

    public synchronized boolean setGlobal(Waypoint waypoint) {
        String key = NameValidator.key(waypoint.getName());
        boolean existed = global.containsKey(key);
        global.put(key, waypoint);
        dirty = true;
        return existed;
    }

    public synchronized boolean deletePersonal(UUID uuid, String name) {
        Map<String, Waypoint> map = personal.get(uuid.toString());
        if (map == null) {
            return false;
        }
        Waypoint removed = map.remove(NameValidator.key(name));
        if (removed != null) {
            dirty = true;
            return true;
        }
        return false;
    }

    public synchronized boolean deleteGlobal(String name) {
        Waypoint removed = global.remove(NameValidator.key(name));
        if (removed != null) {
            dirty = true;
            return true;
        }
        return false;
    }

    public synchronized List<Waypoint> listPersonal(UUID uuid) {
        Map<String, Waypoint> map = personal.get(uuid.toString());
        if (map == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(map.values());
    }

    public synchronized List<Waypoint> listGlobal() {
        return new ArrayList<>(global.values());
    }

    public synchronized int countPersonal(UUID uuid) {
        Map<String, Waypoint> map = personal.get(uuid.toString());
        return map == null ? 0 : map.size();
    }

    public synchronized int countGlobal() {
        return global.size();
    }
}
