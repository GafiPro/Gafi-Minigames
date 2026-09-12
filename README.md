# Gafi Minigames

Gafi Minigames is a lightweight client-side Fabric mod for Minecraft 1.21.11 that turns a normal Minecraft client into a small arcade of classic, puzzle, reflex and endless games.

## Current platform

- Minecraft 1.21.11
- Fabric Loader
- Fabric API
- Java 21
- Client-side mod
- `/games` opens the arcade
- All player-facing text is written in English

## Games

### Arcade
Reaction Test, Whack-A-Mole, Color Rush, Pattern Copy, Fast Click, Safe Tile, Target Practice, Math Rush, Word Scramble, Typing Speed, Simon Says.

### Puzzle
Minesweeper, 2048, 2048 Extreme, Sudoku, Sliding Puzzle, Lights Out, Nonogram, Word Search, Match-3, Maze, Spot the Difference, Sequence Memory, Memory Match.

### Board
Tic-Tac-Toe, Connect Four, Rock Paper Scissors, Battleship, Chess, Checkers, Dots and Boxes, Four-in-a-Row Mini, Reversi / Othello, Gomoku, Hangman.

### Endless
Snake, Dino Run, Flappy Block, Falling Blocks, Breakout, Pong, Frogger, Avoider, Tower Climber, Endless Dodger.

## Controls

`/games` opens the launcher. Inside games, `R` restarts and `Esc` exits. Individual games show their controls in the interface and support mouse and/or keyboard input as appropriate.

## Singleplayer

Games run entirely on the client and never open or replace the player's actual Minecraft inventory. Local progress and statistics are stored in the Minecraft config directory.

## Multiplayer

The project contains a generic multiplayer/session layer and a working invite transport for servers that expose `/msg` or an equivalent private-message command. The transport is deliberately lightweight: protocol messages are encoded and sent through private chat, so no external account or service is required.

Example command:

```text
/games invite PlayerName
```

The long-term authoritative networking path is designed around a small optional server companion using Fabric custom payloads. A plain client-only mod cannot guarantee server-authoritative custom packets through an arbitrary vanilla server. Competitive integrity therefore depends on the transport available on the server.

Current PvP screens include Tic-Tac-Toe, Connect Four and Rock Paper Scissors. The multiplayer framework is intentionally shared rather than duplicating networking code in every game.

## Statistics

Local statistics include games played, wins, losses, draws, best score, current streak and best streak where applicable. Statistics survive Minecraft restarts.

## Configuration

The config directory contains:

- `gafi-minigames.json` for statistics
- `gafi-minigames-settings.json` for favorites, recent games and display/audio preferences

## Build

Use Java 21 and run:

```text
gradle clean build --no-daemon --max-workers=1
```

GitHub Actions performs the same remapped build and keeps the generated artifact for 3 days.

## Development

Game logic is organized into reusable collections and a shared `Game` / `BaseGame` lifecycle. The catalog and factory are centralized so adding another game does not require duplicating launcher logic.

## Known limitations

This is an active development project. Some deeper board-game rules and the full authoritative server-side multiplayer companion remain on the roadmap. The mod should never present a client-only approximation as server-authoritative security.
