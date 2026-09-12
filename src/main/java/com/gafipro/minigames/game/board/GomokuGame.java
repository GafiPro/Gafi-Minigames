package com.gafipro.minigames.game.board;

import com.gafipro.minigames.game.BaseGame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Five-in-a-row on a 15x15 board with tactical AI and local legal-move rules. */
public final class GomokuGame extends BaseGame {
    private static final int N=15; private final int[][] b=new int[N][N]; private boolean white=true; private int aiDelay;
    private Difficulty difficulty=Difficulty.NORMAL; private enum Difficulty{EASY,NORMAL,HARD} private record Move(int r,int c,int value){}
    @Override public String id(){return "gomoku";} @Override public String title(){return "Gomoku";} @Override public String category(){return "Board";}
    @Override public void start(){for(int r=0;r<N;r++)for(int c=0;c<N;c++)b[r][c]=0;white=true;aiDelay=4;status="Normal • White to move • 1 Easy  2 Normal  3 Hard";}
    @Override public void tick(){super.tick();if(!finished&&!white&&--aiDelay<=0){Move m=choose();if(m==null){finishDraw(300);status="Draw.";return;}b[m.r][m.c]=-1;markMove();white=true;resolve(m.r,m.c,-1);aiDelay=difficulty==Difficulty.HARD?2:4;}}
    private Move choose(){List<Move> ms=candidates(-1);if(ms.isEmpty())return null;if(difficulty==Difficulty.EASY)return ms.get(random.nextInt(ms.size()));int best=Integer.MIN_VALUE;List<Move> picks=new ArrayList<>();for(Move m:ms){b[m.r][m.c]=-1;int v=evaluateMove(m.r,m.c,-1);b[m.r][m.c]=0;if(difficulty==Difficulty.HARD){int[][]dirs={{1,0},{0,1},{1,1},{1,-1}};for(int[]d:dirs)if(count(m.r,m.c,-1,d[0],d[1])+count(m.r,m.c,-1,-d[0],-d[1])-1>=4)v+=10000;for(int[]d:dirs)if(count(m.r,m.c,1,d[0],d[1])+count(m.r,m.c,1,-d[0],-d[1])-1>=4)v+=9000;}if(v>best){best=v;picks.clear();picks.add(m);}else if(v==best)picks.add(m);}return picks.get(random.nextInt(picks.size()));}
    private List<Move> candidates(int p){List<Move> ms=new ArrayList<>();boolean any=false;for(int r=0;r<N;r++)for(int c=0;c<N;c++)if(b[r][c]!=0)any=true;if(!any){ms.add(new Move(N/2,N/2,0));return ms;}for(int r=0;r<N;r++)for(int c=0;c<N;c++)if(b[r][c]==0&&near(r,c))ms.add(new Move(r,c,0));ms.sort(Comparator.comparingInt((Move m)->evaluateMove(m.r,m.c,p)).reversed());return ms.subList(0,Math.min(ms.size(),difficulty==Difficulty.HARD?22:12));}
    private boolean near(int r,int c){for(int dr=-2;dr<=2;dr++)for(int dc=-2;dc<=2;dc++)if((dr!=0||dc!=0)&&in(r+dr,c+dc)&&b[r+dr][c+dc]!=0)return true;return false;}
    private int evaluateMove(int r,int c,int p){int v=0;int[][]d={{1,0},{0,1},{1,1},{1,-1}};for(int[]q:d){int a=count(r,c,p,q[0],q[1])+count(r,c,p,-q[0],-q[1])-1;v+=a*a*15;}int center=Math.abs(r-7)+Math.abs(c-7);v+=Math.max(0,12-center);return v;}
    private int count(int r,int c,int p,int dr,int dc){int n=0,x=r,y=c;while(in(x,y)&&b[x][y]==p){n++;x+=dr;y+=dc;}return n;}
    private boolean win(int r,int c,int p){int[][]d={{1,0},{0,1},{1,1},{1,-1}};for(int[]q:d)if(count(r,c,p,q[0],q[1])+count(r,c,p,-q[0],-q[1])-1>=5)return true;return false;}
    private void resolve(int r,int c,int p){if(win(r,c,p)){if(p==1){status="You win!";finishWin(1000+Math.max(0,500-metrics.moves()*5));}else{status="Black wins.";finish(0);}return;}boolean full=true;for(int x=0;x<N;x++)for(int y=0;y<N;y++)if(b[x][y]==0)full=false;if(full){status="Draw — board full.";finishDraw(500);return;}status=white?"Your turn • White":"Black is thinking...";}
    private boolean in(int r,int c){return r>=0&&r<N&&c>=0&&c<N;}
    @Override public void render(DrawContext c,int mx,int my,float d){var mc=MinecraftClient.getInstance();drawHeader(c,"GOMOKU",status);int cell=Math.min(30,Math.max(20,(mc.getWindow().getScaledHeight()-165)/N));int size=cell*N,ox=cx()-size/2,oy=74;c.fill(ox-4,oy-4,ox+size+4,oy+size+4,0xFFB98A55);for(int r=0;r<N;r++)for(int col=0;col<N;col++){int x=ox+col*cell+cell/2,y=oy+r*cell+cell/2;c.fill(x-1,y-1,x+1,y+1,0xFF403322);int p=b[r][col];if(p!=0)c.fill(x-cell/2+3,y-cell/2+3,x+cell/2-3,y+cell/2-3,p>0?0xFFF2F2F2:0xFF252525);}c.drawCenteredTextWithShadow(mc.textRenderer,Text.literal("Five in a row • Click an empty intersection • R Restart • 1/2/3 Difficulty"),cx(),oy+size+8,0xFFAAAAAA);}
    @Override public boolean mouseClicked(double mx,double my,int button){if(button!=0||finished||!white)return true;int cell=Math.min(30,Math.max(20,(MinecraftClient.getInstance().getWindow().getScaledHeight()-165)/N));int size=cell*N,ox=cx()-size/2,oy=74;int col=Math.round((float)((mx-ox-cell/2)/cell)),r=Math.round((float)((my-oy-cell/2)/cell));if(!in(r,col)||b[r][col]!=0)return true;b[r][col]=1;markMove();white=false;resolve(r,col,1);if(!finished)aiDelay=4;return true;}
    @Override public void keyPressed(int k,int s,int m){if(k==GLFW.GLFW_KEY_R){begin();return;}if(finished)return;if(k==GLFW.GLFW_KEY_1){difficulty=Difficulty.EASY;begin();}else if(k==GLFW.GLFW_KEY_2){difficulty=Difficulty.NORMAL;begin();}else if(k==GLFW.GLFW_KEY_3){difficulty=Difficulty.HARD;begin();}}
}
