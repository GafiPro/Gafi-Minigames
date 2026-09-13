package com.gafipro.minigames.game.endless;

import com.gafipro.minigames.game.BaseGame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import java.util.Arrays;

/** Real Tetris-style falling blocks with all seven tetrominoes, rotation, line clears and levels. */
public final class TetrisGame extends BaseGame {
    private static final int W=10,H=20;
    private static final int[][][] SHAPES={{{1,1,1,1}},{{1,1},{1,1}},{{0,1,0},{1,1,1}},{{1,0,0},{1,1,1}},{{0,0,1},{1,1,1}},{{0,1,1},{1,1,0}},{{1,1,0},{0,1,1}}};
    private final int[][] board=new int[H][W];
    private int[][] piece; private int px,py,type,lines; private long nextDrop;
    @Override public String id(){return "falling_blocks";} @Override public String title(){return "Falling Blocks";} @Override public String category(){return "Endless";}
    @Override public void start(){for(int[]r:board)Arrays.fill(r,0);score=0;lines=0;metrics.level(1);spawn();}
    private int delay(){return Math.max(70,800-(metrics.level()-1)*65);}
    private static int[][] copy(int[][]s){int[][]o=new int[s.length][];for(int y=0;y<s.length;y++)o[y]=Arrays.copyOf(s[y],s[y].length);return o;}
    private void spawn(){type=random.nextInt(SHAPES.length);piece=copy(SHAPES[type]);px=(W-piece[0].length)/2;py=0;nextDrop=System.nanoTime()+delay()*1_000_000L;if(collides(px,py,piece))finish(score);}
    private boolean collides(int x,int y,int[][]s){for(int sy=0;sy<s.length;sy++)for(int sx=0;sx<s[sy].length;sx++)if(s[sy][sx]!=0){int bx=x+sx,by=y+sy;if(bx<0||bx>=W||by>=H)return true;if(by>=0&&board[by][bx]!=0)return true;}return false;}
    private void softDrop(){if(!collides(px,py+1,piece))py++;else lock();}
    private void lock(){for(int sy=0;sy<piece.length;sy++)for(int sx=0;sx<piece[sy].length;sx++)if(piece[sy][sx]!=0&&py+sy>=0)board[py+sy][px+sx]=type+1;int cleared=0;for(int y=H-1;y>=0;y--){boolean full=true;for(int x=0;x<W;x++)if(board[y][x]==0){full=false;break;}if(full){cleared++;for(int r=y;r>0;r--)System.arraycopy(board[r-1],0,board[r],0,W);Arrays.fill(board[0],0);y++;}}if(cleared>0){score+=(cleared==1?100:cleared==2?300:cleared==3?500:800)*metrics.level();lines+=cleared;metrics.level(1+lines/10);}spawn();}
    private void move(int dx){if(!collides(px+dx,py,piece))px+=dx;}
    private void rotate(){int h=piece.length,w=piece[0].length;int[][]r=new int[w][h];for(int y=0;y<h;y++)for(int x=0;x<w;x++)r[x][h-1-y]=piece[y][x];for(int k:new int[]{0,-1,1,-2,2})if(!collides(px+k,py,r)){px+=k;piece=r;return;}}
    @Override public void tick(){super.tick();if(finished||System.nanoTime()<nextDrop)return;softDrop();nextDrop=System.nanoTime()+delay()*1_000_000L;}
    @Override public void keyPressed(int k,int s,int m){if(k==GLFW.GLFW_KEY_R){begin();return;}if(finished)return;switch(k){case GLFW.GLFW_KEY_LEFT,GLFW.GLFW_KEY_A->move(-1);case GLFW.GLFW_KEY_RIGHT,GLFW.GLFW_KEY_D->move(1);case GLFW.GLFW_KEY_DOWN,GLFW.GLFW_KEY_S->{softDrop();nextDrop=System.nanoTime()+delay()*1_000_000L;}case GLFW.GLFW_KEY_UP,GLFW.GLFW_KEY_X->rotate();case GLFW.GLFW_KEY_SPACE->{while(!collides(px,py+1,piece))py++;lock();}default->{}}}
    @Override public void render(DrawContext c,int mx,int my,float d){MinecraftClient mc=MinecraftClient.getInstance();int h=mc.getWindow().getScaledHeight(),w=mc.getWindow().getScaledWidth();drawHeader(c,"FALLING BLOCKS","Arrows / WASD • Space: hard drop • Level: "+metrics.level()+" • Score: "+score);int cell=Math.max(6,Math.min(18,Math.min((h-82)/H,(w-24)/W))),ox=cx()-W*cell/2,oy=68;c.fill(ox-3,oy-3,ox+W*cell+3,oy+H*cell+3,0xFF101418);for(int y=0;y<H;y++)for(int x=0;x<W;x++){int v=board[y][x];c.fill(ox+x*cell+1,oy+y*cell+1,ox+(x+1)*cell-1,oy+(y+1)*cell-1,v==0?0xFF252B31:color(v));}for(int sy=0;sy<piece.length;sy++)for(int sx=0;sx<piece[sy].length;sx++)if(piece[sy][sx]!=0&&py+sy>=0)c.fill(ox+(px+sx)*cell+1,oy+(py+sy)*cell+1,ox+(px+sx+1)*cell-1,oy+(py+sy+1)*cell-1,color(type+1));}
    private int color(int v){return switch(v){case 1->0xFF55E6FF;case 2->0xFFFFD84D;case 3->0xFFB86CFF;case 4->0xFFFF9B45;case 5->0xFF5AA9FF;case 6->0xFF58D68D;case 7->0xFFE5679E;default->0xFFFFFFFF;};}
    @Override public boolean mouseClicked(double x,double y,int b){return false;}
}
