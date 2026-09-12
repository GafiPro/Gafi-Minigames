# Gafi Minigames

Gafi Minigames is a lightweight client-side Fabric mod for Minecraft 1.21.11 that turns a normal Minecraft client into an arcade of reflex, puzzle, board and endless games.

## Platform

- Minecraft 1.21.11
- Fabric Loader + Fabric API
- Java 21
- Client-side mod
- `/games` opens the launcher
- Player-facing game text is in English

## Game catalog

### Arcade (11)
Reaction Test, Whack-A-Mole, Color Rush, Pattern Copy, Fast Click, Safe Tile, Target Practice, Math Rush, Word Scramble, Typing Speed, Simon Says.

### Puzzle (12)
Minesweeper, 2048, 2048 Extreme, Sudoku, Sliding Puzzle, Lights Out, Nonogram, Word Search, Match-3, Maze, Spot the Difference, Sequence Memory.

### Board (11)
Tic-Tac-Toe, Connect Four, Rock Paper Scissors, Battleship, Chess, Checkers, Dots and Boxes, Four-in-a-Row Mini, Reversi / Othello, Gomoku, Hangman.

### Endless (10)
Snake, Dino Run, Flappy Block, Falling Blocks, Breakout, Pong, Frogger, Avoider, Tower Climber, Endless Dodger.

The catalog contains 44 games. `GameCatalog` is the launcher source of truth and `GameFactory` routes every catalog ID to the corresponding engine.

## Controls

`/games` opens the launcher. `R` restarts the current game and `Esc` exits back to the launcher. Each game displays its own keyboard and/or mouse controls.

## Singleplayer

Games run locally and do not replace the player's Minecraft inventory. Statistics are stored in the Minecraft config directory.

## Multiplayer

The mod includes shared PvP/session screens and a `/msg`-based invite transport for servers that expose private messaging. Current PvP screens cover Tic-Tac-Toe, Connect Four and Rock Paper Scissors.

The transport validates player identity, match membership, action syntax, sequence numbers, turn ownership and match lifecycle, but it is not server-authoritative or cryptographically secure. A client-only Fabric mod cannot enforce authoritative custom game state on an arbitrary vanilla server through ordinary chat. Competitive integrity therefore depends on the server transport available to both clients.

Hidden-information games such as Battleship remain local unless a future server companion provides a privacy-preserving protocol.

## Statistics

Per-game persistent statistics include games played, wins, losses, draws, best score, current streak, best streak, best time, highest level and average accuracy where applicable. Statistics reject invalid numeric values and are saved through a temporary file and atomic replacement when supported, with a safe fallback for filesystems that do not provide atomic moves.

## Word data

Word Scramble, Word Search, Hangman and Typing Speed use the shared embedded Minecraft-safe word bank in `src/main/resources/data/gafi-minigames/words.txt`.

## Build

Use Java 21 and run:

```text
gradle clean build --no-daemon --max-workers=1
```

GitHub Actions runs the same remapped build and keeps the generated JAR artifact for 3 days.

## Development

Game behavior is built around the shared `Game` / `BaseGame` lifecycle, `GameState`, metrics and persistent statistics. Large rule-heavy or real-time games use dedicated engine classes, while smaller games that share a simple structure remain grouped in focused collections.

Deterministic JUnit coverage includes catalog/factory synchronization, 2048 merge semantics, Connect Four win detection, Chess terminal and move legality cases, Checkers captures and promotion, Reversi flipping, Minesweeper safety/flood fill, Sudoku validity/uniqueness, Sliding Puzzle solvability, Match-3 generation and Lights Out edge toggles.

## Known limitations

The mod is client-side. The multiplayer layer is a chat transport rather than a server-authoritative protocol. Expanding authoritative multiplayer requires a compatible optional server companion using a custom payload protocol; the client does not claim otherwise.
