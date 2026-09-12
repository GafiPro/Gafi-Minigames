package com.gafipro.minigames.game.puzzle;

import com.gafipro.minigames.game.BaseGame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/** Growing position-sequence memory game with timed reveal phases. */
public final class SequenceMemoryGame extends BaseGame {
    private static final int SIZE=4;
    private final List<Integer> sequence=new ArrayList<>();
    private int shown,inputIndex;
    private boolean showing;
    private long nextActionNanos;
    private int flash=-1,round;

    @Override public String id(){return "sequence_memory";}
    @Override public String title(){return "Sequence Memory";}
    @Override public String category(){return "Puzzle";}

    @Override public void start(){sequence.clear();round=1;sequence.add(random.nextInt(16));beginShow();status="Memorize the sequence.";}
    private void beginShow(){shown=0;inputIndex=0;showing=true;flash=-1;nextActionNanos=System.nanoTime()+350_000_000L;}

    @Override public void tick(){super.tick();if(finished||!showing)return;long now=System.nanoTime();if(now<nextActionNanos)return;if(shown<sequence.size()){flash=sequence.get(shown++);nextActionNanos=now+450_000_000L;}else{flash=-1;showing=false;status="Repeat the sequence.";}}

    private void input(int value){if(showing||finished)return;if(value!=sequence.get(inputIndex)){finish(score);return;}inputIndex++;markMove();if(inputIndex==sequence.size()){score+=sequence.size()*100;if(sequence.size()>=12){finishWin(score);return;}sequence.add(random.nextInt(16));round++;beginShow();}}

    @Override public boolean mouseClicked(double mx,double my,int button){if(button!=0||finished)return true;int cell=Math.min(50,Math.max(28,(MinecraftClient.getInstance().getWindow().getScaledHeight()-150)/SIZE));int ox=cx()-SIZE*cell/2,oy=82,x=(int)((mx-ox)/cell),y=(int)((my-oy)/cell);if(x<0||y<0||x>=SIZE||y>=SIZE)return true;input(y*SIZE+x);return true;}
    @Override public void render(DrawContext c,int mx,int my,float delta){MinecraftClient mc=MinecraftClient.getInstance();drawHeader(c,"SEQUENCE MEMORY",status+" • Round: "+round);int cell=Math.min(50,Math.max(28,(mc.getWindow().getScaledHeight()-150)/SIZE));int ox=cx()-SIZE*cell/2,oy=82;for(int y=0;y<SIZE;y++)for(int x=0;x<SIZE;x++){int i=y*SIZE+x;c.fill(ox+x*cell+1,oy+y*cell+1,ox+x*cell+cell-2,oy+y*cell+cell-2,flash==i?0xFFFFFFFF:0xFF3B424A);}}
    @Override public void keyPressed(int key,int scan,int modifiers){if(key==GLFW.GLFW_KEY_R)begin();else if(key>=GLFW.GLFW_KEY_1&&key<=GLFW.GLFW_KEY_9)input(key-GLFW.GLFW_KEY_1);}
}
