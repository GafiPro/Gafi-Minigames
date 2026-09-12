package com.gafipro.minigames.game.endless;

import com.gafipro.minigames.game.BaseGame;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Deque;

/** Deterministic grid-based Snake with real growth, collision and speed progression. */
public final class SnakeGame extends BaseGame {
    private static final int SIZE = 20;
    private final Deque<Cell> snake = new ArrayDeque<>();
    private int foodX, foodY;
    private int dx = 1, dy = 0;
    private int queuedDx = 1, queuedDy = 0;
    private long nextStep;
    private int apples;

    public record Cell(int x, int y) {}

    @Override public String id() { return "snake"; }
    @Override public String title() { return "Snake"; }
    @Override public String category() { return "Endless"; }

    @Override public void start() {
        snake.clear();
        int center = SIZE / 2;
        snake.addFirst(new Cell(center, center));
        snake.addLast(new Cell(center - 1, center));
        snake.addLast(new Cell(center - 2, center));
        dx = queuedDx = 1;
        dy = queuedDy = 0;
        apples = 0;
        metrics.level(1);
        spawnFood();
        nextStep = System.nanoTime();
    }

    private void spawnFood() {
        int free = SIZE * SIZE - snake.size();
        if (free <= 0) { finishWin(score + 1000); return; }
        int pick = random.nextInt(free);
        for (int y = 0; y < SIZE; y++) for (int x = 0; x < SIZE; x++) {
            if (occupied(x, y)) continue;
            if (pick-- == 0) { foodX = x; foodY = y; return; }
        }
    }

    private boolean occupied(int x, int y) {
        for (Cell c : snake) if (c.x() == x && c.y() == y) return true;
        return false;
    }

    private int stepMillis() { return Math.max(65, 180 - (metrics.level() - 1) * 12); }

    @Override public void tick() {
        super.tick();
        if (finished || System.nanoTime() < nextStep) return;
        dx = queuedDx;
        dy = queuedDy;
        Cell head = snake.peekFirst();
        int nx = head.x() + dx, ny = head.y() + dy;
        if (nx < 0 || ny < 0 || nx >= SIZE || ny >= SIZE) { finish(score); return; }
        boolean eating = nx == foodX && ny == foodY;
        if (!eating && isTail(nx, ny)) {
            snake.removeLast();
        } else if (occupied(nx, ny)) {
            finish(score);
            return;
        }
        snake.addFirst(new Cell(nx, ny));
        if (eating) {
            apples++;
            score += 100 + metrics.level() * 10;
            metrics.level(1 + apples / 5);
            if (snake.size() == SIZE * SIZE) { finishWin(score + 1000); return; }
            spawnFood();
        } else {
            snake.removeLast();
        }
        nextStep = System.nanoTime() + stepMillis() * 1_000_000L;
    }

    private boolean isTail(int x, int y) {
        Cell tail = snake.peekLast();
        return tail != null && tail.x() == x && tail.y() == y;
    }

    @Override public void keyPressed(int key, int scanCode, int modifiers) {
        if (key == GLFW.GLFW_KEY_R) { begin(); return; }
        int ndx = dx, ndy = dy;
        if (key == GLFW.GLFW_KEY_LEFT || key == GLFW.GLFW_KEY_A) { ndx = -1; ndy = 0; }
        else if (key == GLFW.GLFW_KEY_RIGHT || key == GLFW.GLFW_KEY_D) { ndx = 1; ndy = 0; }
        else if (key == GLFW.GLFW_KEY_UP || key == GLFW.GLFW_KEY_W) { ndx = 0; ndy = -1; }
        else if (key == GLFW.GLFW_KEY_DOWN || key == GLFW.GLFW_KEY_S) { ndx = 0; ndy = 1; }
        else return;
        if (ndx + dx == 0 && ndy + dy == 0) return;
        queuedDx = ndx;
        queuedDy = ndy;
    }

    @Override public void render(DrawContext c, int mx, int my, float delta) {
        drawHeader(c, "SNAKE", "Arrow keys / WASD  •  Score: " + score + "  •  Level: " + metrics.level());
        int cell = Math.min(18, Math.max(10, (height - 85) / SIZE));
        int ox = cx() - SIZE * cell / 2, oy = 72;
        c.fill(ox - 2, oy - 2, ox + SIZE * cell + 2, oy + SIZE * cell + 2, 0xFF101418);
        c.fill(ox, oy, ox + SIZE * cell, oy + SIZE * cell, 0xFF252B31);
        for (Cell s : snake) c.fill(ox + s.x() * cell + 1, oy + s.y() * cell + 1, ox + s.x() * cell + cell - 1, oy + s.y() * cell + cell - 1, 0xFF55CC88);
        c.fill(ox + foodX * cell + 2, oy + foodY * cell + 2, ox + foodX * cell + cell - 2, oy + foodY * cell + cell - 2, 0xFFE74C3C);
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) { return false; }
}
