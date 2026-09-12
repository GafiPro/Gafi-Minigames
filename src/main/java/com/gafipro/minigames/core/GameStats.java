package com.gafipro.minigames.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

/** Persistent, backwards-compatible structured statistics for mini-games. */
public final class GameStats {
    private static final Logger LOGGER = LoggerFactory.getLogger("GafiMinigames/Stats");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("gafi-minigames.json");
    private static final Map<String, Integer> games = new HashMap<>(), wins = new HashMap<>(), losses = new HashMap<>(), draws = new HashMap<>(), best = new HashMap<>(), streak = new HashMap<>(), bestStreak = new HashMap<>(), highestLevel = new HashMap<>();
    private static final Map<String, Long> bestTime = new HashMap<>();
    private static final Map<String, Double> accuracy = new HashMap<>();
    private static boolean loaded;

    private GameStats() {}

    public static synchronized void load() {
        if (loaded) return;
        loaded = true;
        if (!Files.exists(FILE)) return;
        try {
            JsonObject root = JsonParser.parseString(Files.readString(FILE, StandardCharsets.UTF_8)).getAsJsonObject();
            readInt(root, "games", games);
            readInt(root, "wins", wins);
            readInt(root, "losses", losses);
            readInt(root, "draws", draws);
            readInt(root, "best", best);
            readInt(root, "streak", streak);
            readInt(root, "bestStreak", bestStreak);
            readInt(root, "highestLevel", highestLevel);
            readLong(root, "bestTime", bestTime);
            readDouble(root, "accuracy", accuracy);
            sanitizeCounters();
        } catch (Exception e) {
            LOGGER.warn("Could not read statistics from {}. Existing in-memory values were preserved.", FILE, e);
        }
    }

    private static void readInt(JsonObject root, String key, Map<String, Integer> target) {
        if (!root.has(key) || !root.get(key).isJsonObject()) return;
        root.getAsJsonObject(key).entrySet().forEach(entry -> {
            try {
                int value = entry.getValue().getAsInt();
                if (value >= 0) target.put(entry.getKey(), value);
                else LOGGER.warn("Ignoring negative integer statistic '{}' in '{}'.", entry.getKey(), key);
            } catch (Exception e) { LOGGER.warn("Ignoring invalid integer statistic '{}' in '{}'.", entry.getKey(), key); }
        });
    }

    private static void readLong(JsonObject root, String key, Map<String, Long> target) {
        if (!root.has(key) || !root.get(key).isJsonObject()) return;
        root.getAsJsonObject(key).entrySet().forEach(entry -> {
            try {
                long value = entry.getValue().getAsLong();
                if (value >= 0) target.put(entry.getKey(), value);
                else LOGGER.warn("Ignoring negative long statistic '{}' in '{}'.", entry.getKey(), key);
            } catch (Exception e) { LOGGER.warn("Ignoring invalid long statistic '{}' in '{}'.", entry.getKey(), key); }
        });
    }

    private static void readDouble(JsonObject root, String key, Map<String, Double> target) {
        if (!root.has(key) || !root.get(key).isJsonObject()) return;
        root.getAsJsonObject(key).entrySet().forEach(entry -> {
            try {
                double value = entry.getValue().getAsDouble();
                if (Double.isFinite(value) && value >= 0.0 && value <= 100.0) target.put(entry.getKey(), value);
                else LOGGER.warn("Ignoring out-of-range accuracy statistic '{}' in '{}'.", entry.getKey(), key);
            } catch (Exception e) { LOGGER.warn("Ignoring invalid decimal statistic '{}' in '{}'.", entry.getKey(), key); }
        });
    }

    private static void sanitizeCounters() {
        for (String id : games.keySet()) {
            int total = Math.max(0, games.getOrDefault(id, 0));
            int win = Math.min(Math.max(0, wins.getOrDefault(id, 0)), total);
            int remaining = total - win;
            int loss = Math.min(Math.max(0, losses.getOrDefault(id, 0)), remaining);
            remaining -= loss;
            int draw = Math.min(Math.max(0, draws.getOrDefault(id, 0)), remaining);
            wins.put(id, win);
            losses.put(id, loss);
            draws.put(id, draw);
            streak.put(id, Math.min(Math.max(0, streak.getOrDefault(id, 0)), win));
            bestStreak.put(id, Math.max(0, bestStreak.getOrDefault(id, 0)));
            best.put(id, Math.max(0, best.getOrDefault(id, 0)));
            highestLevel.put(id, Math.max(0, highestLevel.getOrDefault(id, 0)));
        }
    }

    public static int games(String id) { load(); return games.getOrDefault(id, 0); }
    public static int wins(String id) { load(); return wins.getOrDefault(id, 0); }
    public static int losses(String id) { load(); return losses.getOrDefault(id, 0); }
    public static int draws(String id) { load(); return draws.getOrDefault(id, 0); }
    public static int best(String id) { load(); return best.getOrDefault(id, 0); }
    public static int streak(String id) { load(); return streak.getOrDefault(id, 0); }
    public static int bestStreak(String id) { load(); return bestStreak.getOrDefault(id, 0); }
    public static int highestLevel(String id) { load(); return highestLevel.getOrDefault(id, 0); }
    public static long bestTime(String id) { load(); return bestTime.getOrDefault(id, 0L); }
    public static double accuracy(String id) { load(); return accuracy.getOrDefault(id, 100.0); }
    public static double winRate(String id) { int g = games(id); return g == 0 ? 0.0 : wins(id) * 100.0 / g; }

    public static void record(String id, int score, boolean won) { recordResult(id, score, won, false); }
    public static void recordResult(String id, int score, boolean won, boolean draw) { recordResult(id, score, won, draw, 0, 0, 100); }

    public static synchronized void recordResult(String id, int score, boolean won, boolean draw, long elapsedMillis, int level, double accuracyPercent) {
        load();
        games.merge(id, 1, Integer::sum);
        if (draw) draws.merge(id, 1, Integer::sum);
        else if (won) {
            wins.merge(id, 1, Integer::sum);
            int currentStreak = streak.merge(id, 1, Integer::sum);
            bestStreak.merge(id, currentStreak, Math::max);
        } else {
            losses.merge(id, 1, Integer::sum);
            streak.put(id, 0);
        }
        best.merge(id, Math.max(0, score), Math::max);
        if (elapsedMillis > 0) bestTime.merge(id, elapsedMillis, (old, value) -> old == 0 ? value : Math.min(old, value));
        if (level > 0) highestLevel.merge(id, level, Math::max);
        if (Double.isFinite(accuracyPercent) && accuracyPercent >= 0 && accuracyPercent <= 100) {
            double old = accuracy.getOrDefault(id, -1.0);
            accuracy.put(id, old < 0 ? accuracyPercent : (old + accuracyPercent) / 2.0);
        }
        save();
    }

    public static synchronized void reset() {
        games.clear(); wins.clear(); losses.clear(); draws.clear(); best.clear(); streak.clear(); bestStreak.clear(); highestLevel.clear(); bestTime.clear(); accuracy.clear(); save();
    }

    private static void save() {
        try {
            JsonObject root = new JsonObject();
            root.add("games", GSON.toJsonTree(games));
            root.add("wins", GSON.toJsonTree(wins));
            root.add("losses", GSON.toJsonTree(losses));
            root.add("draws", GSON.toJsonTree(draws));
            root.add("best", GSON.toJsonTree(best));
            root.add("streak", GSON.toJsonTree(streak));
            root.add("bestStreak", GSON.toJsonTree(bestStreak));
            root.add("highestLevel", GSON.toJsonTree(highestLevel));
            root.add("bestTime", GSON.toJsonTree(bestTime));
            root.add("accuracy", GSON.toJsonTree(accuracy));
            Files.createDirectories(FILE.getParent());
            Path tmp = FILE.resolveSibling(FILE.getFileName() + ".tmp");
            Files.writeString(tmp, GSON.toJson(root), StandardCharsets.UTF_8);
            try {
                Files.move(tmp, FILE, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (Exception atomicFailure) {
                Files.move(tmp, FILE, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            LOGGER.warn("Could not save statistics to {}. Current in-memory statistics remain available.", FILE, e);
        }
    }
}
