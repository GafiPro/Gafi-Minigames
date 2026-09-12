package com.gafipro.minigames.game;

/** Lifecycle state shared by every mini-game. */
public enum GameState {
    READY,
    COUNTDOWN,
    PLAYING,
    PAUSED,
    WON,
    LOST,
    DRAW,
    RESULTS
}
