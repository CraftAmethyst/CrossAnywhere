package io.github.qianmo0721.crossAnywhere.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.github.qianmo0721.crossAnywhere.model.Waypoint;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class WaypointSerializer {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public Map<String, Map<String, Waypoint>> loadPersonal(Path file) {
        if (!Files.exists(file)) {
            return new HashMap<>();
        }
        try {
            String json = Files.readString(file, StandardCharsets.UTF_8);
            Type type = new TypeToken<Map<String, Map<String, Waypoint>>>() {}.getType();
            Map<String, Map<String, Waypoint>> loaded = gson.fromJson(json, type);
            return loaded == null ? new HashMap<>() : loaded;
        } catch (IOException ex) {
            return new HashMap<>();
        }
    }

    public Map<String, Waypoint> loadGlobal(Path file) {
        if (!Files.exists(file)) {
            return new HashMap<>();
        }
        try {
            String json = Files.readString(file, StandardCharsets.UTF_8);
            Type type = new TypeToken<Map<String, Waypoint>>() {}.getType();
            Map<String, Waypoint> loaded = gson.fromJson(json, type);
            return loaded == null ? new HashMap<>() : loaded;
        } catch (IOException ex) {
            return new HashMap<>();
        }
    }

    public void savePersonal(Path file, Map<String, Map<String, Waypoint>> data) throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, gson.toJson(data), StandardCharsets.UTF_8);
    }

    public void saveGlobal(Path file, Map<String, Waypoint> data) throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, gson.toJson(data), StandardCharsets.UTF_8);
    }
}
