package com.gafipro.minigames;

import com.gafipro.minigames.game.GameState;
import com.gafipro.minigames.game.board.CheckersGame;
import com.gafipro.minigames.game.board.ChessGame;
import com.gafipro.minigames.game.board.ReversiGame;
import com.gafipro.minigames.game.puzzle.LightsOutGame;
import com.gafipro.minigames.game.puzzle.Match3Game;
import com.gafipro.minigames.game.puzzle.MinesweeperGame;
import com.gafipro.minigames.game.puzzle.NonogramGame;
import com.gafipro.minigames.game.puzzle.SlidingPuzzleGame;
import com.gafipro.minigames.game.puzzle.SudokuGame;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AdversarialRuleTest {
    @Test
    void chessRejectsIllegalKingMovesAndKingCaptures() throws Exception {
        ChessGame game = new ChessGame(); method(ChessGame.class, "setupBoard").invoke(game);
        char[][] board = get(game, "board", char[][].class); clear(board); board[7][4]='K'; board[0][4]='r'; board[0][0]='k';
        List<?> legal=legalMoves(game); assertFalse(hasMove(legal,6,4)); clear(board); board[7][4]='K'; board[7][3]='Q'; board[7][0]='k'; legal=legalMoves(game); assertFalse(hasMove(legal,7,0));
    }

    @Test
    void chessRejectsCastlingThroughAttackAndUnsafeEnPassant() throws Exception {
        ChessGame game=new ChessGame(); method(ChessGame.class,"setupBoard").invoke(game); char[][] board=get(game,"board",char[][].class); clear(board);
        board[7][4]='K'; board[7][7]='R'; board[0][4]='k'; board[0][5]='r'; assertFalse((boolean)method(ChessGame.class,"canCastle",boolean.class,boolean.class).invoke(game,true,true));
        clear(board); board[7][4]='K'; board[0][0]='k'; board[0][4]='r'; board[3][4]='P'; board[3][3]='p'; set(game,"enPassantRow",2); set(game,"enPassantCol",3);
        List<?> legal=legalMoves(game); boolean unsafeEp=false; for(Object move:legal)if(boolProperty(move,"enPassant")&&intProperty(move,"tr")==2&&intProperty(move,"tc")==3)unsafeEp=true; assertFalse(unsafeEp);
    }

    @Test
    void chessGeneratesAllPromotionTypesAndDetectsTerminalRules() throws Exception {
        ChessGame game=new ChessGame(); method(ChessGame.class,"setupBoard").invoke(game); char[][] board=get(game,"board",char[][].class); clear(board);
        board[7][7]='K'; board[0][7]='k'; board[1][0]='P'; List<?> legal=legalMoves(game); int promotions=0; for(Object move:legal)if(intProperty(move,"tr")==0&&intProperty(move,"tc")==0&&charProperty(move,"promotion")!='\0')promotions++; assertEquals(4,promotions);
        clear(board); board[0][7]='k'; board[2][5]='K'; board[1][6]='Q'; set(game,"whiteTurn",false); method(ChessGame.class,"resolvePosition").invoke(game); assertEquals(GameState.WON,game.state());
        game=new ChessGame(); method(ChessGame.class,"setupBoard").invoke(game); board=get(game,"board",char[][].class); clear(board); board[0][7]='k'; board[1][5]='K'; board[2][6]='Q'; set(game,"whiteTurn",false); method(ChessGame.class,"resolvePosition").invoke(game); assertEquals(GameState.DRAW,game.state());
        game=new ChessGame(); method(ChessGame.class,"setupBoard").invoke(game); board=get(game,"board",char[][].class); clear(board); board[7][4]='K'; board[0][4]='k'; set(game,"whiteTurn",true); set(game,"halfmoveClock",100); method(ChessGame.class,"resolvePosition").invoke(game); assertEquals(GameState.DRAW,game.state());
    }

    @Test
    void checkersEnforcesCapturesAndMultiJumps() throws Exception {
        CheckersGame game=new CheckersGame(); method(CheckersGame.class,"start").invoke(game); int[][] board=get(game,"board",int[][].class); clear(board); board[5][2]=1; board[4][3]=-1; board[2][5]=-1;
        List<?> turns=(List<?>)method(CheckersGame.class,"legalTurns",boolean.class).invoke(game,true); assertFalse(turns.isEmpty()); boolean sawChain=false;
        for(Object t:turns){List<?> jumps=(List<?>)t;assertFalse(jumps.isEmpty());assertTrue(intProperty(jumps.get(0),"cr")>=0);if(jumps.size()>=2)sawChain=true;} assertTrue(sawChain);
    }

    @Test
    void checkersSupportsKingBackwardsCaptureAndPromotion() throws Exception {
        CheckersGame game=new CheckersGame(); method(CheckersGame.class,"start").invoke(game); int[][] board=get(game,"board",int[][].class); clear(board); board[3][3]=2; board[2][2]=-1;
        List<?> jumps=(List<?>)method(CheckersGame.class,"jumps",int.class,int.class,int.class).invoke(game,3,3,2); assertTrue(hasJumpTo(jumps,1,1)); clear(board); board[1][0]=1;
        Object jump=newRecord("com.gafipro.minigames.game.board.CheckersGame$Jump",1,0,0,1,-1,-1); method(CheckersGame.class,"applyJump",jump.getClass()).invoke(game,jump); assertEquals(2,board[0][1]);
    }

    @Test
    void reversiRecognizesDirectionalFlipsAndAppliesThem() throws Exception {
        ReversiGame game=new ReversiGame(); method(ReversiGame.class,"start").invoke(game); int[][] board=get(game,"board",int[][].class); clear(board); board[3][0]=1; board[3][1]=-1;
        assertTrue((boolean)method(ReversiGame.class,"flips",int.class,int.class,int.class).invoke(game,3,2,1)); Object move=newRecord("com.gafipro.minigames.game.board.ReversiGame$Move",3,2); method(ReversiGame.class,"place",move.getClass(),int.class).invoke(game,move,1); assertEquals(1,board[3][1]);
    }

    @Test
    void minesweeperProtectsOpeningNeighborhoodAndFloodFills() throws Exception {
        MinesweeperGame game=new MinesweeperGame(); method(MinesweeperGame.class,"configureEasy").invoke(game); method(MinesweeperGame.class,"resetBoard").invoke(game); method(MinesweeperGame.class,"placeMines",int.class,int.class).invoke(game,0,0);
        boolean[][] mines=get(game,"mines",boolean[][].class); assertFalse(mines[0][0]);assertFalse(mines[0][1]);assertFalse(mines[1][0]);assertFalse(mines[1][1]);
        set(game,"width",3);set(game,"height",3);set(game,"mineCount",1);set(game,"mines",new boolean[][]{{false,false,false},{false,false,false},{false,false,true}});set(game,"open",new boolean[3][3]);set(game,"flags",new boolean[3][3]);set(game,"opened",0);
        method(MinesweeperGame.class,"reveal",int.class,int.class).invoke(game,0,0);assertTrue(intField(game,"opened")>1);assertFalse(get(game,"open",boolean[][].class)[2][2]);
    }

    @Test
    void sudokuSolutionIsValidAndPuzzleIsUnique() throws Exception {
        SudokuGame game=new SudokuGame(); method(SudokuGame.class,"start").invoke(game); int[][] solution=get(game,"solution",int[][].class),puzzle=get(game,"puzzle",int[][].class);
        assertTrue(validSudoku(solution));for(int r=0;r<9;r++)for(int c=0;c<9;c++)if(puzzle[r][c]!=0)assertEquals(solution[r][c],puzzle[r][c]);assertTrue((boolean)method(SudokuGame.class,"hasUniqueSolution",int[][].class).invoke(game,(Object)puzzle));
    }

    @Test
    void slidingPuzzleSizesRemainSolvableAndUnsolvedAfterGeneration() throws Exception {
        SlidingPuzzleGame game=new SlidingPuzzleGame(); for(int key:new int[]{51,52,53}){method(SlidingPuzzleGame.class,"keyPressed",int.class,int.class,int.class).invoke(game,key,0,0);int size=intField(game,"size");int[] board=get(game,"board",int[].class);assertFalse((boolean)method(SlidingPuzzleGame.class,"solved").invoke(game));assertTrue(solvable(board,size));}
    }

    @Test
    void match3GenerationHasNoInitialMatchesAndAtLeastOneLegalSwap() throws Exception {
        Match3Game game=new Match3Game(); method(Match3Game.class,"start").invoke(game); boolean[][] matches=(boolean[][])method(Match3Game.class,"findMatches").invoke(game);for(boolean[] row:matches)for(boolean v:row)assertFalse(v);assertTrue((boolean)method(Match3Game.class,"hasPossibleMove").invoke(game));
    }

    @Test
    void lightsOutCornerToggleOnlyTouchesThreeCells() throws Exception {
        LightsOutGame game=new LightsOutGame(); boolean[][] lights=get(game,"lights",boolean[][].class);for(boolean[] row:lights)java.util.Arrays.fill(row,false);method(LightsOutGame.class,"toggle",int.class,int.class).invoke(game,0,0);int count=0;for(boolean[] row:lights)for(boolean v:row)if(v)count++;assertEquals(3,count);
    }

    @Test
    void nonogramCluesExactlyDescribeTheGeneratedSolution() throws Exception {
        NonogramGame game=new NonogramGame();method(NonogramGame.class,"start").invoke(game);boolean[][] solution=get(game,"solution",boolean[][].class);Method clues=method(NonogramGame.class,"clues",boolean.class,int.class);
        for(int i=0;i<10;i++){assertArrayEquals(runLengths(solution,true,i),(int[])clues.invoke(game,true,i));assertArrayEquals(runLengths(solution,false,i),(int[])clues.invoke(game,false,i));}
    }

    private static int[] runLengths(boolean[][] solution,boolean row,int index){java.util.ArrayList<Integer>out=new java.util.ArrayList<>();int run=0;for(int i=0;i<10;i++){boolean value=row?solution[i][index]:solution[index][i];if(value)run++;else if(run>0){out.add(run);run=0;}}if(run>0)out.add(run);if(out.isEmpty())out.add(0);return out.stream().mapToInt(Integer::intValue).toArray();}
    private static List<?> legalMoves(ChessGame game)throws Exception{return(List<?>)method(ChessGame.class,"legalMoves",boolean.class).invoke(game,true);}
    private static boolean hasMove(List<?> moves,int tr,int tc)throws Exception{for(Object m:moves)if(intProperty(m,"tr")==tr&&intProperty(m,"tc")==tc)return true;return false;}
    private static boolean hasJumpTo(List<?> jumps,int tr,int tc)throws Exception{for(Object j:jumps)if(intProperty(j,"tr")==tr&&intProperty(j,"tc")==tc)return true;return false;}
    private static int intProperty(Object object,String name)throws Exception{return((Number)property(object,name)).intValue();}
    private static char charProperty(Object object,String name)throws Exception{return(Character)property(object,name);}
    private static boolean boolProperty(Object object,String name)throws Exception{return(Boolean)property(object,name);}
    private static Object property(Object object,String name)throws Exception{Method m=object.getClass().getDeclaredMethod(name);m.setAccessible(true);return m.invoke(object);}
    private static int intField(Object owner,String name)throws Exception{Field f=owner.getClass().getDeclaredField(name);f.setAccessible(true);return f.getInt(owner);}
    private static <T>T get(Object owner,String name,Class<T> type)throws Exception{Field f=owner.getClass().getDeclaredField(name);f.setAccessible(true);return type.cast(f.get(owner));}
    private static void set(Object owner,String name,Object value)throws Exception{Field f=owner.getClass().getDeclaredField(name);f.setAccessible(true);f.set(owner,value);}
    private static Method method(Class<?> type,String name,Class<?>...params)throws Exception{Method m=type.getDeclaredMethod(name,params);m.setAccessible(true);return m;}
    private static void clear(char[][] board){for(char[]row:board)java.util.Arrays.fill(row,'.');}
    private static void clear(int[][] board){for(int[]row:board)java.util.Arrays.fill(row,0);}
    private static Object newRecord(String className,Object...args)throws Exception{Class<?>type=Class.forName(className);for(Constructor<?>c:type.getDeclaredConstructors())if(c.getParameterCount()==args.length){c.setAccessible(true);return c.newInstance(args);}throw new NoSuchMethodException(className);}
    private static boolean validSudoku(int[][]board){for(int i=0;i<9;i++){boolean[]row=new boolean[10],col=new boolean[10];for(int j=0;j<9;j++){int rv=board[i][j],cv=board[j][i];if(rv<1||rv>9||cv<1||cv>9||row[rv]||col[cv])return false;row[rv]=true;col[cv]=true;}}for(int br=0;br<9;br+=3)for(int bc=0;bc<9;bc+=3){boolean[]seen=new boolean[10];for(int r=br;r<br+3;r++)for(int c=bc;c<bc+3;c++){int v=board[r][c];if(seen[v])return false;seen[v]=true;}}return true;}
    private static boolean solvable(int[]board,int size){int inv=0;for(int i=0;i<board.length;i++)for(int j=i+1;j<board.length;j++)if(board[i]!=board.length-1&&board[j]!=board.length-1&&board[i]>board[j])inv++;if((size&1)==1)return(inv&1)==0;int empty=0;for(int i=0;i<board.length;i++)if(board[i]==board.length-1)empty=i;int rowFromBottom=size-(empty/size);return(rowFromBottom%2==0)!=((inv&1)==0);}
}
