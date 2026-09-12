package com.gafipro.minigames.game.puzzle;

import com.gafipro.minigames.game.BaseGame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class MemoryMatchGame extends BaseGame {
    private final int[] cards=new int[16];
    private final boolean[] revealed=new boolean[16];
    private int first=-1,second=-1,moves,matches;
    private int flipDelay;
    private long start;
    private final int[] symbols={1,2,3,4,5,6,7,8};
    @Override public String id(){return "memory_match";}
    @Override public String title(){return "Memory Match";}
    @Override public String category(){return "Puzzle";}
    @Override public void start(){List<Integer>v=new ArrayList<>();for(int s:symbols){v.add(s);v.add(s);}Collections.shuffle(v);for(int i=0;i<16;i++){cards[i]=v.get(i);revealed[i]=false;}first=second=-1;moves=matches=0;flipDelay=0;start=System.currentTimeMillis();}
    @Override public void tick(){if(flipDelay>0&&--flipDelay==0){if(first>=0&&second>=0&&cards[first]!=cards[second]){revealed[first]=revealed[second]=false;}first=second=-1;}}
    private String symbol(int v){return switch(v){case 1->"●";case 2->"■";case 3->"▲";case 4->"◆";case 5->"★";case 6->"+";case 7->"☀";default->"☘";};}
    @Override public void render(DrawContext c,int mx,int my,float delta){var mc=MinecraftClient.getInstance();var tr=mc.textRenderer;int cx=mc.getWindow().getScaledWidth()/2;drawHeader(c,"MEMORY MATCH","Match all pairs • Moves: "+moves+" • Pairs: "+matches+"/8");int cell=38,sx=cx-cell*2,sy=72;for(int i=0;i<16;i++){int x=sx+(i%4)*cell,y=sy+(i/4)*cell;boolean r=revealed[i];c.fill(x+2,y+2,x+cell-2,y+cell-2,r?0xFF315A69:0xFF343A40);String t=r?symbol(cards[i]):"?";c.drawCenteredTextWithShadow(tr,Text.literal(t).formatted(Formatting.BOLD),x+cell/2,y+10,r?0xFFFFFFFF:0xFF888888);} }
    @Override public boolean mouseClicked(double mx,double my,int button){if(button!=0||flipDelay>0)return true;int cx=MinecraftClient.getInstance().getWindow().getScaledWidth()/2,cell=38,sx=cx-cell*2,sy=72;if(mx<sx||mx>=sx+cell*4||my<sy||my>=sy+cell*4)return true;int i=(int)((my-sy)/cell)*4+(int)((mx-sx)/cell);if(revealed[i])return true;revealed[i]=true;if(first<0){first=i;}else{second=i;moves++;if(cards[first]==cards[second]){matches++;first=second=-1;if(matches==8){score=Math.max(1,1000-moves*20);finished=true;}}else flipDelay=12;}return true;}
}
