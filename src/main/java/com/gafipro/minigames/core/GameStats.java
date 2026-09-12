package com.gafipro.minigames.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

/** Persistent, backwards-compatible structured statistics for mini-games. */
public final class GameStats {
    private static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE=FabricLoader.getInstance().getConfigDir().resolve("gafi-minigames.json");
    private static final Map<String,Integer> games=new HashMap<>(),wins=new HashMap<>(),losses=new HashMap<>(),draws=new HashMap<>(),best=new HashMap<>(),streak=new HashMap<>(),bestStreak=new HashMap<>(),highestLevel=new HashMap<>();
    private static final Map<String,Long> bestTime=new HashMap<>();
    private static final Map<String,Double> accuracy=new HashMap<>();
    private static boolean loaded;
    private GameStats(){}
    public static synchronized void load(){if(loaded)return;loaded=true;try{if(!Files.exists(FILE))return;JsonObject r=JsonParser.parseString(Files.readString(FILE,StandardCharsets.UTF_8)).getAsJsonObject();readInt(r,"games",games);readInt(r,"wins",wins);readInt(r,"losses",losses);readInt(r,"draws",draws);readInt(r,"best",best);readInt(r,"streak",streak);readInt(r,"bestStreak",bestStreak);readInt(r,"highestLevel",highestLevel);readLong(r,"bestTime",bestTime);readDouble(r,"accuracy",accuracy);}catch(Exception ignored){}}
    private static void readInt(JsonObject r,String k,Map<String,Integer>m){if(!r.has(k)||!r.get(k).isJsonObject())return;r.getAsJsonObject(k).entrySet().forEach(e->{try{m.put(e.getKey(),e.getValue().getAsInt());}catch(Exception ignored){}});}
    private static void readLong(JsonObject r,String k,Map<String,Long>m){if(!r.has(k)||!r.get(k).isJsonObject())return;r.getAsJsonObject(k).entrySet().forEach(e->{try{m.put(e.getKey(),e.getValue().getAsLong());}catch(Exception ignored){}});}
    private static void readDouble(JsonObject r,String k,Map<String,Double>m){if(!r.has(k)||!r.get(k).isJsonObject())return;r.getAsJsonObject(k).entrySet().forEach(e->{try{m.put(e.getKey(),e.getValue().getAsDouble());}catch(Exception ignored){}});}
    public static int games(String id){load();return games.getOrDefault(id,0);}public static int wins(String id){load();return wins.getOrDefault(id,0);}public static int losses(String id){load();return losses.getOrDefault(id,0);}public static int draws(String id){load();return draws.getOrDefault(id,0);}public static int best(String id){load();return best.getOrDefault(id,0);}public static int streak(String id){load();return streak.getOrDefault(id,0);}public static int bestStreak(String id){load();return bestStreak.getOrDefault(id,0);}public static int highestLevel(String id){load();return highestLevel.getOrDefault(id,0);}public static long bestTime(String id){load();return bestTime.getOrDefault(id,0L);}public static double accuracy(String id){load();return accuracy.getOrDefault(id,100.0);}public static double winRate(String id){int g=games(id);return g==0?0.0:wins(id)*100.0/g;}
    public static void record(String id,int score,boolean won){recordResult(id,score,won,false);}
    public static void recordResult(String id,int score,boolean won,boolean draw){recordResult(id,score,won,draw,0,0,100);}
    public static synchronized void recordResult(String id,int score,boolean won,boolean draw,long elapsedMillis,int level,double accuracyPercent){load();games.merge(id,1,Integer::sum);if(draw)draws.merge(id,1,Integer::sum);else if(won){wins.merge(id,1,Integer::sum);int s=streak.merge(id,1,Integer::sum);bestStreak.merge(id,s,Math::max);}else{losses.merge(id,1,Integer::sum);streak.put(id,0);}best.merge(id,Math.max(0,score),Math::max);if(elapsedMillis>0)bestTime.merge(id,elapsedMillis,(old,v)->old==0?v:Math.min(old,v));if(level>0)highestLevel.merge(id,level,Math::max);if(accuracyPercent>=0&&accuracyPercent<=100){double old=accuracy.getOrDefault(id,-1.0);accuracy.put(id,old<0?accuracyPercent:(old+accuracyPercent)/2.0);}save();}
    public static synchronized void reset(){games.clear();wins.clear();losses.clear();draws.clear();best.clear();streak.clear();bestStreak.clear();highestLevel.clear();bestTime.clear();accuracy.clear();save();}
    private static void save(){try{JsonObject r=new JsonObject();r.add("games",GSON.toJsonTree(games));r.add("wins",GSON.toJsonTree(wins));r.add("losses",GSON.toJsonTree(losses));r.add("draws",GSON.toJsonTree(draws));r.add("best",GSON.toJsonTree(best));r.add("streak",GSON.toJsonTree(streak));r.add("bestStreak",GSON.toJsonTree(bestStreak));r.add("highestLevel",GSON.toJsonTree(highestLevel));r.add("bestTime",GSON.toJsonTree(bestTime));r.add("accuracy",GSON.toJsonTree(accuracy));Files.createDirectories(FILE.getParent());Path tmp=FILE.resolveSibling(FILE.getFileName()+".tmp");Files.writeString(tmp,GSON.toJson(r),StandardCharsets.UTF_8);Files.move(tmp,FILE,StandardCopyOption.REPLACE_EXISTING);}catch(Exception ignored){}}
}
