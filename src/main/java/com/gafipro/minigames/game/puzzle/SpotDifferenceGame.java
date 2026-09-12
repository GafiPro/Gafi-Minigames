package com.gafipro.minigames.game.puzzle;

import com.gafipro.minigames.game.BaseGame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

/** Two generated grids with five real visual differences to locate. */
public final class SpotDifferenceGame extends BaseGame {
    private static final int SIZE=6,DIFFERENCES=5;
    private final int[][] left=new int[SIZE][SIZE],right=new int[SIZE][SIZE];
    private final Set<Integer> targets=new HashSet<>(),found=new HashSet<>();
    private long deadline;

    @Override public String id(){return "spot_difference";}
    @Override public String title(){return "Spot the Difference";}
    @Override public String category(){return "Puzzle";}

    @Override public void start(){
        targets.clear();found.clear();for(int y=0;y<SIZE;y++)for(int x=0;x<SIZE;x++){left[x][y]=random.nextInt(5);right[x][y]=left[x][y];}
        while(targets.size()<DIFFERENCES){int p=random.nextInt(SIZE*SIZE);targets.add(p);int x=p%SIZE,y=p/SIZE;right[x][y]=(right[x][y]+1+random.nextInt(4))%5;}
        deadline=System.nanoTime()+60_000_000_000L;status="Find five differences • 60 seconds";
    }
    @Override public void tick(){super.tick();if(!finished&&System.nanoTime()>=deadline)finish(score);}

    @Override public boolean mouseClicked(double mx,double my,int button){
        if(button!=0||finished)return true;int cell=Math.min(42,Math.max(24,(MinecraftClient.getInstance().getWindow().getScaledHeight()-150)/SIZE));int gap=35,ox1=cx()-SIZE*cell-gap/2,ox2=cx()+gap/2,oy=82;int x=(int)((mx-ox2)/cell),y=(int)((my-oy)/cell);if(x<0||y<0||x>=SIZE||y>=SIZE)return true;int p=y*SIZE+x;if(targets.contains(p)){if(found.add(p)){score+=200;markMove();}if(found.size()==DIFFERENCES)finishWin(score);}else score=Math.max(0,score-50);return true;
    }
    @Override public void render(DrawContext c,int mx,int my,float delta){
        MinecraftClient mc=MinecraftClient.getInstance();drawHeader(c,"SPOT THE DIFFERENCE","Differences: "+found.size()+"/"+DIFFERENCES);int cell=Math.min(42,Math.max(24,(mc.getWindow().getScaledHeight()-150)/SIZE));int gap=35,ox1=cx()-SIZE*cell-gap/2,ox2=cx()+gap/2,oy=82;c.drawCenteredTextWithShadow(mc.textRenderer,net.minecraft.text.Text.literal("ORIGINAL"),ox1+SIZE*cell/2,62,0xFFFFFFFF);c.drawCenteredTextWithShadow(mc.textRenderer,net.minecraft.text.Text.literal("CHANGED"),ox2+SIZE*cell/2,62,0xFFFFFFFF);for(int y=0;y<SIZE;y++)for(int x=0;x<SIZE;x++){drawCell(c,left[x][y],ox1+x*cell,oy+y*cell,cell);drawCell(c,right[x][y],ox2+x*cell,oy+y*cell,cell);if(found.contains(y*SIZE+x))c.fill(ox2+x*cell,oy+y*cell,ox2+x*cell+cell-2,oy+y*cell+cell-2,0x66FFFFFF);}}
    private void drawCell(DrawContext c,int v,int x,int y,int cell){int[][] colors={{0xFF2E86AB,0xFF56B870,0xFFE0A43C,0xFFE06C75,0xFF9B6CC2}};c.fill(x+1,y+1,x+cell-2,y+cell-2,colors[0][v]);if(v%2==0)c.fill(x+8,y+8,x+cell-9,y+cell-9,0x55FFFFFF);else c.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer,net.minecraft.text.Text.literal("◆"),x+cell/2,y+cell/2-5,0xFFFFFFFF);}
    @Override public void keyPressed(int key,int scan,int modifiers){if(key==GLFW.GLFW_KEY_R)begin();}
}
