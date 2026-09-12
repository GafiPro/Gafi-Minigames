package com.gafipro.minigames.game.puzzle;

import com.gafipro.minigames.game.BaseGame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class MinesweeperGame extends BaseGame {
    private final int w=9,h=9,mines=10;
    private final boolean[][] mine=new boolean[h][w], open=new boolean[h][w], flag=new boolean[h][w];
    private boolean started;
    private int opened;
    private long startTime;
    @Override public String id(){return "minesweeper";}
    @Override public String title(){return "Minesweeper";}
    @Override public String category(){return "Puzzle";}
    @Override public void start(){for(int y=0;y<h;y++)for(int x=0;x<w;x++){mine[y][x]=false;open[y][x]=false;flag[y][x]=false;}started=false;opened=0;score=0;}
    private void placeMines(int safeX,int safeY){int placed=0;while(placed<mines){int x=ThreadLocalRandom.current().nextInt(w),y=ThreadLocalRandom.current().nextInt(h);if((x==safeX&&y==safeY)||mine[y][x])continue;mine[y][x]=true;placed++;}startTime=System.currentTimeMillis();started=true;}
    private int adjacent(int x,int y){int n=0;for(int dy=-1;dy<=1;dy++)for(int dx=-1;dx<=1;dx++){if(dx==0&&dy==0)continue;int nx=x+dx,ny=y+dy;if(nx>=0&&nx<w&&ny>=0&&ny<h&&mine[ny][nx])n++;}return n;}
    private void reveal(int x,int y){if(x<0||x>=w||y<0||y>=h||open[y][x]||flag[y][x])return;open[y][x]=true;opened++;if(adjacent(x,y)==0){ArrayDeque<int[]>q=new ArrayDeque<>();q.add(new int[]{x,y});while(!q.isEmpty()){int[]p=q.remove();for(int dy=-1;dy<=1;dy++)for(int dx=-1;dx<=1;dx++){if(dx==0&&dy==0)continue;int nx=p[0]+dx,ny=p[1]+dy;if(nx<0||nx>=w||ny<0||ny>=h||open[ny][nx]||flag[ny][nx]||mine[ny][nx])continue;open[ny][nx]=true;opened++;if(adjacent(nx,ny)==0)q.add(new int[]{nx,ny});}}}}
    private boolean won(){return opened>=w*h-mines;}
    @Override public void render(DrawContext c,int mx,int my,float delta){var mc=MinecraftClient.getInstance();var tr=mc.textRenderer;int cx=mc.getWindow().getScaledWidth()/2;String timer=started?((System.currentTimeMillis()-startTime)/1000)+"s":"Ready";drawHeader(c,"MINESWEEPER",timer+" • Left click reveal • Right click flag");int cell=26,sx=cx-cell*w/2,sy=72;c.fill(sx-4,sy-4,sx+cell*w+4,sy+cell*h+4,0xFF5A5F66);for(int y=0;y<h;y++)for(int x=0;x<w;x++){int px=sx+x*cell,py=sy+y*cell;int color=open[y][x]?0xFFC7CBD1:0xFF3A4149;c.fill(px+1,py+1,px+cell-1,py+cell-1,color);String s=open[y][x]?mine[y][x]?"✹":String.valueOf(adjacent(x,y)):flag[y][x]?"⚑":"";if(!s.isEmpty())c.drawCenteredTextWithShadow(tr,Text.literal(s).formatted(Formatting.BOLD),px+cell/2,py+7,mine[y][x]?0xFFFF5555:0xFF222222);}
        c.drawCenteredTextWithShadow(tr,Text.literal("Safe cells: "+opened+"/"+(w*h-mines)),cx,sy+cell*h+9,0xFFFFFFFF);
    }
    @Override public boolean mouseClicked(double mx,double my,int button){int cx=MinecraftClient.getInstance().getWindow().getScaledWidth()/2,cell=26,sx=cx-cell*w/2,sy=72;if(mx<sx||mx>=sx+cell*w||my<sy||my>=sy+cell*h)return true;int x=(int)((mx-sx)/cell),y=(int)((my-sy)/cell);if(!started)placeMines(x,y);if(button==1){flag[y][x]=!flag[y][x];return true;}if(button==0&&!flag[y][x]){if(mine[y][x]){finished=true;score=0;return true;}reveal(x,y);if(won()){score=Math.max(1,100000-(int)((System.currentTimeMillis()-startTime)));finished=true;}}return true;}
}
