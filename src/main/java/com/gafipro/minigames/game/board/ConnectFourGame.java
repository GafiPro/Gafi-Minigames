package com.gafipro.minigames.game.board;

import com.gafipro.minigames.game.BaseGame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.concurrent.ThreadLocalRandom;

public final class ConnectFourGame extends BaseGame {
    private final int[][] board=new int[6][7];
    private boolean player=true;
    private String status="Your turn — click a column";
    @Override public String id(){return "connect_four";}
    @Override public String title(){return "Connect Four";}
    @Override public String category(){return "Board";}
    @Override public void start(){for(int r=0;r<6;r++)for(int c=0;c<7;c++)board[r][c]=0;player=true;status="Your turn — click a column";}
    @Override public void tick(){if(!finished&&!player){int col=aiColumn();drop(col,2);if(endFor(2)){finish(0);status="AI wins";}else if(full()){finish(1);status="Draw";}else{player=true;status="Your turn";}}}
    private int aiColumn(){for(int c=0;c<7;c++)if(valid(c)&&wouldWin(c,2))return c;for(int c=0;c<7;c++)if(valid(c)&&wouldWin(c,1))return c;int[] pref={3,2,4,1,5,0,6};for(int c:pref)if(valid(c))return c;return ThreadLocalRandom.current().nextInt(7);}
    private boolean wouldWin(int col,int p){int row=dropPreview(col,p);if(row<0)return false;boolean w=win(row,col,p);board[row][col]=0;return w;}
    private int dropPreview(int col,int p){for(int r=5;r>=0;r--)if(board[r][col]==0){board[r][col]=p;return r;}return -1;}
    private boolean drop(int col,int p){return dropPreview(col,p)>=0;}
    private boolean valid(int c){return c>=0&&c<7&&board[0][c]==0;}
    private boolean win(int r,int c,int p){return count(r,c,1,0,p)+count(r,c,-1,0,p)-1>=4||count(r,c,0,1,p)+count(r,c,0,-1,p)-1>=4||count(r,c,1,1,p)+count(r,c,-1,-1,p)-1>=4||count(r,c,1,-1,p)+count(r,c,-1,1,p)-1>=4;}
    private int count(int r,int c,int dr,int dc,int p){int n=0;while(r>=0&&r<6&&c>=0&&c<7&&board[r][c]==p){n++;r+=dr;c+=dc;}return n;}
    private boolean endFor(int p){for(int r=0;r<6;r++)for(int c=0;c<7;c++)if(board[r][c]==p&&win(r,c,p))return true;return false;}
    private boolean full(){for(int c=0;c<7;c++)if(board[0][c]==0)return false;return true;}
    @Override public void render(DrawContext c,int mx,int my,float delta){var mc=MinecraftClient.getInstance();var tr=mc.textRenderer;int cx=mc.getWindow().getScaledWidth()/2;drawHeader(c,"CONNECT FOUR",status);int cell=32,sx=cx-cell*7/2,sy=72;c.fill(sx-4,sy-4,sx+cell*7+4,sy+cell*6+4,0xFF345A9C);for(int r=0;r<6;r++)for(int col=0;col<7;col++){int x=sx+col*cell+4,y=sy+r*cell+4;int color=board[r][col]==1?0xFFFF5555:board[r][col]==2?0xFFFFFF55:0xFF1C2430;c.fill(x+3,y+3,x+cell-3,y+cell-3,color);}for(int col=0;col<7;col++)c.drawCenteredTextWithShadow(tr,Text.literal(""+(col+1)),sx+col*cell+cell/2,sy+cell*6+8,0xFFAAAAAA);}
    @Override public boolean mouseClicked(double mx,double my,int button){if(button!=0||finished||!player)return true;int cx=MinecraftClient.getInstance().getWindow().getScaledWidth()/2,cell=32,sx=cx-cell*7/2,sy=72;for(int col=0;col<7;col++){if(inside(mx,my,sx+col*cell,sy,cell,cell*6)){if(drop(col,1)){if(endFor(1)){finish(1);status="You win!";}else if(full()){finish(1);status="Draw";}else{player=false;status="AI thinking...";}}return true;}}return true;}
}
