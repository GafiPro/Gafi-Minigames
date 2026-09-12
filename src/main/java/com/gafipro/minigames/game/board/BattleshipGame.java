package com.gafipro.minigames.game.board;

import com.gafipro.minigames.game.BaseGame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Local Battleship with player placement, hidden enemy fleet, hits, misses and sunk ships. */
public final class BattleshipGame extends BaseGame {
    private static final int N=10;
    private static final int[] FLEET={5,4,3,3,2};
    private final boolean[][] enemyShips=new boolean[N][N], playerShips=new boolean[N][N], shots=new boolean[N][N], enemyShots=new boolean[N][N];
    private final int[][] enemyShipId=new int[N][N], playerShipId=new int[N][N];
    private final int[] enemyRemaining=new int[FLEET.length], playerRemaining=new int[FLEET.length];
    private int shipsLeft,enemyShipsLeft,placementIndex,hits; private boolean placing,horizontal;

    @Override public String id(){return "battleship";}
    @Override public String title(){return "Battleship";}
    @Override public String category(){return "Board";}
    @Override public void start(){clear();for(int i=0;i<FLEET.length;i++){enemyRemaining[i]=FLEET[i];placeRandom(enemyShips,enemyShipId,i,FLEET[i]);}for(int i=0;i<FLEET.length;i++)playerRemaining[i]=FLEET[i];shipsLeft=FLEET.length;enemyShipsLeft=FLEET.length;placementIndex=0;hits=0;placing=true;horizontal=true;status="Place your Carrier (5). Press Q to rotate.";}
    private void clear(){for(int x=0;x<N;x++)for(int y=0;y<N;y++){enemyShips[x][y]=playerShips[x][y]=shots[x][y]=enemyShots[x][y]=false;enemyShipId[x][y]=playerShipId[x][y]=-1;}}
    private void placeRandom(boolean[][]grid,int[][]ids,int id,int len){for(int t=0;t<5000;t++){int x=random.nextInt(N),y=random.nextInt(N),dx=random.nextBoolean()?1:0,dy=dx==0?1:0;if(x+dx*(len-1)>=N||y+dy*(len-1)>=N)continue;boolean ok=true;for(int i=0;i<len;i++)if(grid[x+dx*i][y+dy*i]){ok=false;break;}if(!ok)continue;for(int i=0;i<len;i++){int a=x+dx*i,b=y+dy*i;grid[a][b]=true;ids[a][b]=id;}return;}}
    private boolean placePlayer(int x,int y){int len=FLEET[placementIndex],dx=horizontal?1:0,dy=horizontal?0:1;if(x+dx*(len-1)>=N||y+dy*(len-1)>=N)return false;for(int i=0;i<len;i++)if(playerShips[x+dx*i][y+dy*i])return false;for(int i=0;i<len;i++){int a=x+dx*i,b=y+dy*i;playerShips[a][b]=true;playerShipId[a][b]=placementIndex;}placementIndex++;if(placementIndex==FLEET.length){placing=false;status="Fleet ready. Fire on the enemy grid.";}else status="Place your "+shipName(placementIndex)+" ("+FLEET[placementIndex]+"). Press Q to rotate.";return true;}
    private String shipName(int id){return switch(id){case 0->"Carrier";case 1->"Battleship";case 2->"Cruiser";case 3->"Submarine";default->"Destroyer";};}
    private void enemyTurn(){for(int tries=0;tries<300;tries++){int x=random.nextInt(N),y=random.nextInt(N);if(enemyShots[x][y])continue;enemyShots[x][y]=true;if(playerShips[x][y]){int id=playerShipId[x][y];if(--playerRemaining[id]==0){shipsLeft--;status="The AI sank your "+shipName(id)+".";}else status="The AI hit your fleet.";}else status="The AI missed.";return;}}
    private int heightSafe(){return Math.max(200,MinecraftClient.getInstance().getWindow().getScaledHeight()-95);}
    @Override public boolean mouseClicked(double mx,double my,int button){if(button!=0||finished)return true;int cell=Math.min(34,Math.max(20,heightSafe()/N)),ox=cx()+15-cell*N/2,oy=72,x=(int)((mx-ox)/cell),y=(int)((my-oy)/cell);if(x<0||y<0||x>=N||y>=N)return true;if(placing)return placePlayer(x,y);if(shots[x][y])return true;shots[x][y]=true;if(enemyShips[x][y]){int id=enemyShipId[x][y];hits++;enemyRemaining[id]--;score+=100;if(enemyRemaining[id]==0){enemyShipsLeft--;status="HIT — "+shipName(id)+" sunk.";}else status="HIT.";}else status="MISS.";if(enemyShipsLeft==0){finishWin(score+1000);return true;}enemyTurn();if(shipsLeft==0)finish(score);return true;}
    @Override public void keyPressed(int key,int scan,int modifiers){if(key==GLFW.GLFW_KEY_R){begin();return;}if(placing&&key==GLFW.GLFW_KEY_Q){horizontal=!horizontal;status="Placement: "+(horizontal?"horizontal":"vertical")+".";}}
    @Override public void render(DrawContext c,int mx,int my,float d){String head=placing?"Place your fleet":"Enemy grid • Blue: unknown • Red: hit • Gray: miss";drawHeader(c,"BATTLESHIP",head);int cell=Math.min(34,Math.max(20,heightSafe()/N)),ox=cx()+15-cell*N/2,oy=72;c.fill(ox-3,oy-3,ox+N*cell+3,oy+N*cell+3,0xFF173A5C);for(int y=0;y<N;y++)for(int x=0;x<N;x++){int color;if(placing)color=playerShips[x][y]?0xFF8A9BA8:0xFF2D6EA3;else color=shots[x][y]?(enemyShips[x][y]?0xFFE74C3C:0xFF777F86):0xFF2D6EA3;c.fill(ox+x*cell+1,oy+y*cell+1,ox+(x+1)*cell-1,oy+(y+1)*cell-1,color);}c.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,Text.literal(placing?"Q: rotate • Click a cell to place":"Enemy ships: "+enemyShipsLeft+" • Your ships: "+shipsLeft),ox,oy+N*cell+10,0xFFFFFFFF);}
}
