package com.gafipro.minigames.game.puzzle;

import com.gafipro.minigames.game.BaseGame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

/** Solvable Lights Out generated from random legal presses. */
public final class LightsOutGame extends BaseGame {
    private static final int SIZE = 5;
    private final boolean[][] lights = new boolean[SIZE][SIZE];

    @Override public String id() { return "lights_out"; }
    @Override public String title() { return "Lights Out"; }
    @Override public String category() { return "Puzzle"; }

    @Override public void start() {
        do {
            for (int x = 0; x < SIZE; x++) java.util.Arrays.fill(lights[x], false);
            int presses = 10 + random.nextInt(15);
            for (int i = 0; i < presses; i++) toggle(random.nextInt(SIZE), random.nextInt(SIZE));
        } while (solved());
        status = "Turn every light off • R restart";
    }

    private void toggle(int x, int y) {
        int[][] dirs = {{0,0},{1,0},{-1,0},{0,1},{0,-1}};
        for (int[] d : dirs) {
            int nx=x+d[0], ny=y+d[1];
            if(nx>=0&&ny>=0&&nx<SIZE&&ny<SIZE) lights[nx][ny]=!lights[nx][ny];
        }
    }
    private boolean solved(){for(boolean[] row:lights)for(boolean v:row)if(v)return false;return true;}

    @Override public boolean mouseClicked(double mx,double my,int button){
        if(button!=0||finished)return true;
        int cell=Math.min(52,Math.max(30,(MinecraftClient.getInstance().getWindow().getScaledHeight()-145)/SIZE));
        int ox=cx()-cell*SIZE/2,oy=82,x=(int)((mx-ox)/cell),y=(int)((my-oy)/cell);
        if(x<0||y<0||x>=SIZE||y>=SIZE)return true;
        toggle(x,y);markMove();status="Moves: "+metrics.moves()+" • Turn every light off";
        if(solved())finishWin(Math.max(100,1200-metrics.moves()*20));
        return true;
    }

    @Override public void render(DrawContext c,int mx,int my,float delta){
        MinecraftClient mc=MinecraftClient.getInstance();drawHeader(c,"LIGHTS OUT",status);
        int cell=Math.min(52,Math.max(30,(mc.getWindow().getScaledHeight()-145)/SIZE));
        int ox=cx()-cell*SIZE/2,oy=82;
        for(int y=0;y<SIZE;y++)for(int x=0;x<SIZE;x++)c.fill(ox+x*cell+1,oy+y*cell+1,ox+x*cell+cell-2,oy+y*cell+cell-2,lights[x][y]?0xFFF1C40F:0xFF3B424A);
    }
    @Override public void keyPressed(int key,int scan,int modifiers){if(key==GLFW.GLFW_KEY_R)begin();}
}
