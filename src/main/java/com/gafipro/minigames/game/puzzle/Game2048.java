package com.gafipro.minigames.game.puzzle;

import com.gafipro.minigames.game.BaseGame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.concurrent.ThreadLocalRandom;

public final class Game2048 extends BaseGame {
    private final int[][] a=new int[4][4];
    private int points;
    private String message="Arrow keys / WASD";
    @Override public String id(){return "2048";}
    @Override public String title(){return "2048";}
    @Override public String category(){return "Puzzle";}
    @Override public void start(){for(int[]r:a)java.util.Arrays.fill(r,0);points=0;add();add();}
    private void add(){int count=0;for(int[]r:a)for(int v:r)if(v==0)count++;if(count==0)return;int n=ThreadLocalRandom.current().nextInt(count);for(int r=0;r<4;r++)for(int c=0;c<4;c++)if(a[r][c]==0){if(n--==0){a[r][c]=ThreadLocalRandom.current().nextInt(10)==0?4:2;return;}}}
    private boolean move(int dr,int dc){boolean changed=false;for(int line=0;line<4;line++){int[] vals=new int[4];int k=0;for(int i=0;i<4;i++){int r=dr!=0?(dr<0?i:3-i):line;int c=dc!=0?(dc<0?i:3-i):i;int v=a[r][c];if(v!=0)vals[k++]=v;}for(int i=0;i<4;i++){int v=0;if(i<k)v=vals[i];int r=dr!=0?(dr<0?i:3-i):line;int c=dc!=0?(dc<0?i:3-i):i;if(a[r][c]!=v){a[r][c]=v;changed=true;}}}return changed;}
    private boolean collapse(int dr,int dc){boolean changed=false;for(int line=0;line<4;line++){for(int i=0;i<3;i++){int r1=dr!=0?(dr<0?i:3-i):line,c1=dc!=0?(dc<0?i:3-i):i;int r2=dr!=0?(dr<0?i+1:2-i):line,c2=dc!=0?(dc<0?i+1:2-i):i+1;if(a[r1][c1]!=0&&a[r1][c1]==a[r2][c2]){a[r1][c1]*=2;points+=a[r1][c1];a[r2][c2]=0;changed=true;}}}return changed;}
    private void play(int dr,int dc){boolean c1=move(dr,dc),c2=collapse(dr,dc);move(dr,dc);if(c1||c2)add();if(!canMove()){finish(points);message="No moves left";}}
    private boolean canMove(){for(int r=0;r<4;r++)for(int c=0;c<4;c++){if(a[r][c]==0)return true;if(r<3&&a[r][c]==a[r+1][c])return true;if(c<3&&a[r][c]==a[r][c+1])return true;}return false;}
    @Override public void render(DrawContext c,int mx,int my,float delta){var mc=MinecraftClient.getInstance();var tr=mc.textRenderer;int cx=mc.getWindow().getScaledWidth()/2;drawHeader(c,"2048","Score: "+points+" • "+message);int cell=42,sx=cx-cell*2,sy=70;c.fill(sx-5,sy-5,sx+cell*4+5,sy+cell*4+5,0xFF353535);for(int r=0;r<4;r++)for(int col=0;col<4;col++){int x=sx+col*cell,y=sy+r*cell;c.fill(x+2,y+2,x+cell-2,y+cell-2,0xFFB7A99A);if(a[r][col]>0)c.drawCenteredTextWithShadow(tr,Text.literal(""+a[r][col]).formatted(Formatting.BOLD),x+cell/2,y+13,0xFFFFFFFF);}}
    @Override public void keyPressed(int key,int scan,int mod){switch(key){case 265,87->play(-1,0);case 264,83->play(1,0);case 263,65->play(0,-1);case 262,68->play(0,1);}}
}
