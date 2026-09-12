package com.gafipro.minigames.game.puzzle;

import com.gafipro.minigames.game.BaseGame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/** Nonogram generated from a hidden binary picture with matching row/column clues. */
public final class NonogramGame extends BaseGame {
    private static final int SIZE=10;
    private final boolean[][] solution=new boolean[SIZE][SIZE];
    private final boolean[][] filled=new boolean[SIZE][SIZE];
    private final boolean[][] markedEmpty=new boolean[SIZE][SIZE];
    private int mistakes;

    @Override public String id(){return "nonogram";}
    @Override public String title(){return "Nonogram";}
    @Override public String category(){return "Puzzle";}

    @Override public void start(){
        do { for(int x=0;x<SIZE;x++)for(int y=0;y<SIZE;y++)solution[x][y]=random.nextDouble()<0.42; } while(!hasAny());
        for(int x=0;x<SIZE;x++)for(int y=0;y<SIZE;y++){filled[x][y]=false;markedEmpty[x][y]=false;}
        mistakes=0;status="Fill the picture from the clues • Left click fills, right click marks empty";
    }
    private boolean hasAny(){for(boolean[] r:solution)for(boolean v:r)if(v)return true;return false;}
    private int[] clues(boolean row,int index){List<Integer> out=new ArrayList<>();int run=0;for(int i=0;i<SIZE;i++){boolean v=row?solution[i][index]:solution[index][i];if(v)run++;else if(run>0){out.add(run);run=0;}}if(run>0)out.add(run);if(out.isEmpty())out.add(0);return out.stream().mapToInt(Integer::intValue).toArray();}
    private boolean solved(){for(int x=0;x<SIZE;x++)for(int y=0;y<SIZE;y++)if(filled[x][y]!=solution[x][y])return false;return true;}

    @Override public boolean mouseClicked(double mx,double my,int button){
        if(finished)return true;
        int cell=Math.min(30,Math.max(18,(MinecraftClient.getInstance().getWindow().getScaledWidth()-130)/(SIZE+4)));
        int ox=cx()-SIZE*cell/2,oy=82,x=(int)((mx-ox)/cell),y=(int)((my-oy)/cell);
        if(x<0||y<0||x>=SIZE||y>=SIZE)return true;
        if(button==0)filled[x][y]=!filled[x][y]; else if(button==1)markedEmpty[x][y]=!markedEmpty[x][y]; else return true;
        markMove();
        if(filled[x][y]!=solution[x][y]){mistakes++;metrics.mistakes(mistakes);status="Mistake: check the clues • Mistakes: "+mistakes;}else status="Use the row and column clues • Mistakes: "+mistakes;
        if(solved())finishWin(Math.max(100,5000-mistakes*100));
        return true;
    }
    @Override public void render(DrawContext c,int mx,int my,float delta){
        MinecraftClient mc=MinecraftClient.getInstance();drawHeader(c,"NONOGRAM",status);
        int cell=Math.min(30,Math.max(18,(mc.getWindow().getScaledWidth()-130)/(SIZE+4)));int ox=cx()-SIZE*cell/2,oy=82;
        for(int y=0;y<SIZE;y++)for(int x=0;x<SIZE;x++){
            int tile=filled[x][y]?0xFF4B6A88:markedEmpty[x][y]?0xFF6A5555:0xFF3B424A;
            c.fill(ox+x*cell+1,oy+y*cell+1,ox+x*cell+cell-2,oy+y*cell+cell-2,tile);
        }
        for(int i=0;i<SIZE;i++){c.drawTextWithShadow(mc.textRenderer,net.minecraft.text.Text.literal(format(clues(false,i))),ox+i*cell+2,oy-18,0xFFFFFFFF);c.drawTextWithShadow(mc.textRenderer,net.minecraft.text.Text.literal(format(clues(true,i))),ox-SIZE*cell/2-cell*2,oy+i*cell+6,0xFFFFFFFF);}
    }
    private String format(int[] a){StringBuilder s=new StringBuilder();for(int v:a){if(s.length()>0)s.append(' ');s.append(v);}return s.toString();}
    @Override public void keyPressed(int key,int scan,int modifiers){if(key==GLFW.GLFW_KEY_R)begin();}
}
