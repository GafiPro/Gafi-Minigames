package com.gafipro.minigames.multiplayer;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class MultiplayerManager {
    public static final String PREFIX="[[GAFI_GAMES|";
    private static final Map<String,Invite> invites=new ConcurrentHashMap<>();
    private static final Map<String,Match> matches=new ConcurrentHashMap<>();
    private static boolean registered;
    private MultiplayerManager(){}
    public record Invite(String matchId,String gameId,String from){}
    public static final class Match {
        public final String id,gameId,host,opponent;public final boolean localHost;public final String[] board=new String[42];public int turn;public String remoteMove="";public boolean finished;public String result="";public boolean rematchLocal,rematchRemote;
        public Match(String id,String gameId,String host,String opponent,boolean localHost){this.id=id;this.gameId=gameId;this.host=host;this.opponent=opponent;this.localHost=localHost;this.turn=0;}
    }
    public static void init(){if(registered)return;registered=true;ClientReceiveMessageEvents.CHAT.register((message,signed,sender,params,receptionTimestamp)->handle(message,sender));ClientReceiveMessageEvents.ALLOW_CHAT.register((message,signed,sender,params,receptionTimestamp)->!message.getString().contains(PREFIX));}
    private static void handle(Text message,GameProfile sender){String s=message.getString();int p=s.indexOf(PREFIX);if(p<0)return;int end=s.indexOf("]]",p);if(end<0)return;String encoded=s.substring(p+PREFIX.length(),end);String body;try{body=new String(Base64.getUrlDecoder().decode(encoded),StandardCharsets.UTF_8);}catch(IllegalArgumentException e){return;}String[]a=body.split("\\|",-1);if(a.length==0)return;MinecraftClient mc=MinecraftClient.getInstance();switch(a[0]){
        case "INVITE"->{if(a.length>=4&&sender!=null){Invite inv=new Invite(a[1],a[2],sender.getName());invites.put(inv.matchId(),inv);if(mc.player!=null)mc.player.sendMessage(Text.literal("Gafi Minigames invite from "+inv.from()+" for "+inv.gameId()+". Open /games."),false);}}
        case "ACCEPT"->{if(a.length>=5){Match m=new Match(a[1],a[2],a[3],a[4],Objects.equals(playerName(),a[3]));matches.put(m.id,m);}}
        case "DECLINE"->{if(a.length>=2)invites.remove(a[1]);}
        case "MOVE"->{if(a.length>=3){Match m=matches.get(a[1]);if(m!=null)m.remoteMove=a[2];}}
        case "REMATCH"->{if(a.length>=3){Match m=matches.get(a[1]);if(m!=null)m.rematchRemote="YES".equals(a[2]);}}
        case "LEAVE"->{if(a.length>=2){Match m=matches.get(a[1]);if(m!=null){m.finished=true;m.result="Opponent left the match.";}}}
        default->{}}
    }
    public static void invite(String player,String gameId){String self=playerName();if(self==null||player==null||player.equalsIgnoreCase(self)||gameId==null)return;String id=UUID.randomUUID().toString().substring(0,12);send(player,"INVITE|"+id+"|"+gameId+"|"+self);if(MinecraftClient.getInstance().player!=null)MinecraftClient.getInstance().player.sendMessage(Text.literal("Invite sent to "+player+"."),false);}
    public static Set<Invite> pendingInvites(){return Set.copyOf(invites.values());}
    public static void accept(Invite inv){String self=playerName();if(self==null)return;invites.remove(inv.matchId());Match m=new Match(inv.matchId(),inv.gameId(),inv.from(),self,false);matches.put(m.id,m);send(inv.from(),"ACCEPT|"+inv.matchId()+"|"+inv.gameId()+"|"+inv.from()+"|"+self);}
    public static void decline(Invite inv){invites.remove(inv.matchId());send(inv.from(),"DECLINE|"+inv.matchId());}
    public static Match latestMatch(){return matches.values().stream().reduce((a,b)->b).orElse(null);}
    public static void sendMove(Match m,String payload){if(m!=null&&!m.finished)send(m.opponent,"MOVE|"+m.id+"|"+payload);}
    public static void requestRematch(Match m){if(m==null)return;m.rematchLocal=true;send(m.opponent,"REMATCH|"+m.id+"|YES");}
    public static void leave(Match m){if(m==null)return;send(m.opponent,"LEAVE|"+m.id);m.finished=true;matches.remove(m.id);}
    private static void send(String target,String body){MinecraftClient mc=MinecraftClient.getInstance();if(mc.getNetworkHandler()==null)return;try{String enc=Base64.getUrlEncoder().withoutPadding().encodeToString(body.getBytes(StandardCharsets.UTF_8));mc.getNetworkHandler().sendChatCommand("msg "+target+" "+PREFIX+enc+"]]");}catch(Exception e){if(mc.player!=null)mc.player.sendMessage(Text.literal("Unable to send a Gafi Minigames message. The server may not support /msg."),false);}}
    private static String playerName(){MinecraftClient mc=MinecraftClient.getInstance();return mc.player==null?null:mc.player.getGameProfile().getName();}
}
