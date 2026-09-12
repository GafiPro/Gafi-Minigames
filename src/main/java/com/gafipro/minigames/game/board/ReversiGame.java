package com.gafipro.minigames.game.board;

import com.gafipro.minigames.game.BaseGame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.List;

public final class ReversiGame extends BaseGame {
    private static final int N=8, W=1, B=-1;
    private static final int[][] DIR={{-1,-1},{-1,0},{-1,1},{0,-1},{0,1},{1,-1},{1,0},{1,1}};
    private final int[][] board=new int[N][N];
    private boolean whiteTurn=true; private Difficulty difficulty=Difficulty.NORMAL; private int aiDelay;
    private enum Difficulty{EASY,NORMAL,HARD} private record Move(int r,int c){}
    @Override public String id(){return "reversi";} @Override public String title(){return "Reversi";} @Override public String category(){return "Board";}
    @Override public void start(){for(int r=0;r<N;r++)for(int c=0;c<N;c++)board[r][c]=0;board[3][3]=B;board[3][4]=W;board[4][3]=W;board[4][4]=B;whiteTurn=true;aiDelay=4;status="Normal • White to move • 1 Easy  2 Normal  3 Hard";}
    @Override public void tick(){super.tick();if(!finished&&!whiteTurn&&--aiDelay<=0)aiMove();}
    private void aiMove(){List<Move> ms=legal(B);if(ms.isEmpty()){passOrEnd();return;}Move m=choose(ms);place(m,B);markMove();whiteTurn=true;resolve();if(!finished)aiDelay=difficulty==Difficulty.HARD?2:4;}
    private Move choose(List<Move> ms){if(difficulty==Difficulty.EASY)return ms.get(random.nextInt(ms.size()));int depth=difficulty==Difficulty.HARD?4:2,best=Integer.MIN_VALUE;List<Move> bests=new ArrayList<>();for(Move m:ms){int[][]s=copy();place(m,B);whiteTurn=true;int v=search(depth-1,Integer.MIN_VALUE+1,Integer.MAX_VALUE-1);whiteTurn=false;restore(s);if(v>best){best=v;bests.clear();bests.add(m);}else if(v==best)bests.add(m);}return bests.get(random.nextInt(bests.size()));}
    private int search(int depth,int alpha,int beta){int p=whiteTurn?W:B;List<Move> ms=legal(p);if(ms.isEmpty()){if(legal(-p).isEmpty())return terminal();boolean old=whiteTurn;whiteTurn=!whiteTurn;int v=search(depth,alpha,beta);whiteTurn=old;return v;}if(depth<=0)return evaluate();boolean max=p==B;int best=max?Integer.MIN_VALUE:Integer.MAX_VALUE;for(Move m:ms){int[][]s=copy();place(m,p);whiteTurn=!whiteTurn;int v=search(depth-1,alpha,beta);whiteTurn=!whiteTurn;restore(s);if(max){best=Math.max(best,v);alpha=Math.max(alpha,best);}else{best=Math.min(best,v);beta=Math.min(beta,best);}if(beta<=alpha)break;}return best;}
    private int evaluate(){int v=0;for(int r=0;r<N;r++)for(int c=0;c<N;c++){int p=board[r][c];if(p==0)continue;int pos=(r==0||r==7)&&(c==0||c==7)?180:(r==0||r==7||c==0||c==7?25:0);v+=p*(100+pos);}v+=(legal(B).size()-legal(W).size())*10;return v;}
    private int terminal(){return (count(B)-count(W))*10000;}
    private List<Move> legal(int p){List<Move> out=new ArrayList<>();for(int r=0;r<N;r++)for(int c=0;c<N;c++)if(board[r][c]==0&&flips(r,c,p))out.add(new Move(r,c));return out;}
    private boolean flips(int r,int c,int p){for(int[]d:DIR){int x=r+d[0],y=c+d[1],n=0;while(in(x,y)&&board[x][y]==-p){n++;x+=d[0];y+=d[1];}if(n>0&&in(x,y)&&board[x][y]==p)return true;}return false;}
    private void place(Move m,int p){board[m.r][m.c]=p;for(int[]d:DIR){int x=m.r+d[0],y=m.c+d[1];List<Move> line=new ArrayList<>();while(in(x,y)&&board[x][y]==-p){line.add(new Move(x,y));x+=d[0];y+=d[1];}if(!line.isEmpty()&&in(x,y)&&board[x][y]==p)for(Move q:line)board[q.r][q.c]=p;}}
    private void resolve(){List<Move> next=legal(whiteTurn?W:B);if(!next.isEmpty()){status=whiteTurn?"Your turn • White":"Black is thinking...";return;}passOrEnd();}
    private void passOrEnd(){whiteTurn=!whiteTurn;if(!legal(whiteTurn?W:B).isEmpty()){status=whiteTurn?"Your turn • White":"Black is thinking...";if(!whiteTurn)aiDelay=2;return;}int w=count(W),b=count(B);if(w>b){status="You win • "+w+"–"+b;finishWin(500+w*10);}else if(b>w){status="Black wins • "+b+"–"+w;finish(0);}else{status="Draw • "+w+"–"+b;finishDraw(300);}}
    private int count(int p){int n=0;for(int[]r:board)for(int v:r)if(v==p)n++;return n;}private boolean in(int r,int c){return r>=0&&r<N&&c>=0&&c<N;}
    private int[][] copy(){int[][]s=new int[N][N];for(int r=0;r<N;r++)s[r]=board[r].clone();return s;}private void restore(int[][]s){for(int r=0;r<N;r++)System.arraycopy(s[r],0,board[r],0,N);}
    @Override public void render(DrawContext c,int mx,int my,float d){var mc=MinecraftClient.getInstance();drawHeader(c,"REVERSI",status);int cell=Math.min(52,Math.max(34,(mc.getWindow().getScaledHeight()-155)/8)),size=cell*8,ox=cx()-size/2,oy=78;for(int r=0;r<N;r++)for(int col=0;col<N;col++){int x=ox+col*cell,y=oy+r*cell;c.fill(x,y,x+cell,y+cell,0xFF2E7D4F);int p=board[r][col];if(p!=0)c.fill(x+7,y+7,x+cell-7,y+cell-7,p==W?0xFFF0F0F0:0xFF202020);}c.drawCenteredTextWithShadow(mc.textRenderer,Text.literal("White: "+count(W)+" • Black: "+count(B)+" • Click a legal square"),cx(),oy+size+10,0xFFAAAAAA);}
    @Override public boolean mouseClicked(double mx,double my,int button){if(button!=0||finished||!whiteTurn)return true;int cell=Math.min(52,Math.max(34,(MinecraftClient.getInstance().getWindow().getScaledHeight()-155)/8)),size=cell*8,ox=cx()-size/2,oy=78,c=(int)((mx-ox)/cell),r=(int)((my-oy)/cell);if(!in(r,c))return true;for(Move m:legal(W))if(m.r==r&&m.c==c){place(m,W);markMove();whiteTurn=false;resolve();return true;}return true;}
    @Override public void keyPressed(int key,int scan,int mods){if(key==GLFW.GLFW_KEY_R){begin();return;}if(finished)return;if(key==GLFW.GLFW_KEY_1){difficulty=Difficulty.EASY;begin();}else if(key==GLFW.GLFW_KEY_2){difficulty=Difficulty.NORMAL;begin();}else if(key==GLFW.GLFW_KEY_3){difficulty=Difficulty.HARD;begin();}}
}
