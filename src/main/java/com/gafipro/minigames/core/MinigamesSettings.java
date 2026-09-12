package com.gafipro.minigames.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public final class MinigamesSettings {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("gafi-minigames-settings.json");
    private static final Set<String> favorites = new HashSet<>();
    private static final Set<String> recent = new java.util.LinkedHashSet<>();
    public static boolean sound = true, animations = true, tips = true, confirmExit = true, inviteNotifications = true;
    private static boolean loaded;
    private MinigamesSettings() {}
    public static void load(){if(loaded)return;loaded=true;try{if(!Files.exists(FILE))return;JsonObject r=JsonParser.parseString(Files.readString(FILE)).getAsJsonObject();sound=!r.has("sound")||r.get("sound").getAsBoolean();animations=!r.has("animations")||r.get("animations").getAsBoolean();tips=!r.has("tips")||r.get("tips").getAsBoolean();confirmExit=!r.has("confirmExit")||r.get("confirmExit").getAsBoolean();inviteNotifications=!r.has("inviteNotifications")||r.get("inviteNotifications").getAsBoolean();if(r.has("favorites"))r.getAsJsonArray("favorites").forEach(v->favorites.add(v.getAsString()));if(r.has("recent"))r.getAsJsonArray("recent").forEach(v->recent.add(v.getAsString()));}catch(Exception ignored){}}
    public static boolean favorite(String id){load();return favorites.contains(id);}
    public static void toggleFavorite(String id){load();if(!favorites.add(id))favorites.remove(id);save();}
    public static void touchRecent(String id){load();recent.remove(id);recent.add(id);while(recent.size()>8)recent.remove(recent.iterator().next());save();}
    public static Set<String> recent(){load();return Set.copyOf(recent);}
    public static Set<String> favorites(){load();return Set.copyOf(favorites);}
    public static void resetStats(){GameStats.reset();}
    public static void save(){try{JsonObject r=new JsonObject();r.addProperty("sound",sound);r.addProperty("animations",animations);r.addProperty("tips",tips);r.addProperty("confirmExit",confirmExit);r.addProperty("inviteNotifications",inviteNotifications);r.add("favorites",GSON.toJsonTree(favorites));r.add("recent",GSON.toJsonTree(recent));Files.createDirectories(FILE.getParent());Files.writeString(FILE,GSON.toJson(r),StandardCharsets.UTF_8);}catch(Exception ignored){}}
}
