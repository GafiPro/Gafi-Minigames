package com.gafipro.minigames.game.board;

import com.gafipro.minigames.game.BaseGame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.concurrent.ThreadLocalRandom;

public final class TicTacToeGame extends BaseGame {
    private final int[] board = new int[9];
    private boolean playerTurn = true;
    private String message = "Your turn";

    @Override public String id(){return "tic_tac_toe";}
    @Override public String title(){return "Tic-Tac-Toe";}
    @Override public String category(){return "Board";}
    @Override public void start(){for(int i=0;i<9;i++)board[i]=0;playerTurn=true;message="Your turn";}
    @Override public void tick(){if(!finished && !playerTurn){aiMove();}}

    private void aiMove(){
        int move=bestMove();
        if(move<0){finish(1);message="Draw";return;}
        board[move]=-1;playerTurn=true;checkEnd();
    }
    private int bestMove(){
        for(int i=0;i<9;i++)if(board[i]==0){board[i]= -1;if(wins(-1)){board[i]=0;return i;}board[i]=0;}
        for(int i=0;i<9;i++)if(board[i]==0){board[i]= 1;if(wins(1)){board[i]=0;return i;}board[i]=0;}
        if(board[4]==0)return 4;
        int[] corners={0,2,6,8};for(int c:corners)if(board[c]==0)return c;
        int[] free=new int[9];int n=0;for(int i=0;i<9;i++)if(board[i]==0)free[n++]=i;return n==0?-1:free[ThreadLocalRandom.current().nextInt(n)];
    }
    private boolean wins(int p){int[][] lines={{0,1,2},{3,4,5},{6,7,8},{0,3,6},{1,4,7},{2,5,8},{0,4,8},{2,4,6}};for(int[]l:lines)if(board[l[0]]==p&&board[l[1]]==p&&board[l[2]]==p)return true;return false;}
    private void checkEnd(){if(wins(1)){score=1;finished=true;message="You win!";return;}if(wins(-1)){score=0;finished=true;message="AI wins";return;}boolean full=true;for(int v:board)if(v==0){full=false;break;}if(full){score=1;finished=true;message="Draw";}}

    @Override public void render(DrawContext c,int mx,int my,float delta){
        var mc=MinecraftClient.getInstance();var tr=mc.textRenderer;int cx=mc.getWindow().getScaledWidth()/2;
        drawHeader(c,"TIC-TAC-TOE",message+" • You are X");
        int cell=48,sx=cx-cell*3/2,sy=80;
        for(int i=0;i<9;i++){int col=i%3,row=i/3,x=sx+col*cell,y=sy+row*cell;c.fill(x+2,y+2,x+cell-2,y+cell-2,0xFF30363D);String s=board[i]==1?"X":board[i]==-1?"O":"";int color=board[i]==1?0xFF55FFFF:0xFFFF7777; if(!s.isEmpty())c.drawCenteredTextWithShadow(tr,Text.literal(s).formatted(Formatting.BOLD),x+cell/2,y+17,color);}
        c.drawCenteredTextWithShadow(tr,Text.literal("Click a square to play"),cx,sy+cell*3+9,0xFFAAAAAA);
    }
    @Override public boolean mouseClicked(double mx,double my,int button){
        if(button!=0||finished||!playerTurn)return true;int cx=MinecraftClient.getInstance().getWindow().getScaledWidth()/2,cell=48,sx=cx-cell*3/2,sy=80;
        for(int i=0;i<9;i++){int x=sx+(i%3)*cell,y=sy+(i/3)*cell;if(inside(mx,my,x,y,cell,cell)&&board[i]==0){board[i]=1;playerTurn=false;message="AI thinking...";checkEnd();return true;}}return true;
    }
}
