package com.gafipro.minigames.game.endless;

import com.gafipro.minigames.game.BaseGame;
import com.gafipro.minigames.game.GameState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

/** Breakout with continuous ball motion, lives and level progression. */
public final class BreakoutGame extends BaseGame {
    private int rows;
    private int cols;
    private boolean[][] bricks;
    private int remaining;
    private int lives;
    private int level;
    private double ballX, ballY, vx, vy, paddleX;
    private long lastNanos;

    @Override public String id() { return "breakout"; }
    @Override public String title() { return "Breakout"; }
    @Override public String category() { return "Endless"; }

    @Override public void start() {
        level = 1;
        lives = 3;
        paddleX = 0;
        lastNanos = System.nanoTime();
        setupLevel();
        status = "A/D or arrows • Destroy every brick • Lives: 3";
    }

    private void setupLevel() {
        rows = Math.min(8, 5 + level / 2);
        cols = 8;
        bricks = new boolean[cols][rows];
        remaining = rows * cols;
        for (int x = 0; x < cols; x++) for (int y = 0; y < rows; y++) bricks[x][y] = true;
        ballX = 0;
        ballY = 112;
        double base = 150 + level * 8;
        vx = (random.nextBoolean() ? 1 : -1) * base * 0.72;
        vy = -base;
        lastNanos = System.nanoTime();
    }

    @Override public void tick() {
        super.tick();
        if (finished || state != GameState.PLAYING) return;
        long now = System.nanoTime();
        double dt = Math.min(0.025, Math.max(0, (now - lastNanos) / 1_000_000_000.0));
        lastNanos = now;
        ballX += vx * dt;
        ballY += vy * dt;

        if (ballX < -180) { ballX = -180; vx = Math.abs(vx); }
        if (ballX > 180) { ballX = 180; vx = -Math.abs(vx); }
        if (ballY < 58) { ballY = 58; vy = Math.abs(vy); }

        double paddleLeft = paddleX - 48;
        double paddleRight = paddleX + 48;
        double paddleTop = 218;
        double paddleBottom = 228;
        if (vy > 0 && ballY + 6 >= paddleTop && ballY - 6 <= paddleBottom && ballX >= paddleLeft - 6 && ballX <= paddleRight + 6) {
            ballY = paddleTop - 7;
            double hit = (ballX - paddleX) / 48.0;
            double speed = Math.hypot(vx, vy) * 1.01;
            vx = hit * speed;
            vy = -Math.sqrt(Math.max(1, speed * speed - vx * vx));
        }

        boolean bounced = false;
        int bx = (int) Math.floor((ballX + 160) / 40);
        int by = (int) Math.floor((ballY - 60) / 22);
        if (bx >= 0 && bx < cols && by >= 0 && by < rows && bricks[bx][by]) {
            bricks[bx][by] = false;
            remaining--;
            score += 50 * level;
            metrics.incrementMoves();
            vy = -vy;
            bounced = true;
        }
        if (remaining == 0) {
            level++;
            metrics.level(level);
            if (level >= 5) finishWin(score + lives * 100);
            else setupLevel();
        } else if (ballY > 250) {
            lives--;
            if (lives <= 0) finish(score);
            else setupBall();
        }
        status = "A/D or arrows • Level: " + level + " • Bricks: " + remaining + " • Lives: " + lives;
    }

    private void setupBall() {
        ballX = 0;
        ballY = 112;
        double speed = 150 + level * 8;
        vx = (random.nextBoolean() ? 1 : -1) * speed * 0.72;
        vy = -speed;
        lastNanos = System.nanoTime();
    }

    @Override public void render(DrawContext c, int mx, int my, float delta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        drawHeader(c, "BREAKOUT", status);
        int baseX = cx();
        for (int y = 0; y < rows; y++) for (int x = 0; x < cols; x++) if (bricks[x][y]) {
            int left = baseX - 160 + x * 40;
            int top = 60 + y * 22;
            c.fill(left + 2, top + 2, left + 38, top + 20, 0xFFE67E22);
        }
        c.fill(baseX + (int) paddleX - 48, 218, baseX + (int) paddleX + 48, 228, 0xFF55CC88);
        c.fill(baseX + (int) ballX - 6, 112 + (int) ballY - 112 - 6, baseX + (int) ballX + 6, 112 + (int) ballY - 112 + 6, 0xFFFFFFFF);
        c.drawCenteredTextWithShadow(mc.textRenderer, net.minecraft.text.Text.literal("Score: " + score + " • Lives: " + lives), baseX, 246, 0xFFFFFFFF);
    }

    @Override public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_R) begin();
        else if (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_A) paddleX -= 18;
        else if (keyCode == GLFW.GLFW_KEY_RIGHT || keyCode == GLFW.GLFW_KEY_D) paddleX += 18;
        paddleX = Math.max(-132, Math.min(132, paddleX));
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) paddleX = Math.max(-132, Math.min(132, mouseX - cx()));
        return true;
    }
}
