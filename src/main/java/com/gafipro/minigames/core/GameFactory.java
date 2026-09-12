package com.gafipro.minigames.core;

import com.gafipro.minigames.game.Game;
import com.gafipro.minigames.game.arcade.ArcadeCollection;
import com.gafipro.minigames.game.board.BoardCollection;
import com.gafipro.minigames.game.endless.EndlessCollection;
import com.gafipro.minigames.game.puzzle.PuzzleCollection;
import com.gafipro.minigames.game.puzzle.MemoryMatchGame;

import java.util.function.Supplier;

public final class GameFactory {
    private GameFactory() {}
    public static Game create(String id) {
        return switch (id) {
            case "reaction_test" -> new ArcadeCollection.ReactionTest();
            case "whack_a_mole" -> new ArcadeCollection.WhackAMole();
            case "color_rush" -> new ArcadeCollection.ColorRush();
            case "pattern_copy" -> new ArcadeCollection.PatternCopy();
            case "fast_click" -> new ArcadeCollection.FastClick();
            case "safe_tile" -> new ArcadeCollection.SafeTile();
            case "target_practice" -> new ArcadeCollection.TargetPractice();
            case "math_rush" -> new ArcadeCollection.MathRush();
            case "word_scramble" -> new ArcadeCollection.WordScramble();
            case "typing_speed" -> new ArcadeCollection.TypingSpeed();
            case "simon_says" -> new ArcadeCollection.SimonSays();
            case "minesweeper" -> new PuzzleCollection.Minesweeper();
            case "2048" -> new PuzzleCollection.Game2048();
            case "2048_extreme" -> new PuzzleCollection.Game2048(true);
            case "sudoku" -> new PuzzleCollection.Sudoku();
            case "sliding_puzzle" -> new PuzzleCollection.SlidingPuzzle();
            case "lights_out" -> new PuzzleCollection.LightsOut();
            case "nonogram" -> new PuzzleCollection.Nonogram();
            case "word_search" -> new PuzzleCollection.WordSearch();
            case "match_3" -> new PuzzleCollection.Match3();
            case "maze" -> new PuzzleCollection.Maze();
            case "spot_difference" -> new PuzzleCollection.SpotDifference();
            case "sequence_memory" -> new PuzzleCollection.SequenceMemory();
            case "tic_tac_toe" -> new BoardCollection.TicTacToe();
            case "connect_four" -> new BoardCollection.ConnectFour();
            case "rock_paper_scissors" -> new BoardCollection.RockPaperScissors();
            case "battleship" -> new BoardCollection.Battleship();
            case "chess" -> new BoardCollection.Chess();
            case "checkers" -> new BoardCollection.Checkers();
            case "dots_and_boxes" -> new BoardCollection.DotsAndBoxes();
            case "four_in_row_mini" -> new BoardCollection.FourInRowMini();
            case "reversi" -> new BoardCollection.Reversi();
            case "gomoku" -> new BoardCollection.Gomoku();
            case "hangman" -> new BoardCollection.Hangman();
            case "snake" -> new EndlessCollection.Snake();
            case "dino_run" -> new EndlessCollection.DinoRun();
            case "flappy_block" -> new EndlessCollection.FlappyBlock();
            case "falling_blocks" -> new EndlessCollection.FallingBlocks();
            case "breakout" -> new EndlessCollection.Breakout();
            case "pong" -> new EndlessCollection.Pong();
            case "frogger" -> new EndlessCollection.Frogger();
            case "avoider" -> new EndlessCollection.Avoider();
            case "tower_climber" -> new EndlessCollection.TowerClimber();
            case "endless_dodger" -> new EndlessCollection.EndlessDodger();
            default -> throw new IllegalArgumentException("Unknown game: " + id);
        };
    }
}
