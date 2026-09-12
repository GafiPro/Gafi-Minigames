package com.gafipro.minigames.core;

import com.gafipro.minigames.game.Game;
import com.gafipro.minigames.game.arcade.ArcadeCollection;
import com.gafipro.minigames.game.arcade.ReactionTestGame;
import com.gafipro.minigames.game.arcade.WhackAMoleGame;
import com.gafipro.minigames.game.board.BattleshipGame;
import com.gafipro.minigames.game.board.CheckersGame;
import com.gafipro.minigames.game.board.ChessGame;
import com.gafipro.minigames.game.board.ConnectFourGame;
import com.gafipro.minigames.game.board.DotsAndBoxesGame;
import com.gafipro.minigames.game.board.FourInRowMiniGame;
import com.gafipro.minigames.game.board.GomokuGame;
import com.gafipro.minigames.game.board.HangmanGame;
import com.gafipro.minigames.game.board.ReversiGame;
import com.gafipro.minigames.game.board.RockPaperScissorsGame;
import com.gafipro.minigames.game.board.TicTacToeGame;
import com.gafipro.minigames.game.endless.AvoiderGame;
import com.gafipro.minigames.game.endless.BreakoutGame;
import com.gafipro.minigames.game.endless.DinoRunGame;
import com.gafipro.minigames.game.endless.EndlessDodgerGame;
import com.gafipro.minigames.game.endless.FlappyBlockGame;
import com.gafipro.minigames.game.endless.FroggerGame;
import com.gafipro.minigames.game.endless.PongGame;
import com.gafipro.minigames.game.endless.SnakeGame;
import com.gafipro.minigames.game.endless.TetrisGame;
import com.gafipro.minigames.game.endless.TowerClimberGame;
import com.gafipro.minigames.game.puzzle.Game2048;
import com.gafipro.minigames.game.puzzle.LightsOutGame;
import com.gafipro.minigames.game.puzzle.Match3Game;
import com.gafipro.minigames.game.puzzle.MazeGame;
import com.gafipro.minigames.game.puzzle.MinesweeperGame;
import com.gafipro.minigames.game.puzzle.NonogramGame;
import com.gafipro.minigames.game.puzzle.SequenceMemoryGame;
import com.gafipro.minigames.game.puzzle.SlidingPuzzleGame;
import com.gafipro.minigames.game.puzzle.SpotDifferenceGame;
import com.gafipro.minigames.game.puzzle.SudokuGame;
import com.gafipro.minigames.game.puzzle.WordSearchGame;

import java.util.Set;

public final class GameFactory {
    private static final Set<String> SUPPORTED_IDS = Set.of(
        "reaction_test","whack_a_mole","color_rush","pattern_copy","fast_click","safe_tile",
        "target_practice","math_rush","word_scramble","typing_speed","simon_says","minesweeper",
        "2048","2048_extreme","sudoku","sliding_puzzle","lights_out","nonogram","word_search",
        "match_3","maze","spot_difference","sequence_memory","tic_tac_toe","connect_four",
        "rock_paper_scissors","battleship","chess","checkers","dots_and_boxes","four_in_row_mini",
        "reversi","gomoku","hangman","snake","dino_run","flappy_block","falling_blocks",
        "breakout","pong","frogger","avoider","tower_climber","endless_dodger"
    );

    private GameFactory() {}

    public static Set<String> supportedIds() { return SUPPORTED_IDS; }

    public static Game create(String id) {
        return switch (id) {
            case "reaction_test" -> new ReactionTestGame();
            case "whack_a_mole" -> new WhackAMoleGame();
            case "color_rush" -> new ArcadeCollection.ColorRush();
            case "pattern_copy" -> new ArcadeCollection.PatternCopy();
            case "fast_click" -> new ArcadeCollection.FastClick();
            case "safe_tile" -> new ArcadeCollection.SafeTile();
            case "target_practice" -> new ArcadeCollection.TargetPractice();
            case "math_rush" -> new ArcadeCollection.MathRush();
            case "word_scramble" -> new ArcadeCollection.WordScramble();
            case "typing_speed" -> new ArcadeCollection.TypingSpeed();
            case "simon_says" -> new ArcadeCollection.SimonSays();
            case "minesweeper" -> new MinesweeperGame();
            case "2048" -> new Game2048();
            case "2048_extreme" -> new Game2048(true);
            case "sudoku" -> new SudokuGame();
            case "sliding_puzzle" -> new SlidingPuzzleGame();
            case "lights_out" -> new LightsOutGame();
            case "nonogram" -> new NonogramGame();
            case "word_search" -> new WordSearchGame();
            case "match_3" -> new Match3Game();
            case "maze" -> new MazeGame();
            case "spot_difference" -> new SpotDifferenceGame();
            case "sequence_memory" -> new SequenceMemoryGame();
            case "tic_tac_toe" -> new TicTacToeGame();
            case "connect_four" -> new ConnectFourGame();
            case "rock_paper_scissors" -> new RockPaperScissorsGame();
            case "battleship" -> new BattleshipGame();
            case "chess" -> new ChessGame();
            case "checkers" -> new CheckersGame();
            case "dots_and_boxes" -> new DotsAndBoxesGame();
            case "four_in_row_mini" -> new FourInRowMiniGame();
            case "reversi" -> new ReversiGame();
            case "gomoku" -> new GomokuGame();
            case "hangman" -> new HangmanGame();
            case "snake" -> new SnakeGame();
            case "dino_run" -> new DinoRunGame();
            case "flappy_block" -> new FlappyBlockGame();
            case "falling_blocks" -> new TetrisGame();
            case "breakout" -> new BreakoutGame();
            case "pong" -> new PongGame();
            case "frogger" -> new FroggerGame();
            case "avoider" -> new AvoiderGame();
            case "tower_climber" -> new TowerClimberGame();
            case "endless_dodger" -> new EndlessDodgerGame();
            default -> throw new IllegalArgumentException("Unknown game: " + id);
        };
    }
}
