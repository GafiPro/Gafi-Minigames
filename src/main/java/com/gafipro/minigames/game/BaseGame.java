package com.gafipro.minigames.game;

import com.gafipro.minigames.core.GameStats;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Random;

/** Shared lifecycle, timing and result behaviour for every local mini-game. */
public abstract class BaseGame implements Game {
    protected final Random random = new Random();
    protected final GameMetrics metrics = new GameMetrics();
    protected GameState state = GameState.READY;
    protected boolean finished;
    protected int score;
    protected int ticks;
    protected boolean recorded;
    protected boolean won;
    protected String status = "";
    private long startedNanos;
    private long pausedAtNanos;
    private long pausedTotalNanos;

    /** Starts a fresh game and guarantees the base lifecycle is reset even when subclasses override start(). */
    public final void begin() {
        state = GameState.READY;
        finished = false;
        won = false;
        recorded = false;
        score = 0;
        ticks = 0;
        status = "";
        startedNanos = System.nanoTime();
        pausedAtNanos = 0L;
        pausedTotalNanos = 0L;
        metrics.score(0).moves(0).combo(0).streak(0).level(0).mistakes(0).accuracyPercent(100).elapsedNanos(0);
        start();
        state = finished ? (won ? GameState.WON : GameState.LOST) : GameState.PLAYING;
    }

    @Override public void start() { }

    @Override
    public void tick() {
        if (state == GameState.PLAYING) {
            ticks++;
            metrics.elapsedNanos(elapsedNanos());
        }
    }

    @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) { }
    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) { return false; }
    @Override public void keyPressed(int keyCode, int scanCode, int modifiers) { }
    @Override public boolean isFinished() { return finished; }

    public GameState state() { return state; }
    public GameMetrics metrics() { return metrics; }

    /** Uses a monotonic clock so elapsed durations are unaffected by wall-clock changes. */
    public long elapsedNanos() {
        if (startedNanos == 0L) return 0L;
        long now = state == GameState.PAUSED && pausedAtNanos != 0L ? pausedAtNanos : System.nanoTime();
        return Math.max(0L, now - startedNanos - pausedTotalNanos);
    }

    public long elapsedMillis() { return elapsedNanos() / 1_000_000L; }

    public void pauseGame() {
        if (state != GameState.PLAYING) return;
        pausedAtNanos = System.nanoTime();
        state = GameState.PAUSED;
    }

    public void resumeGame() {
        if (state != GameState.PAUSED || pausedAtNanos == 0L) return;
        pausedTotalNanos += System.nanoTime() - pausedAtNanos;
        pausedAtNanos = 0L;
        state = GameState.PLAYING;
    }

    @Override
    public void close() {
        if (!finished || recorded) return;
        metrics.score(score).elapsedNanos(elapsedNanos());
        GameStats.record(id(), score(), won);
        recorded = true;
    }

    @Override public int score() { return score; }
    public String status() { return status; }

    protected void finish(int finalScore) {
        score = Math.max(0, finalScore);
        metrics.score(score).elapsedNanos(elapsedNanos());
        finished = true;
        state = GameState.LOST;
    }

    protected void finishWin(int finalScore) {
        won = true;
        score = Math.max(0, finalScore);
        metrics.score(score).elapsedNanos(elapsedNanos());
        finished = true;
        state = GameState.WON;
    }

    protected void finishDraw(int finalScore) {
        won = false;
        score = Math.max(0, finalScore);
        metrics.score(score).elapsedNanos(elapsedNanos());
        finished = true;
        state = GameState.DRAW;
    }

    protected void markMove() { metrics.incrementMoves(); }

    protected void drawHeader(DrawContext c, String title, String subtitle) {
        MinecraftClient mc = MinecraftClient.getInstance();
        int cx = mc.getWindow().getScaledWidth() / 2;
        c.drawCenteredTextWithShadow(mc.textRenderer, Text.literal(title).formatted(Formatting.BOLD, Formatting.AQUA), cx, 34, 0xFFFFFFFF);
        c.drawCenteredTextWithShadow(mc.textRenderer, Text.literal(subtitle).formatted(Formatting.GRAY), cx, 48, 0xFFFFFFFF);
        if (!status.isEmpty()) c.drawCenteredTextWithShadow(mc.textRenderer, Text.literal(status).formatted(Formatting.WHITE), cx, 62, 0xFFFFFFFF);
    }

    protected boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    protected int cx() { return MinecraftClient.getInstance().getWindow().getScaledWidth() / 2; }
    protected int cy() { return MinecraftClient.getInstance().getWindow().getScaledHeight() / 2; }
    protected int rgb(int r, int g, int b) { return 0xFF000000 | (r << 16) | (g << 8) | b; }
}
