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
    private static final int MAX_ENCODED_PAYLOAD = 4096;
    private static final int MAX_DECODED_PAYLOAD = 4096;
    private static final long INVITE_TTL=60_000;
    private static final Map<String,Invite> invites=new ConcurrentHashMap<>();
    private static final Map<String,OutgoingInvite> outgoingInvites=new ConcurrentHashMap<>();
    private static final Map<String,Match> matches=new ConcurrentHashMap<>();
    private static boolean registered;
    private MultiplayerManager(){}
    public record Invite(String matchId,String gameId,String from,long createdAt){}
    private record OutgoingInvite(String matchId,String gameId,String target,long createdAt){}
    public static final class Match {
        public final String id,gameId,host,opponent; public final boolean localHost;
        public final String[] board=new String[42]; public int turn; public String remoteMove=""; public boolean finished; public String result=""; public boolean rematchLocal,rematchRemote;
        private int nextLocalAction=0,nextRemoteAction=0,pendingRemoteAction=-1;
        public Match(String id,String gameId,String host,String opponent,boolean localHost){this.id=id;this.gameId=gameId;this.host=host;this.opponent=opponent;this.localHost=localHost;}
    }
    public static void init(){if(registered)return;registered=true;ClientReceiveMessageEvents.CHAT.register((message,signed,sender,params,receptionTimestamp)->handle(message,sender));ClientReceiveMessageEvents.ALLOW_CHAT.register((message,signed,sender,params,receptionTimestamp)->!message.getString().contains(PREFIX));}
    private static void handle(Text message,GameProfile sender){
        String s=message.getString();
        int p=s.indexOf(PREFIX);
        if(p<0)return;
        int end=s.indexOf("]]",p);
        if(end<0)return;
        int encodedStart=p+PREFIX.length();
        if(encodedStart>end||end-encodedStart>MAX_ENCODED_PAYLOAD)return;
        String enc=s.substring(encodedStart,end),body;
        try {byte[] decoded=Base64.getUrlDecoder().decode(enc);if(decoded.length>MAX_DECODED_PAYLOAD)return;body=new String(decoded,StandardCharsets.UTF_8);} catch(IllegalArgumentException e){return;}
        String[]a=body.split("\\|",-1);if(a.length==0)return;
        MinecraftClient mc=MinecraftClient.getInstance();String senderName=profileName(sender);
        purgeExpiredInvites();
        switch(a[0]){
        case "INVITE"->{if(a.length==3&&senderName!=null&&!a[1].isBlank()&&!a[2].isBlank()&&validGameId(a[2]))invites.putIfAbsent(a[1],new Invite(a[1],a[2],senderName,System.currentTimeMillis()));}
        case "ACCEPT"->{if(a.length==5&&senderName!=null){OutgoingInvite pending=outgoingInvites.get(a[1]);String self=playerName();if(pending!=null&&self!=null&&senderName.equalsIgnoreCase(pending.target())&&pending.gameId().equals(a[2])&&validGameId(a[2])&&self.equalsIgnoreCase(a[3])&&senderName.equalsIgnoreCase(a[4])){outgoingInvites.remove(a[1],pending);if(hasActiveMatchWith(self,senderName))return;Match m=new Match(pending.matchId(),pending.gameId(),self,senderName,true);if(matches.putIfAbsent(m.id,m)==null)mc.execute(()->mc.setScreen(new MultiplayerGameScreen(null,m)));}}}
        case "DECLINE"->{if(a.length==2)invites.remove(a[1]);}
        case "MOVE"->{if(a.length==4&&senderName!=null){Match m=matches.get(a[1]);if(m!=null&&!m.finished&&senderName.equalsIgnoreCase(m.opponent)){try{int seq=Integer.parseInt(a[2]);if(seq==m.nextRemoteAction&&m.pendingRemoteAction<0&&validRemoteTurn(m)&&validMove(m.gameId,a[3])){m.pendingRemoteAction=seq;m.remoteMove=a[3];}}catch(NumberFormatException ignored){}}}}
        case "REMATCH"->{if(a.length==3&&senderName!=null){Match m=matches.get(a[1]);if(m!=null&&!m.finished&&senderName.equalsIgnoreCase(m.opponent)&&"YES".equals(a[2])){m.rematchRemote=true;tryStartRematch(m);}}}
        case "LEAVE"->{if(a.length==2&&senderName!=null){Match m=matches.get(a[1]);if(m!=null&&senderName.equalsIgnoreCase(m.opponent)){m.finished=true;m.result="Opponent left the match.";}}}
        default->{}
    }}
    private static boolean validGameId(String id){return id.equals("tic_tac_toe")||id.equals("connect_four")||id.equals("rock_paper_scissors");}
    private static boolean validRemoteTurn(Match m){return m.gameId.equals("rock_paper_scissors")||((m.turn==0)!=m.localHost);}
    private static boolean validMove(String game,String move){try{if(move==null||move.length()<3||move.length()>12||move.charAt(1)!=':')return false;int value=Integer.parseInt(move.substring(2));if(game.equals("tic_tac_toe")&&move.startsWith("T:"))return value>=0&&value<9;if(game.equals("connect_four")&&move.startsWith("C:"))return value>=0&&value<7;if(game.equals("rock_paper_scissors")&&move.startsWith("R:"))return value>=0&&value<3;}catch(Exception ignored){}return false;}
    public static void acknowledgeRemoteMove(Match m,String payload){if(m==null||m.finished||m.pendingRemoteAction<0||!Objects.equals(m.remoteMove,payload))return;m.nextRemoteAction++;m.pendingRemoteAction=-1;m.remoteMove="";}
    public static void invite(String player,String gameId){String self=playerName();if(self==null||player==null||player.isBlank()||player.equalsIgnoreCase(self)||!validGameId(gameId))return;if(matches.values().stream().anyMatch(m->!m.finished&&(m.opponent.equalsIgnoreCase(player)||m.host.equalsIgnoreCase(player))))return;String id=UUID.randomUUID().toString().substring(0,12);outgoingInvites.put(id,new OutgoingInvite(id,gameId,player,System.currentTimeMillis()));send(player,"INVITE|"+id+"|"+gameId);}
    public static Set<Invite> pendingInvites(){purgeExpiredInvites();return Set.copyOf(invites.values());}
    private static void purgeExpiredInvites(){long now=System.currentTimeMillis();invites.values().removeIf(i->now-i.createdAt()>INVITE_TTL);outgoingInvites.values().removeIf(i->now-i.createdAt()>INVITE_TTL);}
    public static void accept(Invite inv){String self=playerName();if(self==null||inv==null||!validGameId(inv.gameId())||System.currentTimeMillis()-inv.createdAt()>INVITE_TTL||hasActiveMatchWith(self,inv.from()))return;invites.remove(inv.matchId());Match m=new Match(inv.matchId(),inv.gameId(),inv.from(),self,false);if(matches.putIfAbsent(m.id,m)!=null)return;send(inv.from(),"ACCEPT|"+m.id+"|"+m.gameId+"|"+m.host+"|"+m.opponent);MinecraftClient.getInstance().setScreen(new MultiplayerGameScreen(null,m));}
    public static void decline(Invite inv){if(inv==null)return;invites.remove(inv.matchId());send(inv.from(),"DECLINE|"+inv.matchId());}
    public static Match latestMatch(){purgeExpiredInvites();return matches.values().stream().filter(m->!m.finished).findFirst().orElseGet(()->matches.values().stream().findFirst().orElse(null));}
    public static void sendMove(Match m,String payload){if(m!=null&&!m.finished&&validMove(m.gameId,payload)){int seq=m.nextLocalAction++;send(m.opponent,"MOVE|"+m.id+"|"+seq+"|"+payload);}}
    public static void requestRematch(Match m){if(m==null||m.finished||m.rematchLocal)return;m.rematchLocal=true;send(m.opponent,"REMATCH|"+m.id+"|YES");tryStartRematch(m);}
    private static void tryStartRematch(Match m){if(m.rematchLocal&&m.rematchRemote){m.finished=false;m.result="";m.remoteMove="";m.rematchLocal=m.rematchRemote=false;m.nextLocalAction=m.nextRemoteAction=0;m.pendingRemoteAction=-1;m.turn=0;MinecraftClient.getInstance().setScreen(new MultiplayerGameScreen(null,m));}}
    public static void leave(Match m){if(m==null)return;send(m.opponent,"LEAVE|"+m.id);m.finished=true;matches.remove(m.id);}
    private static void send(String target,String body){MinecraftClient mc=MinecraftClient.getInstance();if(mc.getNetworkHandler()==null)return;try{String enc=Base64.getUrlEncoder().withoutPadding().encodeToString(body.getBytes(StandardCharsets.UTF_8));if(enc.length()>MAX_ENCODED_PAYLOAD)return;mc.getNetworkHandler().sendChatCommand("msg "+target+" "+PREFIX+enc+"]]");}catch(Exception e){if(mc.player!=null)mc.player.sendMessage(Text.literal("Unable to send a Gafi Minigames message. The server may not support /msg."),false);}}
    private static boolean hasActiveMatchWith(String a,String b){return matches.values().stream().anyMatch(m->!m.finished&&((m.host.equalsIgnoreCase(a)&&m.opponent.equalsIgnoreCase(b))||(m.host.equalsIgnoreCase(b)&&m.opponent.equalsIgnoreCase(a))));}
    private static String profileName(GameProfile p){return p==null?null:p.name();}
    private static String playerName(){MinecraftClient mc=MinecraftClient.getInstance();return mc.player==null?null:profileName(mc.player.getGameProfile());}
}
