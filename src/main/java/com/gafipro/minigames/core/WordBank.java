package com.gafipro.minigames.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Shared, safe word data for word-based minigames. */
public final class WordBank {
    private static final String RESOURCE="/data/gafi-minigames/words.txt";
    private static final List<String> WORDS=load();
    private WordBank(){}
    private static List<String> load(){
        try(var stream=WordBank.class.getResourceAsStream(RESOURCE)){
            if(stream==null)throw new IllegalStateException("Missing word bank resource: "+RESOURCE);
            List<String> out=new ArrayList<>();
            try(var reader=new BufferedReader(new InputStreamReader(stream,StandardCharsets.UTF_8))){
                String line; while((line=reader.readLine())!=null){line=line.trim().toUpperCase();if(!line.isEmpty()&&line.matches("[A-Z]{3,18}"))out.add(line);}
            }
            if(out.size()<32)throw new IllegalStateException("Word bank is too small: "+out.size());
            return Collections.unmodifiableList(out);
        }catch(Exception e){
            return List.of("MINECRAFT","REDSTONE","DIAMOND","CREEPER","VILLAGER","PORTAL","NETHER","ENDER");
        }
    }
    public static String random(Random random){return WORDS.get(random.nextInt(WORDS.size()));}
    public static List<String> all(){return WORDS;}
}
