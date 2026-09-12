package com.gafipro.minigames.multiplayer;

import com.gafipro.minigames.gui.MultiplayerGameScreen;
import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Client-only private-message transport. It validates membership, action format and sequencing, but is not authoritative. */
public final class MultiplayerManager {
    public static final String PREFIX="[[GAFI_GAMES|";
    private static final long INVITE_TTL=60_000;
    private static final Map<String,Invite> invites=new ConcurrentHashMap<>();
    private static final Map<String,Match> matches=new ConcurrentHashMap<>();
    private static boolean registered;

    private MultiplayerManager(){}
    public record Invite(String matchId,String gameId,String from,long createdAt){}
    public static final class Match {
        public final String id,gameId,host,opponent; public final boolean localHost;
        public final String[] board=new String[42]; public int turn; public String remoteMove=""; public boolean finished; public String result=""; public boolean rematchLocal,rematchRemote;
        private int nextLocalAction=0,nextRemoteAction=0;
        public Match(String id,String gameId,String host,String opponent,boolean localHost){this.id=id;this.gameId=gameId;this.host=host;this.opponent=opponent;this.localHost=localHost;}
    }

    public static void init(){
        if(registered)return;registered=true;
        ClientReceiveMessageEvents.CHAT.register((message,signed,sender,params,receptionTimestamp)->handle(message,sender));
        ClientReceiveMessageEvents.ALLOW_CHAT.register((message,signed,sender,params,receptionTimestamp)->!message.getString().contains(PREFIX));
    }

    private static void handle(Text message,GameProfile sender){
        String s=message.getString();int p=s.indexOf(PREFIX);if(p<0)return;int end=s.indexOf("]]",p);if(end<0)return;
        String encoded=s.substring(p+PREFIX.length(),end),body;try{body=new String(Base64.getUrlDecoder().decode(encoded),StandardCharsets.UTF_8);}catch(IllegalArgumentException e){return;}
        String[] a=body.split("\\|",-1);if(a.length==0)return;MinecraftClient mc=MinecraftClient.getInstance();String senderName=profileName(sender);
        switch(a[0]){
            case "INVITE"->{if(a.length==3&&senderName!=null&&!a[1].isBlank()&&!a[2].isBlank())invites.putIfAbsent(a[1],new Invite(a[1],a[2],senderName,System.currentTimeMillis()));}
            case "ACCEPT"->{if(a.length==5&&senderName!=null){Match m=new Match(a[1],a[2],a[3],a[4],Objects.equals(playerName(),a[3]));if(senderName.equalsIgnoreCase(m.opponent)&&validGameId(m.gameId)){matches.putIfAbsent(m.id,m);mc.execute(()->mc.setScreen(new MultiplayerGameScreen(null,m)));}}}
            case "DECLINE"->{if(a.length==2)invites.remove(a[1]);}
            case "MOVE"->{if(a.length==4&&senderName!=null){Match m=matches.get(a[1]);if(m!=null&&!m.finished&&senderName.equalsIgnoreCase(m.opponent)){try{int seq=Integer.parseInt(a[2]);if(seq==m.nextRemoteAction&&validMove(m.gameId,a[3])){m.nextRemoteAction++;m.remoteMove=a[3];}}catch(NumberFormatException ignored){}}}}
            case "REMATCH"->{if(a.length==3&&senderName!=null){Match m=matches.get(a[1]);if(m!=null&&!m.finished&&senderName.equalsIgnoreCase(m.opponent)&&"YES".equals(a[2])){m.rematchRemote=true;tryStartRematch(m);}}}
            case "LEAVE"->{if(a.length==2&&senderName!=null){Match m=matches.get(a[1]);if(m!=null&&senderName.equalsIgnoreCase(m.opponent)){m.finished=true;m.result="Opponent left the match.";}}}
            default->{}
        }
    }

    private static boolean validGameId(String id){return id.equals("tic_tac_toe")||id.equals("connect_four")||id.equals("rock_paper_scissors");}
    private static boolean validMove(String game,String move){
        if(game.equals("tic_tac_toe")&&move.startsWith("T:"))try{return Integer.parseInt(move.substring(2))>=0&&Integer.parseInt(move.substring(2))<9;}catch(NumberFormatException e){return false;}
        if(game.equals("connect_four")&&move.startsWith("C:"))try{return Integer.parseInt(move.substring(2))>=0&&Integer.parseInt(move.substring(2))<7;}catch(NumberFormatException e){return false;}
        if(game.equals("rock_paper_scissors")&&move.startsWith("R:"))try{return Integer.parseInt(move.substring(2))>=0&&Integer.parseInt(move.substring(2))<3;}catch(NumberFormatException e){return false;}
        return false;
    }

    public static void invite(String player,String gameId){String self=playerName();if(self==null||player==null||player.equalsIgnoreCase(self)||!validGameId(gameId))return;if(matches.values().stream().anyMatch(m->!m.finished&&(m.opponent.equalsIgnoreCase(player)||m.host.equalsIgnoreCase(player))))return;String id=UUID.randomUUID().toString().substring(0,12);send(player,"INVITE|"+id+"|"+gameId);}
    public static Set<Invite> pendingInvites(){long now=System.currentTimeMillis();invites.values().removeIf(i->now-i.createdAt()>INVITE_TTL);return Set.copyOf(invites.values());}
    public static void accept(Invite inv){String self=playerName();if(self==null||inv==null||!validGameId(inv.gameId()))return;invites.remove(inv.matchId());Match m=new Match(inv.matchId(),inv.gameId(),inv.from(),self,false);matches.put(m.id,m);send(inv.from(),"ACCEPT|"+m.id+"|"+m.gameId+"|"+m.host+"|"+m.opponent);MinecraftClient.getInstance().setScreen(new MultiplayerGameScreen(null,m));}
    public static void decline(Invite inv){if(inv==null)return;invites.remove(inv.matchId());send(inv.from(),"DECLINE|"+inv.matchId());}
    public static Match latestMatch(){return matches.values().stream().reduce((a,b)->b).orElse(null);}
    public static void sendMove(Match m,String payload){if(m!=null&&!m.finished&&validMove(m.gameId,payload)){int seq=m.nextLocalAction++;send(m.opponent,"MOVE|"+m.id+"|"+seq+"|"+payload);}}
    public static void requestRematch(Match m){if(m==null)return;m.rematchLocal=true;send(m.opponent,"REMATCH|"+m.id+"|YES");tryStartRematch(m);}
    private static void tryStartRematch(Match m){if(m.rematchLocal&&m.rematchRemote){m.finished=false;m.result="";m.remoteMove="";m.rematchLocal=m.rematchRemote=false;m.nextLocalAction=m.nextRemoteAction=0;m.turn=0;MinecraftClient.getInstance().setScreen(new MultiplayerGameScreen(null,m));}}
    public static void leave(Match m){if(m==null)return;send(m.opponent,"LEAVE|"+m.id);m.finished=true;matches.remove(m.id);}
    private static void send(String target,String body){MinecraftClient mc=MinecraftClient.getInstance();if(mc.getNetworkHandler()==null)return;try{String enc=Base64.getUrlEncoder().withoutPadding().encodeToString(body.getBytes(StandardCharsets.UTF_8));mc.getNetworkHandler().sendChatCommand("msg "+target+" "+PREFIX+enc+"]]");}catch(Exception e){if(mc.player!=null)mc.player.sendMessage(Text.literal("Unable to send a Gafi Minigames message. The server may not support /msg."),false);}}
    private static String profileName(GameProfile p){return p==null?null:p.name();}
    private static String playerName(){MinecraftClient mc=MinecraftClient.getInstance();return mc.player==null?null:profileName(mc.player.getGameProfile());}
}
