package com.gafipro.minigames.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class GameStats {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("gafi-minigames.json");
    private static final Map<String, Integer> games = new HashMap<>();
    private static final Map<String, Integer> wins = new HashMap<>();
    private static final Map<String, Integer> best = new HashMap<>();
    private static boolean loaded;

    private GameStats() {}

    public static void load() {
        if (loaded) return;
        loaded = true;
        try {
            if (!Files.exists(FILE)) return;
            JsonObject root = JsonParser.parseString(Files.readString(FILE, StandardCharsets.UTF_8)).getAsJsonObject();
            readMap(root, "games", games);
            readMap(root, "wins", wins);
            readMap(root, "best", best);
        } catch (Exception ignored) { }
    }

    public static int games(String id) { load(); return games.getOrDefault(id, 0); }
    public static int wins(String id) { load(); return wins.getOrDefault(id, 0); }
    public static int best(String id) { load(); return best.getOrDefault(id, 0); }

    public static void record(String id, int score, boolean won) {
        load();
        games.merge(id, 1, Integer::sum);
        if (won) wins.merge(id, 1, Integer::sum);
        if (score > best(id)) best.put(id, score);
        save();
    }

    private static void readMap(JsonObject root, String name, Map<String, Integer> out) {
        if (!root.has(name) || !root.get(name).isJsonObject()) return;
        root.getAsJsonObject(name).entrySet().forEach(e -> {
            try { out.put(e.getKey(), e.getValue().getAsInt()); } catch (Exception ignored) { }
        });
    }

    private static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            JsonObject root = new JsonObject();
            root.add("games", GSON.toJsonTree(games));
            root.add("wins", GSON.toJsonTree(wins));
            root.add("best", GSON.toJsonTree(best));
            Files.writeString(FILE, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (Exception ignored) { }
    }
}
