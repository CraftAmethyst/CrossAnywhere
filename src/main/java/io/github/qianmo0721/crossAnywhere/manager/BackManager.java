package io.github.qianmo0721.crossAnywhere.manager;

import org.bukkit.Location;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class BackManager {
    private final Map<UUID, Location> backLocations = new ConcurrentHashMap<>();

    public void setBack(UUID uuid, Location location) {
        if (location == null) {
            return;
        }
        backLocations.put(uuid, location.clone());
    }

    public Location getBack(UUID uuid) {
        Location location = backLocations.get(uuid);
        return location == null ? null : location.clone();
    }

    public void clear(UUID uuid) {
        backLocations.remove(uuid);
    }
}
