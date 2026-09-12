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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/** Persisted user preferences and launcher history. */
public final class MinigamesSettings {
    private static final Logger LOGGER = LoggerFactory.getLogger("GafiMinigames/Settings");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("gafi-minigames-settings.json");
    private static final Set<String> favorites = new HashSet<>();
    private static final LinkedHashSet<String> recent = new LinkedHashSet<>();
    public static boolean sound = true, animations = true, tips = true, confirmExit = true, inviteNotifications = true;
    private static boolean loaded;
    private MinigamesSettings() {}

    public static synchronized void load() {
        if (loaded) return;
        loaded = true;
        if (!Files.exists(FILE)) return;
        try {
            JsonObject r = JsonParser.parseString(Files.readString(FILE, StandardCharsets.UTF_8)).getAsJsonObject();
            readBoolean(r, "sound", value -> sound = value);
            readBoolean(r, "animations", value -> animations = value);
            readBoolean(r, "tips", value -> tips = value);
            readBoolean(r, "confirmExit", value -> confirmExit = value);
            readBoolean(r, "inviteNotifications", value -> inviteNotifications = value);
            favorites.clear(); recent.clear();
            if (r.has("favorites") && r.get("favorites").isJsonArray()) r.getAsJsonArray("favorites").forEach(v -> {
                if (v.isJsonPrimitive()) {
                    try { favorites.add(v.getAsString()); } catch (Exception e) { LOGGER.warn("Ignoring malformed favorite entry.", e); }
                }
            });
            if (r.has("recent") && r.get("recent").isJsonArray()) r.getAsJsonArray("recent").forEach(v -> {
                if (v.isJsonPrimitive()) {
                    try { recent.add(v.getAsString()); } catch (Exception e) { LOGGER.warn("Ignoring malformed recent entry.", e); }
                }
            });
            while (recent.size() > 8) recent.remove(recent.iterator().next());
        } catch (Exception e) {
            LOGGER.warn("Could not load settings from {}. Defaults were retained.", FILE, e);
        }
    }

    @FunctionalInterface private interface BooleanSetter { void set(boolean value); }
    private static void readBoolean(JsonObject root, String key, BooleanSetter setter) {
        if (!root.has(key)) return;
        try { setter.set(root.get(key).getAsBoolean()); }
        catch (Exception e) { LOGGER.warn("Ignoring malformed boolean setting '{}'.", key, e); }
    }

    public static synchronized boolean favorite(String id){load();return favorites.contains(id);}
    public static synchronized void toggleFavorite(String id){load();if(!favorites.add(id))favorites.remove(id);save();}
    public static synchronized void touchRecent(String id){load();recent.remove(id);recent.add(id);while(recent.size()>8)recent.remove(recent.iterator().next());save();}
    public static synchronized Set<String> recent(){load();return Set.copyOf(new LinkedHashSet<>(recent));}
    public static synchronized Set<String> favorites(){load();return Set.copyOf(new HashSet<>(favorites));}
    public static void resetStats(){GameStats.reset();}

    public static synchronized void save(){
        try {
            JsonObject r=new JsonObject();
            r.addProperty("sound",sound);r.addProperty("animations",animations);r.addProperty("tips",tips);r.addProperty("confirmExit",confirmExit);r.addProperty("inviteNotifications",inviteNotifications);
            r.add("favorites",GSON.toJsonTree(favorites));r.add("recent",GSON.toJsonTree(recent));
            Files.createDirectories(FILE.getParent());
            Path tmp=FILE.resolveSibling(FILE.getFileName()+".tmp");
            Files.writeString(tmp,GSON.toJson(r),StandardCharsets.UTF_8);
            try { Files.move(tmp,FILE, StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE); }
            catch(Exception atomicFailure) { Files.move(tmp,FILE,StandardCopyOption.REPLACE_EXISTING); }
        } catch(Exception e) {
            LOGGER.warn("Could not save settings to {}. Current in-memory preferences remain active.",FILE,e);
        }
    }
}
