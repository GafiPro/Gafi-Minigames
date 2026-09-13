package com.gafipro.minigames.game.board;

import com.gafipro.minigames.game.BaseGame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/** Real 8x8 checkers rules with mandatory captures, multi-jumps, kings and AI. */
public final class CheckersGame extends BaseGame {
    private static final int EMPTY=0, WHITE=1, BLACK=-1, WK=2, BK=-2;
    private final int[][] board=new int[8][8];
    private Difficulty difficulty=Difficulty.NORMAL;
    private int sr=-1,sc=-1,forcedR=-1,forcedC=-1,aiDelay;
    private boolean whiteTurn;
    private enum Difficulty{EASY,NORMAL,HARD}
    private record Jump(int fr,int fc,int tr,int tc,int cr,int cc){}
    private record Snap(int[][] b,boolean turn,int fr,int fc){}

    @Override public String id(){return "checkers";}
    @Override public String title(){return "Checkers";}
    @Override public String category(){return "Board";}

    @Override public void start(){
        for(int r=0;r<8;r++)for(int c=0;c<8;c++)board[r][c]=EMPTY;
        for(int r=0;r<3;r++)for(int c=0;c<8;c++)if((r+c)%2==1)board[r][c]=BLACK;
        for(int r=5;r<8;r++)for(int c=0;c<8;c++)if((r+c)%2==1)board[r][c]=WHITE;
        whiteTurn=true;sr=sc=forcedR=forcedC=-1;aiDelay=5;
        status="Normal • White to move • 1 Easy  2 Normal  3 Hard";
    }

    @Override public void tick(){super.tick();if(!finished&&!whiteTurn&&--aiDelay<=0){
        List<List<Jump>> turns=legalTurns(false);
        if(turns.isEmpty()){status="No legal moves — you win!";finishWin(1000+score);return;}
        playTurn(selectAi(turns));aiDelay=difficulty==Difficulty.HARD?2:5;
    }}

    private List<Jump> selectAi(List<List<Jump>> turns){
        if(difficulty==Difficulty.EASY)return turns.get(random.nextInt(turns.size()));
        int best=Integer.MIN_VALUE;List<List<Jump>> bests=new ArrayList<>();
        for(List<Jump> t:turns){Snap s=snap();applyTurn(t);int v=difficulty==Difficulty.HARD?search(3,Integer.MIN_VALUE+1,Integer.MAX_VALUE-1):evaluate();restore(s);
            if(v>best){best=v;bests.clear();bests.add(t);}else if(v==best)bests.add(t);
        }
        return bests.get(random.nextInt(bests.size()));
    }

    private int search(int depth,int alpha,int beta){
        List<List<Jump>> turns=legalTurns(whiteTurn);
        if(turns.isEmpty())return whiteTurn?-50000:50000;
        if(depth==0)return evaluate();
        boolean max=!whiteTurn;int best=max?Integer.MIN_VALUE:Integer.MAX_VALUE;
        for(List<Jump> t:turns){Snap s=snap();applyTurn(t);int v=search(depth-1,alpha,beta);restore(s);
            if(max){best=Math.max(best,v);alpha=Math.max(alpha,best);}else{best=Math.min(best,v);beta=Math.min(beta,best);}if(beta<=alpha)break;
        }
        return best;
    }

    private int evaluate(){int v=0;for(int r=0;r<8;r++)for(int c=0;c<8;c++){int p=board[r][c];if(p!=0){int m=Math.abs(p)==2?190:100;int adv=Math.abs(p)==2?0:(p>0?7-r:r);v+=Integer.signum(p)*(m+adv*3);}}return v;}
    private Snap snap(){int[][] b=new int[8][8];for(int r=0;r<8;r++)b[r]=board[r].clone();return new Snap(b,whiteTurn,forcedR,forcedC);}
    private void restore(Snap s){for(int r=0;r<8;r++)System.arraycopy(s.b[r],0,board[r],0,8);whiteTurn=s.turn;forcedR=s.fr;forcedC=s.fc;}

    private List<List<Jump>> legalTurns(boolean white){
        List<List<Jump>> caps=new ArrayList<>();
        if(forcedR>=0){
            for(Jump j:jumps(forcedR,forcedC,board[forcedR][forcedC]))caps.add(List.of(j));
            return caps;
        }
        for(int r=0;r<8;r++)for(int c=0;c<8;c++)if(owns(board[r][c],white))collectCaptures(r,c,new ArrayList<>(),caps);
        if(!caps.isEmpty())return caps;
        List<List<Jump>> moves=new ArrayList<>();
        for(int r=0;r<8;r++)for(int c=0;c<8;c++)if(owns(board[r][c],white))for(int[]d:dirs(board[r][c])){int tr=r+d[0],tc=c+d[1];if(in(tr,tc))moves.add(List.of(new Jump(r,c,tr,tc,-1,-1)));}
        return moves;
    }

    private void collectCaptures(int r,int c,List<Jump> path,List<List<Jump>> out){
        int piece=board[r][c];List<Jump> js=jumps(r,c,piece);
        if(js.isEmpty()){if(!path.isEmpty())out.add(new ArrayList<>(path));return;}
        for(Jump j:js){Snap s=snap();applyJump(j);path.add(j);boolean crowned=Math.abs(piece)==1&&(j.tr==(piece>0?0:7));if(crowned)out.add(new ArrayList<>(path));else collectCaptures(j.tr,j.tc,path,out);path.remove(path.size()-1);restore(s);}
    }

    private List<Jump> jumps(int r,int c,int piece){List<Jump> out=new ArrayList<>();for(int[]d:dirs(piece)){int mr=r+d[0],mc=c+d[1],tr=r+2*d[0],tc=c+2*d[1];if(in(tr,tc)&&in(mr,mc)&&board[mr][mc]!=0&&Integer.signum(board[mr][mc])!=Integer.signum(piece)&&board[tr][tc]==0)out.add(new Jump(r,c,tr,tc,mr,mc));}return out;}
    private int[][] dirs(int p){if(Math.abs(p)==2)return new int[][]{{1,1},{1,-1},{-1,1},{-1,-1}};return p>0?new int[][]{{-1,1},{-1,-1}}:new int[][]{{1,1},{1,-1}};}
    private boolean owns(int p,boolean white){return white?p>0:p<0;}
    private boolean in(int r,int c){return r>=0&&r<8&&c>=0&&c<8;}

    private void applyTurn(List<Jump> t){for(Jump j:t)applyJump(j);forcedR=forcedC=-1;whiteTurn=!whiteTurn;}
    private void playTurn(List<Jump> t){for(Jump j:t){applyJump(j);markMove();score+=j.cr>=0?100:10;}forcedR=forcedC=-1;whiteTurn=!whiteTurn;resolve();}
    private void applyJump(Jump j){int p=board[j.fr][j.fc];board[j.fr][j.fc]=0;if(j.cr>=0)board[j.cr][j.cc]=0;if(p==WHITE&&j.tr==0)p=WK;if(p==BLACK&&j.tr==7)p=BK;board[j.tr][j.tc]=p;}

    private void playHumanMove(Jump j){
        int movingPiece=board[j.fr][j.fc];
        boolean crowned=Math.abs(movingPiece)==1&&(j.tr==(movingPiece>0?0:7));
        applyJump(j);
        markMove();
        score+=j.cr>=0?100:10;
        if(crowned){
            forcedR=forcedC=-1;whiteTurn=!whiteTurn;sr=sc=-1;resolve();return;
        }
        List<Jump> next=jumps(j.tr,j.tc,board[j.tr][j.tc]);
        if(j.cr>=0 && !next.isEmpty()){
            forcedR=j.tr;forcedC=j.tc;sr=j.tr;sc=j.tc;status="Continue capture from the highlighted piece.";return;
        }
        forcedR=forcedC=-1;whiteTurn=!whiteTurn;sr=sc=-1;resolve();
    }

    private void resolve(){int white=0,black=0;for(int[]row:board)for(int p:row){if(p>0)white++;if(p<0)black++;}if(black==0){status="You win!";finishWin(1000+score);return;}if(white==0){status="Black wins.";finish(0);return;}if(legalTurns(whiteTurn).isEmpty()){if(whiteTurn){status="No legal moves — Black wins.";finish(0);}else{status="No legal moves — you win!";finishWin(1000+score);}}else status=whiteTurn?"Your turn • White":"Black is thinking...";}

    private int boardCell(){int h=MinecraftClient.getInstance().getWindow().getScaledHeight();int w=MinecraftClient.getInstance().getWindow().getScaledWidth();return Math.max(14,Math.min(54,Math.min((h-94)/8,(w-24)/8)));}
    private int boardTop(){return 68;}
    @Override public void render(DrawContext c,int mx,int my,float delta){var mc=MinecraftClient.getInstance();drawHeader(c,"CHECKERS",status);int cell=boardCell();int size=cell*8,ox=cx()-size/2,oy=boardTop();
        for(int r=0;r<8;r++)for(int col=0;col<8;col++){int x=ox+col*cell,y=oy+r*cell;c.fill(x,y,x+cell,y+cell,((r+col)&1)==0?0xFFD7C2A6:0xFF6D4A32);if(r==sr&&col==sc)c.fill(x+2,y+2,x+cell-2,y+cell-2,0xFFCCAA33);int p=board[r][col];if(p!=0){int cx=x+cell/2,cy=y+cell/2;c.fill(cx-cell/2+8,cy-cell/2+8,cx+cell/2-8,cy+cell/2-8,p>0?0xFFEEEEEE:0xFF333333);if(Math.abs(p)==2)c.drawCenteredTextWithShadow(mc.textRenderer,Text.literal("K"),cx,cy-5,p>0?0xFF333333:0xFFFFFFFF);}}
        c.drawCenteredTextWithShadow(mc.textRenderer,Text.literal("Click a piece and its legal destination • R Restart • 1/2/3 Difficulty"),cx(),oy+size+10,0xFFAAAAAA);
    }

    @Override public boolean mouseClicked(double mx,double my,int button){
        if(button!=0||finished||!whiteTurn)return true;
        int cell=boardCell();int size=cell*8,ox=cx()-size/2,oy=boardTop();
        int c=(int)((mx-ox)/cell),r=(int)((my-oy)/cell);if(!in(r,c))return true;

        if(sr<0){
            if(owns(board[r][c],true)){
                List<List<Jump>> legal=legalTurns(true);
                boolean captureExists=legal.stream().anyMatch(t->!t.isEmpty()&&t.get(0).cr>=0);
                boolean pieceCanMove=legal.stream().anyMatch(t->!t.isEmpty()&&t.get(0).fr==r&&t.get(0).fc==c);
                if(!captureExists||pieceCanMove){sr=r;sc=c;}
            }
            return true;
        }

        for(List<Jump> t:legalTurns(true)){
            if(t.isEmpty())continue;
            Jump j=t.get(0);
            if(j.fr==sr&&j.fc==sc&&j.tr==r&&j.tc==c){playHumanMove(j);return true;}
        }

        if(forcedR<0&&owns(board[r][c],true)){sr=r;sc=c;}
        return true;
    }
    @Override public void keyPressed(int key,int scan,int modifiers){if(key==GLFW.GLFW_KEY_R){begin();return;}if(finished)return;if(key==GLFW.GLFW_KEY_1){difficulty=Difficulty.EASY;begin();}else if(key==GLFW.GLFW_KEY_2){difficulty=Difficulty.NORMAL;begin();}else if(key==GLFW.GLFW_KEY_3){difficulty=Difficulty.HARD;begin();}}
}
