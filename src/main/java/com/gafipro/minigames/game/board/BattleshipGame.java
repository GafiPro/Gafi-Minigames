package com.gafipro.minigames.game.board;

import com.gafipro.minigames.game.BaseGame;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.List;

/** Local Battleship: hidden fleet, legal placement, hits, misses and sunk ships against an AI. */
public final class BattleshipGame extends BaseGame {
    private static final int N=10;
    private static final int[] FLEET={5,4,3,3,2};
    private final boolean[][] enemyShips=new boolean[N][N], playerShips=new boolean[N][N], shots=new boolean[N][N], enemyShots=new boolean[N][N];
    private final int[][] enemyShipId=new int[N][N], playerShipId=new int[N][N];
    private final int[] enemyRemaining=new int[FLEET.length], playerRemaining=new int[FLEET.length];
    private int shipsLeft=FLEET.length, enemyShipsLeft=FLEET.length, hits;

    @Override public String id(){return "battleship";}
    @Override public String title(){return "Battleship";}
    @Override public String category(){return "Board";}
    @Override public void start(){clear();for(int i=0;i<FLEET.length;i++){enemyRemaining[i]=playerRemaining[i]=FLEET[i];placeRandom(playerShips,playerShipId,i,FLEET[i]);placeRandom(enemyShips,enemyShipId,i,FLEET[i]);}shipsLeft=enemyShipsLeft=FLEET.length;hits=0;status="Fire on the enemy grid.";}
    private void clear(){for(int x=0;x<N;x++)for(int y=0;y<N;y++){enemyShips[x][y]=playerShips[x][y]=shots[x][y]=enemyShots[x][y]=false;enemyShipId[x][y]=playerShipId[x][y]=-1;}}
    private void placeRandom(boolean[][]grid,int[][]ids,int id,int len){for(int t=0;t<500;t++){int x=random.nextInt(N),y=random.nextInt(N),dx=random.nextBoolean()?1:0,dy=dx==0?1:0;if(x+dx*(len-1)>=N||y+dy*(len-1)>=N)continue;boolean ok=true;for(int i=0;i<len;i++)if(grid[x+dx*i][y+dy*i]){ok=false;break;}if(!ok)continue;for(int i=0;i<len;i++){int a=x+dx*i,b=y+dy*i;grid[a][b]=true;ids[a][b]=id;}return;}}
    private boolean allSunk(int[]left){for(int v:left)if(v>0)return false;return true;}
    private void enemyTurn(){for(int tries=0;tries<300;tries++){int x=random.nextInt(N),y=random.nextInt(N);if(enemyShots[x][y])continue;enemyShots[x][y]=true;if(playerShips[x][y]){int id=playerShipId[x][y];if(--playerRemaining[id]==0){shipsLeft--;status="The AI sank one of your ships.";}else status="The AI hit your fleet.";}else status="The AI missed.";return;}}
    @Override public boolean mouseClicked(double mx,double my,int button){if(button!=0||finished)return true;int cell=Math.min(34,Math.max(20,(heightSafe())/N));int ox=cx()+15-cell*N/2,oy=72;int x=(int)((mx-ox)/cell),y=(int)((my-oy)/cell);if(x<0||y<0||x>=N||y>=N||shots[x][y])return true;shots[x][y]=true;if(enemyShips[x][y]){int id=enemyShipId[x][y];hits++;enemyRemaining[id]--;score+=100;if(enemyRemaining[id]==0){enemyShipsLeft--;status="HIT — ship sunk.";}else status="HIT.";}else status="MISS.";if(enemyShipsLeft==0){finishWin(score+1000);return true;}enemyTurn();if(shipsLeft==0)finish(score);return true;}
    private int heightSafe(){return Math.max(200,net.minecraft.client.MinecraftClient.getInstance().getWindow().getScaledHeight()-95);}
    @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"BATTLESHIP","Enemy grid • Blue: unknown  •  Red: hit  •  Gray: miss");int cell=Math.min(34,Math.max(20,heightSafe()/N)),ox=cx()+15-cell*N/2,oy=72;c.fill(ox-3,oy-3,ox+N*cell+3,oy+N*cell+3,0xFF173A5C);for(int y=0;y<N;y++)for(int x=0;x<N;x++){int color=shots[x][y]?(enemyShips[x][y]?0xFFE74C3C:0xFF777F86):0xFF2D6EA3;c.fill(ox+x*cell+1,oy+y*cell+1,ox+(x+1)*cell-1,oy+(y+1)*cell-1,color);}c.drawTextWithShadow(textRenderer,net.minecraft.text.Text.literal("Enemy ships: "+enemyShipsLeft+"  •  Your ships: "+shipsLeft),ox,oy+N*cell+10,0xFFFFFFFF);}
    @Override public void keyPressed(int key,int scan,int modifiers){if(key==GLFW.GLFW_KEY_R)begin();}
}
