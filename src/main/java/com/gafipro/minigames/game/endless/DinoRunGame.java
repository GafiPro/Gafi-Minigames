package com.gafipro.minigames.game.endless;

import com.gafipro.minigames.game.BaseGame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Endless runner with monotonic timing, jump physics and collision-aware obstacles. */
public final class DinoRunGame extends BaseGame {
    private static final int GROUND_Y = 220;
    private static final int PLAYER_X = -125;
    private static final double GRAVITY = 24.0;
    private static final double JUMP_VELOCITY = -10.5;

    private record Obstacle(double x, int width, int height) {}

    private final List<Obstacle> obstacles = new ArrayList<>();
    private double playerY;
    private double velocityY;
    private long lastNanos;
    private long nextSpawnNanos;
    private long runNanos;
    private double distance;

    @Override public String id() { return "dino_run"; }
    @Override public String title() { return "Dino Run"; }
    @Override public String category() { return "Endless"; }

    @Override public void start() {
        obstacles.clear();
        playerY = 0;
        velocityY = 0;
        distance = 0;
        runNanos = 0;
        lastNanos = System.nanoTime();
        nextSpawnNanos = lastNanos + 900_000_000L;
        metrics.level(1);
        status = "SPACE / UP / click to jump • Survive as long as possible";
    }

    private void jump() {
        if (finished || playerY != 0) return;
        velocityY = JUMP_VELOCITY;
        markMove();
    }

    @Override public void tick() {
        super.tick();
        if (finished || state() != com.gafipro.minigames.game.GameState.PLAYING) return;
        long now = System.nanoTime();
        double dt = Math.min(0.05, Math.max(0, (now - lastNanos) / 1_000_000_000.0));
        lastNanos = now;
        runNanos = elapsedNanos();

        double speed = 165.0 + Math.min(180.0, runNanos / 12_000_000.0);
        playerY += velocityY * dt;
        velocityY += GRAVITY * dt;
        if (playerY > 0) {
            playerY = 0;
            velocityY = 0;
        }

        for (Iterator<Obstacle> it = obstacles.iterator(); it.hasNext();) {
            Obstacle o = it.next();
            it.removeIf(v -> v == null);
        }
        List<Obstacle> moved = new ArrayList<>(obstacles.size());
        for (Obstacle o : obstacles) {
            double x = o.x() - speed * dt;
            if (x > -210) moved.add(new Obstacle(x, o.width(), o.height()));
            else score += 5;
        }
        obstacles.clear();
        obstacles.addAll(moved);

        if (now >= nextSpawnNanos) {
            int h = 28 + random.nextInt(28);
            int w = 18 + random.nextInt(16);
            obstacles.add(new Obstacle(200, w, h));
            double spawnSeconds = Math.max(0.65, 1.35 - runNanos / 70_000_000_000.0);
            nextSpawnNanos = now + (long) (spawnSeconds * 1_000_000_000L);
            metrics.level(1 + (int) (runNanos / 20_000_000_000L));
        }

        if (runNanos / 100_000_000L > 0) score = Math.max(score, (int) (runNanos / 250_000_000L));
        if (collides()) finish(score);
    }

    private boolean collides() {
        double playerLeft = PLAYER_X;
        double playerRight = PLAYER_X + 28;
        double playerBottom = GROUND_Y - playerY;
        double playerTop = playerBottom - 34;
        for (Obstacle o : obstacles) {
            double left = o.x();
            double right = o.x() + o.width();
            double bottom = GROUND_Y;
            double top = GROUND_Y - o.height();
            if (playerRight > left && playerLeft < right && playerBottom > top && playerTop < bottom) return true;
        }
        return false;
    }

    @Override public void render(DrawContext c, int mx, int my, float delta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        drawHeader(c, "DINO RUN", status);
        int ground = Math.min(GROUND_Y, mc.getWindow().getScaledHeight() - 55);
        int baseX = cx();
        c.fill(baseX - 190, ground, baseX + 190, ground + 3, 0xFFFFFFFF);
        int py = (int) (ground - 34 - playerY);
        c.fill(baseX + PLAYER_X, py, baseX + PLAYER_X + 28, py + 34, 0xFF55CC88);
        for (Obstacle o : obstacles) {
            int x = baseX + (int) o.x();
            c.fill(x, ground - o.height(), x + o.width(), ground, 0xFFE74C3C);
        }
        c.drawCenteredTextWithShadow(mc.textRenderer, Text.literal("Score: " + score), baseX, Math.min(ground + 18, mc.getWindow().getScaledHeight() - 25), 0xFFFFFFFF);
    }

    @Override public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_R) begin();
        else if (keyCode == GLFW.GLFW_KEY_SPACE || keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_W) jump();
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) jump();
        return true;
    }
}
