package com.gafipro.minigames.game.endless;

import com.gafipro.minigames.game.BaseGame;
import com.gafipro.minigames.game.GameState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/** Lane-based Frogger with moving hazards, goals and progressive speed. */
public final class FroggerGame extends BaseGame {
    private record Car(double x, int lane, double speed, int width) {}

    private static final int LANES = 8;
    private static final int COLUMNS = 9;
    private static final int CELL = 32;
    private final List<Car> cars = new ArrayList<>();
    private int row;
    private int col;
    private int level;
    private long lastNanos;

    @Override public String id() { return "frogger"; }
    @Override public String title() { return "Frogger"; }
    @Override public String category() { return "Endless"; }

    @Override public void start() {
        level = 1;
        row = LANES - 1;
        col = COLUMNS / 2;
        lastNanos = System.nanoTime();
        buildCars();
        metrics.level(level);
        status = "Arrow keys / WASD • Reach the top • Level: 1";
    }

    private void buildCars() {
        cars.clear();
        for (int lane = 1; lane < LANES - 1; lane++) {
            double speed = (lane % 2 == 0 ? 80 : -95) * (1 + level * 0.12);
            int count = 2 + (lane % 3 == 0 ? 1 : 0);
            for (int i = 0; i < count; i++) {
                double x = -170 + i * (340.0 / count) + random.nextInt(40) - 20;
                cars.add(new Car(x, lane, speed, 28));
            }
        }
    }

    @Override public void tick() {
        super.tick();
        if (finished || state != GameState.PLAYING) return;
        long now = System.nanoTime();
        double dt = Math.min(0.05, Math.max(0, (now - lastNanos) / 1_000_000_000.0));
        lastNanos = now;
        List<Car> moved = new ArrayList<>(cars.size());
        for (Car car : cars) {
            double x = car.x() + car.speed() * dt;
            if (x < -220) x = 220;
            if (x > 220) x = -220;
            moved.add(new Car(x, car.lane(), car.speed(), car.width()));
        }
        cars.clear();
        cars.addAll(moved);
        if (row > 0 && row < LANES - 1) {
            double playerX = (col - COLUMNS / 2) * CELL;
            for (Car car : cars) {
                double carY = car.lane() * CELL;
                double playerY = row * CELL;
                if (Math.abs(playerX - car.x()) < (car.width() + 18) / 2.0 && Math.abs(playerY - carY) < CELL * 0.45) {
                    finish(score);
                    return;
                }
            }
        }
    }

    @Override public void render(DrawContext c, int mx, int my, float delta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        drawHeader(c, "FROGGER", status);
        int baseX = cx() - COLUMNS * CELL / 2;
        int baseY = Math.min(72, mc.getWindow().getScaledHeight() - LANES * CELL - 40);
        for (int r = 0; r < LANES; r++) {
            int color = r == 0 ? 0xFF3A8A58 : r == LANES - 1 ? 0xFF4D5A3A : 0xFF28313A;
            c.fill(baseX, baseY + r * CELL, baseX + COLUMNS * CELL, baseY + (r + 1) * CELL - 1, color);
        }
        int px = baseX + col * CELL + 8;
        int py = baseY + row * CELL + 7;
        c.fill(px, py, px + 16, py + 18, 0xFF55CC88);
        for (Car car : cars) {
            int x = baseX + COLUMNS * CELL / 2 + (int) car.x() - car.width() / 2;
            int y = baseY + car.lane() * CELL + 5;
            c.fill(x, y, x + car.width(), y + 22, 0xFFE74C3C);
        }
        c.drawCenteredTextWithShadow(mc.textRenderer, net.minecraft.text.Text.literal("Level: " + level + " • Score: " + score), cx(), baseY + LANES * CELL + 8, 0xFFFFFFFF);
    }

    @Override public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_R) begin();
        else if (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_W) move(0, -1);
        else if (keyCode == GLFW.GLFW_KEY_DOWN || keyCode == GLFW.GLFW_KEY_S) move(0, 1);
        else if (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_A) move(-1, 0);
        else if (keyCode == GLFW.GLFW_KEY_RIGHT || keyCode == GLFW.GLFW_KEY_D) move(1, 0);
    }

    private void move(int dc, int dr) {
        if (finished || state != GameState.PLAYING) return;
        int nextCol = Math.max(0, Math.min(COLUMNS - 1, col + dc));
        int nextRow = Math.max(0, Math.min(LANES - 1, row + dr));
        if (nextCol == col && nextRow == row) return;
        col = nextCol;
        row = nextRow;
        score += 10;
        markMove();
        if (row == 0) {
            level++;
            score += 250 * level;
            metrics.level(level);
            row = LANES - 1;
            col = COLUMNS / 2;
            buildCars();
            status = "Safe! New level: " + level;
        }
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) { return true; }
}
