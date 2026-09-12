package com.gafipro.minigames.multiplayer;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class MultiplayerManager {
    public static final String PREFIX="[[GAFI_GAMES|";
    private static final Map<String,Invite> invites=new ConcurrentHashMap<>();
    private static final Map<String,Match> matches=new ConcurrentHashMap<>();
    private static boolean registered;
    private MultiplayerManager(){}
    public record Invite(String matchId,String gameId,String from){ }
    public static final class Match {
        public final String id,gameId,host,opponent;public final boolean localHost;public final String[] board;public int turn;public String localMove="",remoteMove="";public boolean finished;public String result="";public boolean rematchLocal,rematchRemote;
        public Match(String id,String gameId,String host,String opponent,boolean localHost){this.id=id;this.gameId=gameId;this.host=host;this.opponent=opponent;this.localHost=localHost;this.board=new String[42];this.turn=0;}
        public String protocolState(){return Base64.getUrlEncoder().withoutPadding().encodeToString(String.join(",",Arrays.stream(board).map(v->v==null?"":v).toArray(String[]::new)).getBytes(java.nio.charset.StandardCharsets.UTF_8));}
    }
    public static void init(){if(registered)return;registered=true;ClientReceiveMessageEvents.CHAT.register((message,signed,sender,params,receptionTimestamp)->handle(message,sender==null?null:sender.getName()));ClientReceiveMessageEvents.ALLOW_CHAT.register((message,signed,sender,params,receptionTimestamp)->!message.getString().contains(PREFIX));}
    private static void handle(Text message,net.minecraft.util.Identifier ignored){ }
    private static void handle(Text message,com.mojang.authlib.GameProfile sender){handle(message,sender==null?null:sender.getName());}
    private static void handle(Text message,String sender){String s=message.getString();int p=s.indexOf(PREFIX);if(p<0)return;int end=s.indexOf("]]",p);if(end<0)return;String body=s.substring(p+PREFIX.length(),end);String[]a=body.split("\\|",-1);if(a.length<1)return;MinecraftClient mc=MinecraftClient.getInstance();switch(a[0]){
            case "INVITE"-> {if(a.length>=4){Invite inv=new Invite(a[1],a[2],a[3]);invites.put(inv.matchId(),inv);mc.execute(()->mc.player.sendMessage(Text.literal("Gafi Minigames invite from "+inv.from()+" — use /games to open invites."),false));}}
            case "ACCEPT"-> {if(a.length>=5){String id=a[1],game=a[2],from=a[3],to=a[4];Match m=new Match(id,game,from,to,Objects.equals(playerName(),from));matches.put(id,m);}}
            case "MOVE"-> {if(a.length>=3){Match m=matches.get(a[1]);if(m!=null){m.remoteMove=a[2];applyMove(m,a[2]);}}}
            case "REMATCH"-> {if(a.length>=3){Match m=matches.get(a[1]);if(m!=null){if("YES".equals(a[2]))m.rematchRemote=true;else m.rematchRemote=false;}}}
            case "LEAVE"-> {if(a.length>=2){Match m=matches.get(a[1]);if(m!=null){m.finished=true;m.result="Opponent left the match.";}}}
        }}
    private static void applyMove(Match m,String move){/* Game screens consume remoteMove and apply game-specific validation. */}
    public static void invite(String player,String gameId){String self=playerName();if(self==null||player==null||player.equalsIgnoreCase(self)||gameId==null)return;String id=UUID.randomUUID().toString().substring(0,12);invites.put(id,new Invite(id,gameId,self));send(player,"INVITE|"+id+"|"+gameId+"|"+self);MinecraftClient.getInstance().player.sendMessage(Text.literal("Invite sent to "+player+"."),false);}
    public static Set<Invite> pendingInvites(){return Set.copyOf(invites.values());}
    public static void accept(Invite inv){String self=playerName();if(self==null)return;invites.remove(inv.matchId());Match m=new Match(inv.matchId(),inv.gameId(),inv.from(),self,false);matches.put(m.id,m);send(inv.from(),"ACCEPT|"+inv.matchId()+"|"+inv.gameId()+"|"+inv.from()+"|"+self);}
    public static void decline(Invite inv){invites.remove(inv.matchId());send(inv.from(),"DECLINE|"+inv.matchId());}
    public static Match match(String id){return matches.get(id);}public static Match latestMatch(){return matches.values().stream().reduce((a,b)->b).orElse(null);}
    public static void sendMove(Match m,String payload){if(m==null||m.finished)return;send(m.opponent,"MOVE|"+m.id+"|"+payload);}
    public static void requestRematch(Match m){if(m==null)return;m.rematchLocal=true;send(m.opponent,"REMATCH|"+m.id+"|YES");}
    public static void leave(Match m){if(m==null)return;send(m.opponent,"LEAVE|"+m.id);m.finished=true;matches.remove(m.id);}
    private static void send(String target,String body){MinecraftClient mc=MinecraftClient.getInstance();if(mc.getNetworkHandler()==null)return;try{mc.getNetworkHandler().sendChatCommand("msg "+target+" "+PREFIX+body+"]]" );}catch(Exception e){if(mc.player!=null)mc.player.sendMessage(Text.literal("Unable to send multiplayer message. Your server may not support /msg."),false);}}
    private static String playerName(){MinecraftClient mc=MinecraftClient.getInstance();return mc.player==null?null:mc.player.getGameProfile().getName();}
}
