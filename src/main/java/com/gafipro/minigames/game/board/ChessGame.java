package com.gafipro.minigames.game.board;

import com.gafipro.minigames.game.BaseGame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Complete local chess rules engine with legal-move validation and a lightweight selectable AI. */
public final class ChessGame extends BaseGame {
    private static final int SIZE = 8;
    private static final int WHITE = 1;
    private static final int BLACK = -1;
    private final char[][] board = new char[SIZE][SIZE];
    private final Map<String,Integer> repetition = new HashMap<>();
    private boolean whiteTurn;
    private int selectedRow=-1,selectedCol=-1,enPassantRow=-1,enPassantCol=-1;
    private boolean whiteKingMoved,blackKingMoved,whiteKingRookMoved,whiteQueenRookMoved,blackKingRookMoved,blackQueenRookMoved;
    private int halfmoveClock,aiDelay;
    private Difficulty difficulty=Difficulty.NORMAL;
    private enum Difficulty { EASY,NORMAL,HARD }
    private record Move(int fr,int fc,int tr,int tc,char promotion,boolean castleKingSide,boolean castleQueenSide,boolean enPassant){static Move normal(int fr,int fc,int tr,int tc){return new Move(fr,fc,tr,tc,'\0',false,false,false);}}
    private record Snapshot(char[][] board,boolean whiteTurn,int epRow,int epCol,boolean wkm,boolean bkm,boolean wkr,boolean wqr,boolean bkr,boolean bqr,int halfmoveClock){}

    @Override public String id(){return "chess";} @Override public String title(){return "Chess";} @Override public String category(){return "Board";}
    @Override public void start(){setupBoard();whiteTurn=true;selectedRow=selectedCol=-1;enPassantRow=enPassantCol=-1;whiteKingMoved=blackKingMoved=false;whiteKingRookMoved=whiteQueenRookMoved=blackKingRookMoved=blackQueenRookMoved=false;halfmoveClock=0;aiDelay=0;repetition.clear();repetition.merge(positionKey(),1,Integer::sum);status="Normal • White to move • 1 Easy  2 Normal  3 Hard";}
    private void setupBoard(){String[] rows={"rnbqkbnr","pppppppp","........","........","........","........","PPPPPPPP","RNBQKBNR"};for(int r=0;r<SIZE;r++)for(int c=0;c<SIZE;c++)board[r][c]=rows[r].charAt(c);}
    @Override public void tick(){super.tick();if(!finished&&!whiteTurn&&--aiDelay<=0)aiMove();}
    private void aiMove(){List<Move> legal=legalMoves(false);if(legal.isEmpty()){resolvePosition();return;}Move chosen=switch(difficulty){case EASY->legal.get(random.nextInt(legal.size()));case NORMAL->chooseBest(legal,2);case HARD->chooseBest(legal,3);};applyAndResolve(chosen);}
    private Move chooseBest(List<Move> legal,int depth){int best=Integer.MIN_VALUE;List<Move> bestMoves=new ArrayList<>();for(Move move:orderMoves(legal)){Snapshot s=snapshot();apply(move);int value=minimax(depth-1,Integer.MIN_VALUE+1,Integer.MAX_VALUE-1);restore(s);if(value>best){best=value;bestMoves.clear();bestMoves.add(move);}else if(value==best)bestMoves.add(move);}return bestMoves.get(random.nextInt(bestMoves.size()));}
    private int minimax(int depth,int alpha,int beta){List<Move> legal=legalMoves(whiteTurn);if(legal.isEmpty()){if(isInCheck(whiteTurn))return whiteTurn?-100000-depth:100000+depth;return 0;}if(isThreefold()||halfmoveClock>=100||insufficientMaterial())return 0;if(depth<=0)return evaluation();boolean maximizing=!whiteTurn;int best=maximizing?Integer.MIN_VALUE:Integer.MAX_VALUE;for(Move move:orderMoves(legal)){Snapshot s=snapshot();apply(move);int value=minimax(depth-1,alpha,beta);restore(s);if(maximizing){best=Math.max(best,value);alpha=Math.max(alpha,best);}else{best=Math.min(best,value);beta=Math.min(beta,best);}if(beta<=alpha)break;}return best;}
    private List<Move> orderMoves(List<Move> moves){List<Move> ordered=new ArrayList<>(moves);ordered.sort((a,b)->Integer.compare(movePriority(b),movePriority(a)));return ordered;}
    private int movePriority(Move m){char target=board[m.tr][m.tc];int value=pieceValue(Character.toLowerCase(target));if(m.enPassant)value=100;if(m.promotion!='\0')value+=800;if(m.castleKingSide||m.castleQueenSide)value+=50;return value;}
    private int evaluation(){int total=0;for(int r=0;r<SIZE;r++)for(int c=0;c<SIZE;c++){char p=board[r][c];if(p=='.')continue;int value=pieceValue(Character.toLowerCase(p));int color=Character.isUpperCase(p)?WHITE:BLACK;int center=(int)Math.round(3.0-(Math.abs(3.5-r)+Math.abs(3.5-c))*0.35);total+=color*(value+center);}total+=whiteTurn?-2:2;return total;}
    private int pieceValue(char p){return switch(p){case'p'->100;case'n','b'->320;case'r'->500;case'q'->900;case'k'->20000;default->0;};}
    private void applyAndResolve(Move move){apply(move);markMove();selectedRow=selectedCol=-1;resolvePosition();if(!finished){status=whiteTurn?"Your turn • White":"Thinking...";if(!whiteTurn)aiDelay=difficulty==Difficulty.HARD?2:5;}}
    private void resolvePosition(){List<Move> legal=legalMoves(whiteTurn);if(legal.isEmpty()){if(isInCheck(whiteTurn)){if(whiteTurn){status="Checkmate — Black wins.";finish(0);}else{status="Checkmate — You win!";finishWin(1000+Math.max(0,1000-halfmoveClock));}}else{status="Stalemate — draw.";finishDraw(500);}return;}if(isThreefold()){status="Draw — threefold repetition.";finishDraw(500);return;}if(halfmoveClock>=100){status="Draw — fifty-move rule.";finishDraw(500);return;}if(insufficientMaterial()){status="Draw — insufficient material.";finishDraw(500);return;}if(isInCheck(whiteTurn))status=(whiteTurn?"White":"Black")+" is in check.";}
    private boolean isThreefold(){return repetition.getOrDefault(positionKey(),0)>=3;}
    private boolean insufficientMaterial(){int bishops=0,knights=0,others=0;for(int r=0;r<SIZE;r++)for(int c=0;c<SIZE;c++){char p=board[r][c];if(p=='.'||Character.toLowerCase(p)=='k')continue;switch(Character.toLowerCase(p)){case'b'->bishops++;case'n'->knights++;default->others++;}}if(others>0)return false;if(bishops+knights<=1)return true;if(bishops==2&&knights==0){int first=-1;for(int r=0;r<SIZE;r++)for(int c=0;c<SIZE;c++)if(Character.toLowerCase(board[r][c])=='b'){if(first<0)first=(r+c)&1;else if(first!=((r+c)&1))return false;}return true;}return false;}
    private List<Move> legalMoves(boolean forWhite){Map<String,Integer> repetitionSnapshot=new HashMap<>(repetition);try{List<Move> pseudo=pseudoMoves(forWhite),legal=new ArrayList<>(pseudo.size());for(Move move:pseudo){Snapshot s=snapshot();apply(move);boolean illegal=isInCheck(forWhite);restore(s);if(!illegal)legal.add(move);}return legal;}finally{repetition.clear();repetition.putAll(repetitionSnapshot);}}
