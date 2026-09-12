package com.gafipro.minigames.game.puzzle;

import com.gafipro.minigames.game.BaseGame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Genuine Minesweeper with first-click safety, flood fill and flag accounting. */
public final class MinesweeperGame extends BaseGame {
    private int width = 16;
    private int height = 16;
    private int mineCount = 40;
    private boolean[][] mines;
    private boolean[][] open;
    private boolean[][] flags;
    private int opened;
    private int flagged;
    private boolean generated;
    private long startedNanos;
    private Difficulty difficulty = Difficulty.NORMAL;
    private enum Difficulty { EASY, NORMAL, HARD }

    @Override public String id() { return "minesweeper"; }
    @Override public String title() { return "Minesweeper"; }
    @Override public String category() { return "Puzzle"; }

    @Override
    public void start() {
        configureDifficulty();
        resetBoard();
        status = difficultyLabel() + " • Left click reveal • Right click flag • 1 Easy  2 Normal  3 Hard";
    }

    private void configureDifficulty() {
        switch (difficulty) {
            case EASY -> configureEasy();
            case NORMAL -> configureNormal();
            case HARD -> configureHard();
        }
    }

    private void configureEasy() { width = 9; height = 9; mineCount = 10; }
    private void configureNormal() { width = 16; height = 16; mineCount = 40; }
    private void configureHard() { width = 30; height = 16; mineCount = 99; }
    private String difficultyLabel() { return switch (difficulty) { case EASY -> "Easy"; case NORMAL -> "Normal"; case HARD -> "Hard"; }; }

    private void resetBoard() {
        mines = new boolean[height][width];
        open = new boolean[height][width];
        flags = new boolean[height][width];
        opened = 0;
        flagged = 0;
        generated = false;
        startedNanos = 0L;
    }

    private void placeMines(int safeX, int safeY) {
        List<Integer> candidates = new ArrayList<>(width * height);
        for (int y = 0; y < height; y++) for (int x = 0; x < width; x++) {
            if (Math.abs(x - safeX) <= 1 && Math.abs(y - safeY) <= 1) continue;
            candidates.add(y * width + x);
        }
        Collections.shuffle(candidates, random);
        for (int i = 0; i < mineCount && i < candidates.size(); i++) {
            int index = candidates.get(i);
            mines[index / width][index % width] = true;
        }
        generated = true;
        startedNanos = System.nanoTime();
    }

    private int adjacent(int x, int y) {
        int count = 0;
        for (int dy = -1; dy <= 1; dy++) for (int dx = -1; dx <= 1; dx++) {
            if (dx == 0 && dy == 0) continue;
            int nx = x + dx, ny = y + dy;
            if (nx >= 0 && nx < width && ny >= 0 && ny < height && mines[ny][nx]) count++;
        }
        return count;
    }

    private void reveal(int startX, int startY) {
        if (open[startY][startX] || flags[startY][startX] || mines[startY][startX]) return;
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        queue.add(startY * width + startX);
        while (!queue.isEmpty()) {
            int index = queue.removeFirst();
            int x = index % width, y = index / width;
            if (open[y][x] || flags[y][x] || mines[y][x]) continue;
            open[y][x] = true;
            opened++;
            if (adjacent(x, y) != 0) continue;
            for (int dy = -1; dy <= 1; dy++) for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue;
                int nx = x + dx, ny = y + dy;
                if (nx >= 0 && nx < width && ny >= 0 && ny < height && !open[ny][nx] && !flags[ny][nx] && !mines[ny][nx]) {
                    queue.addLast(ny * width + nx);
                }
            }
        }
    }

    private boolean won() { return opened >= width * height - mineCount; }

    private void explode() {
        for (int y = 0; y < height; y++) for (int x = 0; x < width; x++) if (mines[y][x]) open[y][x] = true;
        status = "Mine triggered.";
        finish(0);
    }

    private int cellSize() {
        MinecraftClient mc = MinecraftClient.getInstance();
        return Math.max(16, Math.min(28, Math.min((mc.getWindow().getScaledWidth() - 30) / width, (mc.getWindow().getScaledHeight() - 110) / height)));
    }

    private int boardLeft(int cell) { return cx() - width * cell / 2; }
    private int boardTop() { return 70; }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        var tr = MinecraftClient.getInstance().textRenderer;
        long elapsed = generated ? (System.nanoTime() - startedNanos) / 1_000_000_000L : 0L;
        drawHeader(c, "MINESWEEPER", "Mines: " + Math.max(0, mineCount - flagged) + " • Time: " + elapsed + "s");
        int cell = cellSize(), left = boardLeft(cell), top = boardTop();
        c.fill(left - 4, top - 4, left + width * cell + 4, top + height * cell + 4, 0xFF20252A);
        for (int y = 0; y < height; y++) for (int x = 0; x < width; x++) {
            int px = left + x * cell, py = top + y * cell;
            boolean shown = open[y][x];
            int bg = shown ? 0xFFC9CDD2 : 0xFF3C444D;
            c.fill(px + 1, py + 1, px + cell - 1, py + cell - 1, bg);
            if (!shown && flags[y][x]) {
                c.drawCenteredTextWithShadow(tr, Text.literal("⚑").formatted(Formatting.BOLD), px + cell / 2, py + Math.max(3, cell / 2 - 5), 0xFFFFD34D);
            } else if (shown && mines[y][x]) {
                c.drawCenteredTextWithShadow(tr, Text.literal("✹").formatted(Formatting.BOLD), px + cell / 2, py + Math.max(3, cell / 2 - 5), 0xFFE74C3C);
            } else if (shown) {
                int n = adjacent(x, y);
                if (n > 0) c.drawCenteredTextWithShadow(tr, Text.literal(Integer.toString(n)).formatted(Formatting.BOLD), px + cell / 2, py + Math.max(3, cell / 2 - 5), numberColor(n));
            }
        }
        if (!generated) c.drawCenteredTextWithShadow(tr, Text.literal("First click is safe").formatted(Formatting.GRAY), cx(), top + height * cell + 12, 0xFFFFFFFF);
    }

    private int numberColor(int n) {
        return switch (n) {
            case 1 -> 0xFF2B67C5;
            case 2 -> 0xFF2E8B57;
            case 3 -> 0xFFE04B3A;
            case 4 -> 0xFF6A3DA5;
            default -> 0xFF8E3A2C;
        };
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (finished) return true;
        int cell = cellSize(), left = boardLeft(cell), top = boardTop();
        if (!inside(mouseX, mouseY, left, top, width * cell, height * cell)) return true;
        int x = (int) ((mouseX - left) / cell), y = (int) ((mouseY - top) / cell);
        if (button == 1) {
            if (!open[y][x]) {
                flags[y][x] = !flags[y][x];
                flagged += flags[y][x] ? 1 : -1;
            }
            return true;
        }
        if (button != 0 || flags[y][x]) return true;
        if (!generated) placeMines(x, y);
        if (mines[y][x]) {
            explode();
            return true;
        }
        reveal(x, y);
        markMove();
        if (won()) {
            long millis = Math.max(1L, (System.nanoTime() - startedNanos) / 1_000_000L);
            int result = (int) Math.max(1L, 100_000L - millis * 3L - Math.max(0, flagged - mineCount) * 100L);
            status = "Board cleared!";
            finishWin(result);
        }
        return true;
    }

    @Override
    public void keyPressed(int key, int scan, int modifiers) {
        switch (key) {
            case 49 -> { difficulty = Difficulty.EASY; begin(); }
            case 50 -> { difficulty = Difficulty.NORMAL; begin(); }
            case 51 -> { difficulty = Difficulty.HARD; begin(); }
            case 82 -> begin();
            default -> { }
        }
    }
}
