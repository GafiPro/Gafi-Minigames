package com.gafipro.minigames.gui;

import com.gafipro.minigames.multiplayer.MultiplayerManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class MultiplayerGameScreen extends Screen {
    private final Screen parent; private final MultiplayerManager.Match match;
    private final int[] ttt=new int[9]; private final int[][] connect=new int[7][6];
    private int localRps=-1,remoteRps=-1; private boolean localFinished;
    public MultiplayerGameScreen(Screen parent,MultiplayerManager.Match match){super(Text.literal("Gafi Minigames PvP"));this.parent=parent;this.match=match;}
    @Override protected void init(){}
    private String name(){return MinecraftClient.getInstance().player==null?"":MinecraftClient.getInstance().player.getGameProfile().name();}
    private boolean myTurn(){return match.gameId.equals("rock_paper_scissors")||((match.turn==0)==match.localHost);}
    @Override public void tick(){
        if(match.finished){
            if(!localFinished){localFinished=true;String msg=match.result==null||match.result.isBlank()?"Match finished.":match.result;MinecraftClient.getInstance().setScreen(new PvPResultScreen(parent,match,msg));}
            return;
        }
        String r=match.remoteMove;if(!r.isEmpty()){if(applyRemote(r))MultiplayerManager.acknowledgeRemoteMove(match,r);}
    }
    private boolean applyRemote(String move){try{
        if(match.gameId.equals("tic_tac_toe")&&move.startsWith("T:")){int i=Integer.parseInt(move.substring(2));if(i>=0&&i<9&&ttt[i]==0){ttt[i]=match.localHost?2:1;match.turn=1-match.turn;checkTtt();return true;}}
        else if(match.gameId.equals("connect_four")&&move.startsWith("C:")){int col=Integer.parseInt(move.substring(2));if(col>=0&&col<7&&drop(connect,col,match.localHost?2:1)){match.turn=1-match.turn;checkConnect();return true;}}
        else if(match.gameId.equals("rock_paper_scissors")&&move.startsWith("R:")){int v=Integer.parseInt(move.substring(2));if(v>=0&&v<3){remoteRps=v;resolveRps();return true;}}
    }catch(NumberFormatException ignored){}return false;}
    private void checkTtt(){int w=winner(ttt);if(w==1||w==2)finishMessage(w==(match.localHost?1:2)?"You win!":"You lose!");else if(w==3)finishMessage("Draw!");}
    private int winner(int[] b){int[][] l={{0,1,2},{3,4,5},{6,7,8},{0,3,6},{1,4,7},{2,5,8},{0,4,8},{2,4,6}};for(int[]q:l)if(b[q[0]]!=0&&b[q[0]]==b[q[1]]&&b[q[1]]==b[q[2]])return b[q[0]];for(int v:b)if(v==0)return 0;return 3;}
    private void checkConnect(){for(int p:new int[]{1,2})for(int x=0;x<7;x++)for(int y=0;y<6;y++)for(int[]d:new int[][]{{1,0},{0,1},{1,1},{1,-1}}){int n=0;for(int k=0;k<4;k++){int a=x+d[0]*k,z=y+d[1]*k;if(a>=0&&a<7&&z>=0&&z<6&&connect[a][z]==p)n++;}if(n==4){finishMessage(p==(match.localHost?1:2)?"You win!":"You lose!");return;}}boolean full=true;for(int x=0;x<7;x++)if(connect[x][0]==0)full=false;if(full)finishMessage("Draw!");}
    private boolean drop(int[][] b,int col,int p){if(col<0||col>=7)return false;for(int y=5;y>=0;y--)if(b[col][y]==0){b[col][y]=p;return true;}return false;}
    private void resolveRps(){if(localRps<0||remoteRps<0)return;int r=localRps==remoteRps?0:((localRps-remoteRps+3)%3==1?1:2);finishMessage(r==1?"You win!":r==2?"You lose!":"Draw!");}
    private void finishMessage(String msg){if(localFinished)return;localFinished=true;match.finished=true;match.result=msg;MinecraftClient.getInstance().setScreen(new PvPResultScreen(parent,match,msg));}
    @Override public void render(DrawContext c,int mx,int my,float d){renderInGameBackground(c);String title=switch(match.gameId){case "tic_tac_toe"->"TIC-TAC-TOE";case "connect_four"->"CONNECT FOUR";default->"ROCK PAPER SCISSORS";};drawHeader(c,title,match.opponent);if(match.gameId.equals("tic_tac_toe"))renderTtt(c);else if(match.gameId.equals("connect_four"))renderConnect(c);else renderRps(c);}
    private void drawHeader(DrawContext c,String title,String opp){c.drawCenteredTextWithShadow(textRenderer,Text.literal(title),width/2,18,0xFFFFFFFF);c.drawCenteredTextWithShadow(textRenderer,Text.literal("You: "+name()+"   vs   "+opp),width/2,32,0xFFAAAAAA);c.drawCenteredTextWithShadow(textRenderer,Text.literal(myTurn()?"YOUR TURN":"OPPONENT'S TURN"),width/2,48,0xFF55CC88);}
    private int tttCell(){return Math.min(76,Math.max(45,(height-108)/3));}
    private int tttLeft(int cell){return width/2-3*cell/2;}
    private int tttTop(){return 60;}
    private int connectCell(){return Math.min(42,Math.max(24,(height-102)/6));}
    private int connectLeft(int cell){return width/2-7*cell/2;}
    private int connectTop(){return 62;}
    private void renderTtt(DrawContext c){int s=tttCell(),ox=tttLeft(s),oy=tttTop();for(int i=0;i<9;i++){int x=i%3,y=i/3;c.fill(ox+x*s,oy+y*s,ox+x*s+s-4,oy+y*s+s-4,0xFF3B424A);if(ttt[i]!=0)c.drawCenteredTextWithShadow(textRenderer,Text.literal(ttt[i]==1?"X":"O"),ox+x*s+s/2,oy+y*s+s/2-5,ttt[i]==(match.localHost?1:2)?0xFF55CC88:0xFFE76C5F);}}
    private void renderConnect(DrawContext c){int s=connectCell(),ox=connectLeft(s),oy=connectTop();c.fill(ox-4,oy-4,ox+7*s+4,oy+6*s+4,0xFF244A99);for(int y=0;y<6;y++)for(int x=0;x<7;x++){int p=connect[x][y];c.fill(ox+x*s+4,oy+y*s+4,ox+x*s+s-4,oy+y*s+s-4,p==0?0xFFD5D7DA:p==(match.localHost?1:2)?0xFFE74C3C:0xFF55AADD);}}
    private void renderRps(DrawContext c){String[]a={"Rock","Paper","Scissors"};for(int i=0;i<3;i++){int x=width/2-150+i*100;c.fill(x,90,x+85,145,0xFF3B424A);c.drawCenteredTextWithShadow(textRenderer,Text.literal(a[i]),x+42,110,0xFFFFFFFF);}if(localRps>=0)c.drawCenteredTextWithShadow(textRenderer,Text.literal("Your move: "+a[localRps]),width/2,175,0xFFFFFFFF);}
    @Override public boolean mouseClicked(Click click,boolean doubled){if(click.button()!=GLFW.GLFW_MOUSE_BUTTON_1||match.finished)return true;double mx=click.x(),my=click.y();if(match.gameId.equals("tic_tac_toe"))clickTtt(mx,my);else if(match.gameId.equals("connect_four"))clickConnect(mx,my);else clickRps(mx,my);return true;}
    private void clickTtt(double mx,double my){if(!myTurn())return;int s=tttCell(),ox=tttLeft(s),oy=tttTop(),x=(int)((mx-ox)/s),y=(int)((my-oy)/s);if(x<0||y<0||x>=3||y>=3)return;int i=y*3+x;if(ttt[i]!=0)return;ttt[i]=match.localHost?1:2;match.turn=1-match.turn;MultiplayerManager.sendMove(match,"T:"+i);checkTtt();}
    private void clickConnect(double mx,double my){if(!myTurn())return;int s=connectCell(),ox=connectLeft(s),oy=connectTop(),col=(int)((mx-ox)/s);if(col<0||col>=7||!drop(connect,col,match.localHost?1:2))return;match.turn=1-match.turn;MultiplayerManager.sendMove(match,"C:"+col);checkConnect();}
    private void clickRps(double mx,double my){if(localRps>=0)return;for(int i=0;i<3;i++){int x=width/2-150+i*100;if(mx>=x&&mx<x+85&&my>=90&&my<145){localRps=i;MultiplayerManager.sendMove(match,"R:"+i);resolveRps();return;}}}
    @Override public boolean keyPressed(KeyInput input){if(input.key()==GLFW.GLFW_KEY_ESCAPE){MultiplayerManager.leave(match);MinecraftClient.getInstance().setScreen(parent);return true;}return super.keyPressed(input);}
}
