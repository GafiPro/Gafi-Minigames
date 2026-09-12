package com.gafipro.minigames.game.puzzle;

import com.gafipro.minigames.game.BaseGame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/** Match-3 with legal adjacent swaps, cascades, refills and dead-board recovery. */
public final class Match3Game extends BaseGame {
    private static final int SIZE=8, TYPES=6;
    private final int[][] grid=new int[SIZE][SIZE];
    private int selectedX=-1,selectedY=-1,movesLeft;

    @Override public String id(){return "match_3";}
    @Override public String title(){return "Match-3";}
    @Override public String category(){return "Puzzle";}

    @Override public void start(){
        movesLeft=30;selectedX=selectedY=-1;
        do { fillWithoutMatches(); } while(!hasPossibleMove());
        status="Swap adjacent tiles to make matches • Moves: 30";
    }

    private void fillWithoutMatches(){
        for(int x=0;x<SIZE;x++)for(int y=0;y<SIZE;y++){do{grid[x][y]=random.nextInt(TYPES);}while(createsMatch(x,y));}
    }
    private boolean createsMatch(int x,int y){
        return x>=2&&grid[x-1][y]==grid[x-2][y]&&grid[x-1][y]==grid[x][y]
                || y>=2&&grid[x][y-1]==grid[x][y-2]&&grid[x][y-1]==grid[x][y];
    }
    private boolean adjacent(int x1,int y1,int x2,int y2){return Math.abs(x1-x2)+Math.abs(y1-y2)==1;}
    private void swap(int x1,int y1,int x2,int y2){int t=grid[x1][y1];grid[x1][y1]=grid[x2][y2];grid[x2][y2]=t;}

    private boolean[][] findMatches(){
        boolean[][] remove=new boolean[SIZE][SIZE];
        for(int y=0;y<SIZE;y++)for(int x=0;x<SIZE;x++){
            if(x<SIZE-2&&grid[x][y]>=0&&grid[x][y]==grid[x+1][y]&&grid[x][y]==grid[x+2][y]){int v=grid[x][y];for(int k=x;k<SIZE&&grid[k][y]==v;k++)remove[k][y]=true;}
            if(y<SIZE-2&&grid[x][y]>=0&&grid[x][y]==grid[x][y+1]&&grid[x][y]==grid[x][y+2]){int v=grid[x][y];for(int k=y;k<SIZE&&grid[x][k]==v;k++)remove[x][k]=true;}
        }
        return remove;
    }

    private boolean resolve(){
        boolean cascaded=false;
        while(true){
            boolean[][] remove=findMatches();int count=0;
            for(int x=0;x<SIZE;x++)for(int y=0;y<SIZE;y++)if(remove[x][y]){grid[x][y]=-1;count++;}
            if(count==0)break;
            cascaded=true;score+=count*100;
            for(int x=0;x<SIZE;x++){
                int write=SIZE-1;
                for(int y=SIZE-1;y>=0;y--)if(grid[x][y]>=0)grid[x][write--]=grid[x][y];
                while(write>=0)grid[x][write--]=random.nextInt(TYPES);
            }
        }
        return cascaded;
    }

    private boolean hasPossibleMove(){
        for(int x=0;x<SIZE;x++)for(int y=0;y<SIZE;y++){
            if(x+1<SIZE&&createsMatchAfterSwap(x,y,x+1,y))return true;
            if(y+1<SIZE&&createsMatchAfterSwap(x,y,x,y+1))return true;
        }
        return false;
    }
    private boolean createsMatchAfterSwap(int x1,int y1,int x2,int y2){swap(x1,y1,x2,y2);boolean result=false;boolean[][] m=findMatches();outer:for(boolean[] row:m)for(boolean v:row)if(v){result=true;break outer;}swap(x1,y1,x2,y2);return result;}
    private void reshuffle(){do{fillWithoutMatches();}while(!hasPossibleMove());}

    @Override public boolean mouseClicked(double mx,double my,int button){
        if(button!=0||finished)return true;
        int cell=Math.min(44,Math.max(26,(MinecraftClient.getInstance().getWindow().getScaledHeight()-135)/SIZE));
        int ox=cx()-SIZE*cell/2,oy=80,x=(int)((mx-ox)/cell),y=(int)((my-oy)/cell);
        if(x<0||y<0||x>=SIZE||y>=SIZE)return true;
        if(selectedX<0){selectedX=x;selectedY=y;return true;}
        if(!adjacent(selectedX,selectedY,x,y)){selectedX=x;selectedY=y;return true;}
        if(!createsMatchAfterSwap(selectedX,selectedY,x,y)){selectedX=selectedY=-1;return true;}
        swap(selectedX,selectedY,x,y);selectedX=selectedY=-1;markMove();movesLeft--;resolve();
        if(movesLeft<=0){finish(score);return true;}
        if(!hasPossibleMove())reshuffle();
        status="Swap adjacent tiles • Moves left: "+movesLeft;
        return true;
    }

    @Override public void render(DrawContext c,int mx,int my,float delta){
        MinecraftClient mc=MinecraftClient.getInstance();drawHeader(c,"MATCH-3",status+" • Score: "+score);
        int cell=Math.min(44,Math.max(26,(mc.getWindow().getScaledHeight()-135)/SIZE));int ox=cx()-SIZE*cell/2,oy=80;
        for(int y=0;y<SIZE;y++)for(int x=0;x<SIZE;x++){int v=grid[x][y];int r=(v*31+70)&255,g=(v*47+80)&255,b=(v*61+90)&255;int color=0xFF000000|(r<<16)|(g<<8)|b;c.fill(ox+x*cell+1,oy+y*cell+1,ox+x*cell+cell-2,oy+y*cell+cell-2,color);if(x==selectedX&&y==selectedY)c.fill(ox+x*cell,oy+y*cell,ox+x*cell+cell,oy+y*cell+cell,0xFFFFFFFF);}
    }
    @Override public void keyPressed(int key,int scan,int modifiers){if(key==GLFW.GLFW_KEY_R)begin();}
}
