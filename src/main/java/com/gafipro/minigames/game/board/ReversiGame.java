package com.gafipro.minigames.game.board;

import com.gafipro.minigames.game.BaseGame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.List;

/** Reversi/Othello with legal moves, directional flips, forced passes and AI. */
public final class ReversiGame extends BaseGame {
    private final int[][] b=new int[8][8]; private boolean white=true; private Difficulty difficulty=Difficulty.NORMAL; private int aiDelay;
    private enum Difficulty{EASY,NORMAL,HARD} private record Move(int r,int c){}
    private static final int[][] D={{-1,-1},{-1,0},{-1,1},{0,-1},{0,1},{1,-1},{1,0},{1,1}};
    @Override public String id(){return "reversi";} @Override public String title(){return "Reversi";} @Override public String category(){return "Board";}
    @Override public void start(){for(int r=0;r<8;r++)for(int c=0;c<8;c++)b[r][c]=0;b[3][3]=-1;b[3][4]=1;b[4][3]=1;b[4][4]=-1;white=true;aiDelay=4;status="Normal • White to move • 1 Easy  2 Normal  3 Hard";}
    @Override public void tick(){super.tick();if(!finished&&!white&&--aiDelay<=0)ai();}
    private void ai(){List<Move> ms=moves(-1);if(ms.isEmpty()){pass();return;}Move m=choose(ms);play(m,-1);white=true;resolve();aiDelay=difficulty==Difficulty.HARD?2:4;}
    private Move choose(List<Move> ms){if(difficulty==Difficulty.EASY)return ms.get(random.nextInt(ms.size()));int depth=difficulty==Difficulty.HARD?4:2,best=Integer.MIN_VALUE;List<Move> picks=new ArrayList<>();for(Move m:ms){int[][]s=copy();play(m,-1);boolean old=white;white=false;int v=search(depth-1,Integer.MIN_VALUE+1,Integer.MAX_VALUE-1);white=old;restore(s);if(v>best){best=v;picks.clear();picks.add(m);}else if(v==best)picks.add(m);}return picks.get(random.nextInt(picks.size()));}
    private int search(int depth,int a,int z){List<Move> ms=moves(white?1:-1);if(ms.isEmpty()){if(moves(white? -1:1).isEmpty())return terminal();boolean old=white;white=!white;int v=search(depth,a,z);white=old;return v;}if(depth<=0)return eval();boolean max=!white;int best=max?Integer.MIN_VALUE:Integer.MAX_VALUE;for(Move m:ms){int[][]s=copy();int p=white?1:-1;play(m,p);white=!white;int v=search(depth-1,a,z);white=!white;restore(s);if(max){best=Math.max(best,v);a=Math.max(a,best);}else{best=Math.min(best,v);z=Math.min(z,best);}if(z<=a)break;}return best;}
    private int terminal(){int w=count(1),q=count(-1);return (q-w)*10000;}
    private int eval(){int v=0;for(int r=0;r<8;r++)for(int c=0;c<8;c++)if(b[r][c]!=0){int corner=(r==0||r==7)&&(c==0||c==7)?150:((r==0||r==7||c==0||c==7)?20:0);v+=b[r][c]*(100+corner);}v+=(moves(-1).size()-moves(1).size())*-12;return v;}
    private List<Move> moves(int p){List<Move> out=new ArrayList<>();for(int r=0;r<8;r++)for(int c=0;c<8;c++)if(b[r][c]==0&&flips(r,c,p))out.add(new Move(r,c));return out;}
    private boolean flips(int r,int c,int p){for(int[]d:D){int x=r+d[0],y=c+d[1],n=0;while(in(x,y)&&b[x][y]==-p){n++;x+=d[0];y+=d[1];}if(n>0&&in(x,y)&&b[x][y]==p)return true;}return false;}
    private void play(Move m,int p){b[m.r][m.c]=p;for(int[]d:D){int x=m.r+d[0],y=m.c+d[1];List<Move> f=new ArrayList<>();while(in(x,y)&&b[x][y]==-p){f.add(new Move(x,y));x+=d[0];y+=d[1];}if(!f.isEmpty()&&in(x,y)&&b[x][y]==p)for(Move q:f)b[q.r][q.c]=p;}}
    private void resolve(){List<Move> ms=moves(white?1:-1);if(!ms.isEmpty()){status=white?"Your turn • White":"Black is thinking...";return;}pass();}
    private void pass(){white=!white;if(!moves(white?1:-1).isEmpty()){status=white?"Your turn • White":"Black is thinking...";if(!white)aiDelay=2;return;}int w=count(1),q=count(-1);if(w>q){status="You win • "+w+"–"+q;finishWin(500+w*10);}else if(q>w){status="Black wins • "+q+"–"+w;finish(0);}else{status="Draw • "+w+"–"+q;finishDraw(300);}}
    private int count(int p){int n=0;for(int[]r:b)for(int v:r)if(v==p)n++;return n;} private boolean in(int r,int c){return r>=0&&r<8&&c>=0&&c<8;}
    private int[][] copy(){int[][]s=new int[8][8];for(int r=0;r<8;r++)s[r]=b[r].clone();return s;} private void restore(int[][]s){for(int r=0;r<8;r++)System.arraycopy(s[r],0,b[r],0,8);}
    @Override public void render(DrawContext c,int mx,int my,float d){var mc=MinecraftClient.getInstance();drawHeader(c,"REVERSI",status);int cell=Math.min(52,Math.max(34,(mc.getWindow().getScaledHeight()-155)/8)),size=cell*8,ox=cx()-size/2,oy=78;for(int r=0;r<8;r++)for(int col=0;col<8;col++){int x=ox+col*cell,y=oy+r*cell;c.fill(x,y,x+cell,y+cell,0xFF2E7D4F);if(b[r][col]!=0)c.fill(x+7,y+7,x+cell-7,y+cell-7,b[r][col]>0?0xFFF0F0F0:0xFF202020);}c.drawCenteredTextWithShadow(mc.textRenderer,Text.literal("White: "+count(1)+" • Black: "+count(-1)),cx(),oy+size+10,0xFFAAAAAA);}
    @Override public boolean mouseClicked(double mx,double my,int button){if(button!=0||finished||!white)return true;int cell=Math.min(52,Math.max(34,(MinecraftClient.getInstance().getWindow().getScaledHeight()-155)/8)),size=cell*8,ox=cx()-size/2,oy=78,c=(int)((mx-ox)/cell),r=(int)((my-oy)/cell);if(!in(r,c))return true;for(Move m:moves(1))if(m.r==r&&m.c==c){play(m,1);markMove();white=false;resolve();return true;}return true;}
    @Override public void keyPressed(int key,int scan,int mods){if(key==GLFW.GLFW_KEY_R){begin();return;}if(finished)return;if(key==GLFW.GLFW_KEY_1){difficulty=Difficulty.EASY;begin();}else if(key==GLFW.GLFW_KEY_2){difficulty=Difficulty.NORMAL;begin();}else if(key==GLFW.GLFW_KEY_3){difficulty=Difficulty.HARD;begin();}}
}
