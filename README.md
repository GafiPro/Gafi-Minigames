# Gafi Minigames

A polished Fabric client-side Minecraft 1.21.11 arcade containing small games that run entirely inside Minecraft GUIs.

## Features

- `/games` opens the arcade launcher.
- Separate categories for arcade, puzzle, board and quick games.
- Current playable games:
  - Reaction Test
  - Whack-A-Mole
  - Memory Match
  - Minesweeper
  - 2048
  - Tic-Tac-Toe
  - Connect Four
  - Rock Paper Scissors
- Local statistics are saved automatically in `.minecraft/config/gafi-minigames.json`.
- Designed with modular game classes so additional games can be added without rewriting the launcher.
- No interaction with the player's real inventory while playing.
- Java 21 / Fabric / Minecraft 1.21.11.

## Installation

Install Fabric Loader for Minecraft 1.21.11, Fabric API, and the Gafi Minigames JAR in your `mods` folder.

## Commands

`/games` — open the Gafi Minigames arcade.

## Multiplayer

The current release focuses on polished local single-player and AI games. Multiplayer transport is intentionally kept separate from game logic so a compatible server/network bridge can be added without rewriting the games.

## Roadmap

Planned game families include Sudoku, Sliding Puzzle, Lights Out, Nonogram, Word Search, Match-3, Maze, Snake, Breakout, Pong, Tetris-style Falling Blocks, Frogger, Hangman, Chess, Checkers, Reversi, Gomoku, Battleship and multiplayer variants.

## Screenshots

Screenshots will be added as the UI evolves.
