package com.gafipro.minigames.game.endless;

import com.gafipro.minigames.game.BaseGame;
import com.gafipro.minigames.game.GameState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/** Auto-bouncing vertical climber with platform collision and progressive scrolling. */
public final class TowerClimberGame extends BaseGame {
    private record Platform(double x, double y, double width) {}

    private final List<Platform> platforms = new ArrayList<>();
    private double playerX;
    private double playerY;
    private double velocityY;
    private double cameraY;
    private long lastNanos;

    @Override public String id() { return "tower_climber"; }
    @Override public String title() { return "Tower Climber"; }
    @Override public String category() { return "Endless"; }

    @Override public void start() {
        platforms.clear();
        playerX = 0;
        playerY = 210;
        velocityY = -320;
        cameraY = 0;
        lastNanos = System.nanoTime();
        platforms.add(new Platform(0, 235, 120));
        double y = 180;
        for (int i = 0; i < 16; i++) {
            addPlatformAt(y);
            y -= 55 + random.nextInt(18);
        }
        metrics.level(1);
        status = "A/D or arrows • Land on platforms and keep climbing";
    }

    private void addPlatformAt(double y) {
        double x = -150 + random.nextDouble() * 300;
        double width = 60 + random.nextInt(30);
        platforms.add(new Platform(x, y, width));
    }

    @Override public void tick() {
        super.tick();
        if (finished || state != GameState.PLAYING) return;
        long now = System.nanoTime();
        double dt = Math.min(0.04, Math.max(0, (now - lastNanos) / 1_000_000_000.0));
        lastNanos = now;
        double previousY = playerY;
        velocityY += 720 * dt;
        playerY += velocityY * dt;

        if (velocityY > 0) {
            for (Platform p : platforms) {
                boolean crossed = previousY + 20 <= p.y() && playerY + 20 >= p.y();
                if (crossed && Math.abs(playerX - p.x()) < p.width() / 2 + 10) {
                    playerY = p.y() - 20;
                    velocityY = -320;
                    score += 25;
                    markMove();
                    break;
                }
            }
        }

        if (playerY < cameraY + 105) {
            double shift = cameraY + 105 - playerY;
            cameraY += shift;
            score += (int) shift;
            double minimumY = platforms.stream().mapToDouble(Platform::y).min().orElse(playerY);
            while (minimumY > cameraY - 520) {
                minimumY -= 55 + random.nextInt(18);
                addPlatformAt(minimumY);
            }
            metrics.level(1 + Math.max(0, score / 500));
        }
        platforms.removeIf(p -> p.y() > cameraY + 360);
        if (playerY > cameraY + 310) finish(score);
    }

    private void steer(double amount) {
        if (finished || state != GameState.PLAYING) return;
        playerX = Math.max(-170, Math.min(170, playerX + amount));
    }

    @Override public void render(DrawContext c, int mx, int my, float delta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        drawHeader(c, "TOWER CLIMBER", status);
        int baseX = cx();
        int top = 55;
        int bottom = Math.min(300, mc.getWindow().getScaledHeight() - 20);
        c.fill(baseX - 190, top, baseX + 190, bottom, 0xFF20262B);
        for (Platform p : platforms) {
            int x = baseX + (int) p.x();
            int y = top + (int) (p.y() - cameraY);
            if (y < top - 10 || y > bottom) continue;
            c.fill(x - (int) (p.width() / 2), y, x + (int) (p.width() / 2), y + 7, 0xFFE67E22);
        }
        int px = baseX + (int) playerX;
        int py = top + (int) (playerY - cameraY);
        c.fill(px - 10, py, px + 10, py + 20, 0xFF55CC88);
        c.drawCenteredTextWithShadow(mc.textRenderer, net.minecraft.text.Text.literal("Height score: " + score), baseX, Math.min(bottom + 14, mc.getWindow().getScaledHeight() - 10), 0xFFFFFFFF);
    }

    @Override public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_R) begin();
        else if (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_A) steer(-18);
        else if (keyCode == GLFW.GLFW_KEY_RIGHT || keyCode == GLFW.GLFW_KEY_D) steer(18);
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) { return true; }
}
