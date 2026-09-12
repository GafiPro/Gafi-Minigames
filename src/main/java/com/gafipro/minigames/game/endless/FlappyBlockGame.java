package com.gafipro.minigames.game.endless;

import com.gafipro.minigames.game.BaseGame;
import com.gafipro.minigames.game.GameState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/** Flappy-style endless game with fixed pipe gaps and continuous collision checks. */
public final class FlappyBlockGame extends BaseGame {
    private static final int PLAYER_X = -105;
    private static final int PLAYER_SIZE = 18;
    private static final int DEFAULT_TOP = 65;
    private static final int DEFAULT_BOTTOM = 245;
    private static final int PIPE_WIDTH = 28;
    private static final int GAP = 66;

    private record Pipe(double x, int gapTop, boolean scored) {}
    private final List<Pipe> pipes = new ArrayList<>();
    private double playerY;
    private double velocity;
    private long lastNanos;
    private long nextSpawnNanos;

    @Override public String id() { return "flappy_block"; }
    @Override public String title() { return "Flappy Block"; }
    @Override public String category() { return "Endless"; }

    private int topY() {
        return Math.max(50, Math.min(DEFAULT_TOP, MinecraftClient.getInstance().getWindow().getScaledHeight() - 190));
    }

    private int bottomY() {
        return Math.min(DEFAULT_BOTTOM, MinecraftClient.getInstance().getWindow().getScaledHeight() - 35);
    }

    @Override public void start() {
        pipes.clear();
        playerY = (topY() + bottomY() - PLAYER_SIZE) / 2.0;
        velocity = 0;
        lastNanos = System.nanoTime();
        nextSpawnNanos = lastNanos + 850_000_000L;
        addPipe(210);
        metrics.level(1);
        status = "SPACE / click to flap • Pass through every gap";
    }

    private void addPipe(double x) {
        int span = Math.max(1, bottomY() - topY() - GAP - 8);
        int gapTop = topY() + 4 + random.nextInt(span);
        pipes.add(new Pipe(x, gapTop, false));
    }

    private void flap() {
        if (finished || state != GameState.PLAYING) return;
        velocity = -6.2;
        markMove();
    }

    @Override public void tick() {
        super.tick();
        if (finished || state != GameState.PLAYING) return;
        long now = System.nanoTime();
        double dt = Math.min(0.05, Math.max(0, (now - lastNanos) / 1_000_000_000.0));
        lastNanos = now;
        double speed = 125 + Math.min(90, elapsedNanos() / 20_000_000.0);
        playerY += velocity * dt;
        velocity += 18.5 * dt;
        int top = topY(), bottom = bottomY();

        for (int i = pipes.size() - 1; i >= 0; i--) {
            Pipe p = pipes.get(i);
            double x = p.x() - speed * dt;
            if (x < -230) {
                pipes.remove(i);
                continue;
            }
            if (!p.scored() && x + PIPE_WIDTH < PLAYER_X) {
                score += 100;
                pipes.set(i, new Pipe(x, p.gapTop(), true));
                p = pipes.get(i);
            } else if (x != p.x()) {
                pipes.set(i, new Pipe(x, p.gapTop(), p.scored()));
            }
            if (overlapsPipe(x, p.gapTop())) {
                finish(score);
                return;
            }
        }
        if (now >= nextSpawnNanos) {
            addPipe(215);
            nextSpawnNanos = now + 900_000_000L;
            metrics.level(1 + score / 500);
        }
        if (playerY < top || playerY + PLAYER_SIZE > bottom) finish(score);
    }

    private boolean overlapsPipe(double x, int gapTop) {
        double left = PLAYER_X;
        double right = PLAYER_X + PLAYER_SIZE;
        double top = playerY;
        double bottom = playerY + PLAYER_SIZE;
        if (right <= x || left >= x + PIPE_WIDTH) return false;
        return top < gapTop || bottom > gapTop + GAP;
    }

    @Override public void render(DrawContext c, int mx, int my, float delta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        drawHeader(c, "FLAPPY BLOCK", status);
        int baseX = cx();
        int top = topY(), bottom = bottomY();
        c.fill(baseX - 190, top, baseX + 190, bottom, 0xFF20262B);
        int playerRenderY = (int) Math.round(playerY);
        c.fill(baseX + PLAYER_X, playerRenderY, baseX + PLAYER_X + PLAYER_SIZE, playerRenderY + PLAYER_SIZE, 0xFF55CC88);
        for (Pipe p : pipes) {
            int x = baseX + (int) p.x();
            int gapRenderTop = p.gapTop();
            c.fill(x, top, x + PIPE_WIDTH, gapRenderTop, 0xFF3F9B76);
            int lowerY = gapRenderTop + GAP;
            c.fill(x, lowerY, x + PIPE_WIDTH, bottom, 0xFF3F9B76);
        }
    }

    @Override public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_R) begin();
        else if (keyCode == GLFW.GLFW_KEY_SPACE || keyCode == GLFW.GLFW_KEY_UP) flap();
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) flap();
        return true;
    }
}
