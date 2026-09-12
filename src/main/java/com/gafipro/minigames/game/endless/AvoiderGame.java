package com.gafipro.minigames.game.endless;

import com.gafipro.minigames.game.BaseGame;
import com.gafipro.minigames.game.GameState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/** Survival dodging game with accelerating, re-spawning hazards. */
public class AvoiderGame extends BaseGame {
    protected record Hazard(double x, double y, double vx, double vy, int size) {}
    protected final List<Hazard> hazards = new ArrayList<>();
    protected double playerX;
    protected long lastNanos;
    private long nextSpawnNanos;

    @Override public String id() { return "avoider"; }
    @Override public String title() { return "Avoider"; }
    @Override public String category() { return "Endless"; }

    @Override public void start() {
        hazards.clear();
        playerX = 0;
        lastNanos = System.nanoTime();
        nextSpawnNanos = lastNanos + 500_000_000L;
        metrics.level(1);
        status = "A/D or arrows • Survive the hazards";
    }

    protected double hazardSpeedMultiplier() { return 1.0 + Math.min(2.5, elapsedNanos() / 30_000_000_000.0); }

    protected void spawnHazard() {
        double x = -170 + random.nextDouble() * 340;
        double vx = random.nextDouble() * 22 - 11;
        double vy = 65 + random.nextDouble() * 70;
        hazards.add(new Hazard(x, 48, vx, vy, 12 + random.nextInt(8)));
    }

    @Override public void tick() {
        super.tick();
        if (finished || state != GameState.PLAYING) return;
        long now = System.nanoTime();
        double dt = Math.min(0.05, Math.max(0, (now - lastNanos) / 1_000_000_000.0));
        lastNanos = now;
        double speed = hazardSpeedMultiplier();
        List<Hazard> moved = new ArrayList<>(hazards.size());
        for (Hazard h : hazards) {
            double x = h.x() + h.vx() * dt;
            double y = h.y() + h.vy() * dt * speed;
            if (y < 275) moved.add(new Hazard(x, y, h.vx(), h.vy(), h.size()));
            else score += 10;
        }
        hazards.clear();
        hazards.addAll(moved);
        if (now >= nextSpawnNanos) {
            spawnHazard();
            long interval = (long) Math.max(160_000_000L, 600_000_000L - elapsedNanos() / 80L);
            nextSpawnNanos = now + interval;
            metrics.level(1 + (int) (elapsedNanos() / 15_000_000_000L));
        }
        score = Math.max(score, (int) (elapsedNanos() / 200_000_000L));
        if (collides()) finish(score);
    }

    protected boolean collides() {
        for (Hazard h : hazards) {
            if (Math.abs(playerX - h.x()) < h.size() + 12 && Math.abs(235 - h.y()) < h.size() + 12) return true;
        }
        return false;
    }

    protected void movePlayer(double amount) {
        if (finished || state != GameState.PLAYING) return;
        playerX = Math.max(-170, Math.min(170, playerX + amount));
        markMove();
    }

    @Override public void render(DrawContext c, int mx, int my, float delta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        drawHeader(c, title().toUpperCase(), status);
        int baseX = cx();
        int bottom = Math.max(100, Math.min(280, mc.getWindow().getScaledHeight() - 25));
        c.fill(baseX - 190, 70, baseX + 190, bottom, 0xFF20262B);
        int playerY = Math.max(80, bottom - 55);
        c.fill(baseX + (int) playerX - 10, playerY, baseX + (int) playerX + 10, playerY + 20, 0xFF55CC88);
        for (Hazard h : hazards) {
            int x = baseX + (int) h.x();
            int y = (int) h.y();
            if (x < baseX - 205 || x > baseX + 205 || y < 65 || y > bottom) continue;
            c.fill(x - h.size(), y - h.size(), x + h.size(), y + h.size(), 0xFFE74C3C);
        }
        c.drawCenteredTextWithShadow(mc.textRenderer, net.minecraft.text.Text.literal("Score: " + score), baseX, Math.max(75, bottom + 7), 0xFFFFFFFF);
    }

    @Override public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_R) begin();
        else if (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_A) movePlayer(-18);
        else if (keyCode == GLFW.GLFW_KEY_RIGHT || keyCode == GLFW.GLFW_KEY_D) movePlayer(18);
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || finished || state != GameState.PLAYING) return true;
        playerX = Math.max(-170, Math.min(170, mouseX - cx()));
        return true;
    }
}
