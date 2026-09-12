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
    private static final Map<String,Integer> games=new HashMap<>(),wins=new HashMap<>(),losses=new HashMap<>(),draws=new HashMap<>(),best=new HashMap<>(),streak=new HashMap<>(),bestStreak=new HashMap<>();
    private static boolean loaded;
    private GameStats(){}
    public static void load(){if(loaded)return;loaded=true;try{if(!Files.exists(FILE))return;JsonObject r=JsonParser.parseString(Files.readString(FILE,StandardCharsets.UTF_8)).getAsJsonObject();read(r,"games",games);read(r,"wins",wins);read(r,"losses",losses);read(r,"draws",draws);read(r,"best",best);read(r,"streak",streak);read(r,"bestStreak",bestStreak);}catch(Exception ignored){}}
    private static void read(JsonObject r,String k,Map<String,Integer>m){if(!r.has(k)||!r.get(k).isJsonObject())return;r.getAsJsonObject(k).entrySet().forEach(e->{try{m.put(e.getKey(),e.getValue().getAsInt());}catch(Exception ignored){}});}
    public static int games(String id){load();return games.getOrDefault(id,0);}public static int wins(String id){load();return wins.getOrDefault(id,0);}public static int losses(String id){load();return losses.getOrDefault(id,0);}public static int draws(String id){load();return draws.getOrDefault(id,0);}public static int best(String id){load();return best.getOrDefault(id,0);}public static int streak(String id){load();return streak.getOrDefault(id,0);}public static int bestStreak(String id){load();return bestStreak.getOrDefault(id,0);}public static double winRate(String id){int g=games(id);return g==0?0.0:(wins(id)*100.0/g);}
    public static void record(String id,int score,boolean won){recordResult(id,score,won,false);}
    public static void recordResult(String id,int score,boolean won,boolean draw){load();games.merge(id,1,Integer::sum);if(draw)draws.merge(id,1,Integer::sum);else if(won){wins.merge(id,1,Integer::sum);int s=streak.merge(id,1,Integer::sum);bestStreak.merge(id,s,Math::max);}else{losses.merge(id,1,Integer::sum);streak.put(id,0);}best.merge(id,Math.max(0,score),Math::max);save();}
    public static void reset(){games.clear();wins.clear();losses.clear();draws.clear();best.clear();streak.clear();bestStreak.clear();save();}
    private static void save(){try{JsonObject r=new JsonObject();r.add("games",GSON.toJsonTree(games));r.add("wins",GSON.toJsonTree(wins));r.add("losses",GSON.toJsonTree(losses));r.add("draws",GSON.toJsonTree(draws));r.add("best",GSON.toJsonTree(best));r.add("streak",GSON.toJsonTree(streak));r.add("bestStreak",GSON.toJsonTree(bestStreak));Files.createDirectories(FILE.getParent());Path tmp=FILE.resolveSibling(FILE.getFileName()+".tmp");Files.writeString(tmp,GSON.toJson(r),StandardCharsets.UTF_8);Files.move(tmp,FILE,java.nio.file.StandardCopyOption.REPLACE_EXISTING);}catch(Exception ignored){}}
}
