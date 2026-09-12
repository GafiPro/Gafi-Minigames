package com.gafipro.minigames.core;

import java.util.List;

public final class GameCatalog {
    public enum Category { ARCADE, PUZZLE, BOARD, ENDLESS }
    public record Entry(String id,String title,Category category,String description,boolean ai,boolean pvp,boolean leaderboard,String key){}
    private GameCatalog(){}
    public static List<Entry> all(){return List.of(
        e("reaction_test","Reaction Test",Category.ARCADE,"Wait for the green signal and react.",false,false,true,"R"),
        e("whack_a_mole","Whack-A-Mole",Category.ARCADE,"Hit targets before they move.",false,false,true,"W"),
        e("color_rush","Color Rush",Category.ARCADE,"Choose the matching ink color as fast as possible.",false,false,true,"C"),
        e("pattern_copy","Pattern Copy",Category.ARCADE,"Memorize and reproduce the pattern.",false,false,true,"P"),
        e("fast_click","Fast Click",Category.ARCADE,"Click the target 20 times as quickly as possible.",false,false,true,"F"),
        e("safe_tile","Safe Tile",Category.ARCADE,"Find the safe tile before time runs out.",false,false,true,"S"),
        e("target_practice","Target Practice",Category.ARCADE,"Hit moving targets for points.",false,false,true,"T"),
        e("math_rush","Math Rush",Category.ARCADE,"Solve calculations before the timer expires.",false,false,true,"M"),
        e("word_scramble","Word Scramble",Category.ARCADE,"Unscramble the hidden word.",false,false,true,"O"),
        e("typing_speed","Typing Speed",Category.ARCADE,"Type the prompt accurately.",false,false,true,"Y"),
        e("simon_says","Simon Says",Category.ARCADE,"Repeat the growing sequence.",false,false,true,"I"),
        e("minesweeper","Minesweeper",Category.PUZZLE,"Clear mines using numbers and flags.",false,false,true,"1"),
        e("2048","2048",Category.PUZZLE,"Merge tiles to reach 2048.",false,false,true,"2"),
        e("2048_extreme","2048 Extreme",Category.PUZZLE,"A faster 5x5 2048 challenge.",false,false,true,"E"),
        e("sudoku","Sudoku",Category.PUZZLE,"Complete the 9x9 number puzzle.",false,false,true,"U"),
        e("sliding_puzzle","Sliding Puzzle",Category.PUZZLE,"Arrange the numbered tiles.",false,false,true,"L"),
        e("lights_out","Lights Out",Category.PUZZLE,"Turn every light off.",false,false,true,"G"),
        e("nonogram","Nonogram",Category.PUZZLE,"Reveal the hidden picture using row and column clues.",false,false,true,"N"),
        e("word_search","Word Search",Category.PUZZLE,"Find hidden words in the grid.",false,false,true,"H"),
        e("match_3","Match-3",Category.PUZZLE,"Swap adjacent tiles, clear matches and chain cascades.",false,false,true,"3"),
        e("maze","Maze",Category.PUZZLE,"Reach the exit as quickly as possible.",false,false,true,"Z"),
        e("spot_difference","Spot the Difference",Category.PUZZLE,"Find every changed cell.",false,false,true,"D"),
        e("sequence_memory","Sequence Memory",Category.PUZZLE,"Remember and reproduce the sequence.",false,false,true,"Q"),
        e("tic_tac_toe","Tic-Tac-Toe",Category.BOARD,"Classic three in a row.",true,true,false,"A"),
        e("connect_four","Connect Four",Category.BOARD,"Connect four pieces vertically, horizontally or diagonally.",true,true,true,"B"),
        e("rock_paper_scissors","Rock Paper Scissors",Category.BOARD,"Choose your move and beat the opponent.",true,true,false,"V"),
        e("battleship","Battleship",Category.BOARD,"Sink the hidden enemy fleet.",true,false,true,"K"),
        e("chess","Chess",Category.BOARD,"Full legal chess rules against AI.",true,false,true,"X"),
        e("checkers","Checkers",Category.BOARD,"Capture every opposing piece.",true,false,true,"J"),
        e("dots_and_boxes","Dots and Boxes",Category.BOARD,"Complete boxes and claim territory.",true,false,true,"O2"),
        e("four_in_row_mini","Four-in-a-Row Mini",Category.BOARD,"A compact connect-four challenge.",true,false,true,"R2"),
        e("reversi","Reversi / Othello",Category.BOARD,"Surround pieces and control the board.",true,false,true,"T2"),
        e("gomoku","Gomoku",Category.BOARD,"Connect five stones in a row.",true,false,true,"G2"),
        e("hangman","Hangman",Category.BOARD,"Guess the hidden word before six misses.",false,false,false,"H2"),
        e("snake","Snake",Category.ENDLESS,"Grow longer, eat food and survive.",false,false,true,"N2"),
        e("dino_run","Dino Run",Category.ENDLESS,"Jump over obstacles and survive.",false,false,true,"D2"),
        e("flappy_block","Flappy Block",Category.ENDLESS,"Fly through gaps without crashing.",false,false,true,"F2"),
        e("falling_blocks","Falling Blocks",Category.ENDLESS,"Clear lines with falling tetrominoes.",false,false,true,"T3"),
        e("breakout","Breakout",Category.ENDLESS,"Break bricks with the ball.",false,false,true,"B2"),
        e("pong","Pong",Category.ENDLESS,"Beat the computer in classic Pong.",true,false,true,"P2"),
        e("frogger","Frogger",Category.ENDLESS,"Cross hazards and reach safety.",false,false,true,"F3"),
        e("avoider","Avoider",Category.ENDLESS,"Dodge incoming hazards.",false,false,true,"A2"),
        e("tower_climber","Tower Climber",Category.ENDLESS,"Climb higher platforms without falling.",false,false,true,"C2"),
        e("endless_dodger","Endless Dodger",Category.ENDLESS,"Survive an accelerating hazard field.",false,false,true,"E2")
    );}
    public static String displayCategory(Category category) {
        return switch (category) {
            case ARCADE -> "Arcade";
            case PUZZLE -> "Puzzle";
            case BOARD -> "Board";
            case ENDLESS -> "Endless";
        };
    }
    private static Entry e(String id,String title,Category category,String description,boolean ai,boolean pvp,boolean leaderboard,String key){return new Entry(id,title,category,description,ai,pvp,leaderboard,key);}
}
