package com.gafipro.minigames.game;

/** Small mutable metrics object used by result screens and persistent statistics. */
public final class GameMetrics {
    private int score;
    private int moves;
    private int combo;
    private int streak;
    private int level;
    private int mistakes;
    private double accuracyPercent = 100.0;
    private long elapsedNanos;

    public int score() { return score; }
    public int moves() { return moves; }
    public int combo() { return combo; }
    public int streak() { return streak; }
    public int level() { return level; }
    public int mistakes() { return mistakes; }
    public double accuracyPercent() { return accuracyPercent; }
    public long elapsedNanos() { return elapsedNanos; }

    public GameMetrics score(int value) { score = Math.max(0, value); return this; }
    public GameMetrics addScore(int value) { score = Math.max(0, score + value); return this; }
    public GameMetrics moves(int value) { moves = Math.max(0, value); return this; }
    public GameMetrics incrementMoves() { moves++; return this; }
    public GameMetrics combo(int value) { combo = Math.max(0, value); return this; }
    public GameMetrics streak(int value) { streak = Math.max(0, value); return this; }
    public GameMetrics level(int value) { level = Math.max(0, value); return this; }
    public GameMetrics mistakes(int value) { mistakes = Math.max(0, value); return this; }
    public GameMetrics accuracyPercent(double value) { accuracyPercent = Math.max(0.0, Math.min(100.0, value)); return this; }
    public GameMetrics elapsedNanos(long value) { elapsedNanos = Math.max(0, value); return this; }
    public long elapsedMillis() { return elapsedNanos / 1_000_000L; }
}
