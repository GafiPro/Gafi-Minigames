package com.gafipro.minigames.game.puzzle;

import com.gafipro.minigames.game.BaseGame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Valid Sudoku generator and rule validator with three difficulty levels. */
public final class SudokuGame extends BaseGame {
    private final int[][] solution = new int[9][9];
    private final int[][] puzzle = new int[9][9];
    private final int[][] board = new int[9][9];
    private Difficulty difficulty = Difficulty.NORMAL;
    private int selected = -1;
    private int mistakes;
    private long startedNanos;

    private enum Difficulty {
        EASY(36), NORMAL(46), HARD(54);
        final int removals;
        Difficulty(int removals) { this.removals = removals; }
    }

    @Override public String id() { return "sudoku"; }
    @Override public String title() { return "Sudoku"; }
    @Override public String category() { return "Puzzle"; }

    @Override
    public void start() {
        generate();
        selected = -1;
        mistakes = 0;
        startedNanos = System.nanoTime();
        status = difficulty + " • Click a cell, then press 1–9 • F1 Easy  F2 Normal  F3 Hard";
    }

    private void generate() {
        clear(solution);
        clear(puzzle);
        clear(board);
        fillSolved(0);
        copyInto(solution, puzzle);
        List<Integer> cells = new ArrayList<>(81);
        for (int i = 0; i < 81; i++) cells.add(i);
        Collections.shuffle(cells, random);
        int removed = 0;
        for (int idx : cells) {
            int r = idx / 9, c = idx % 9, old = puzzle[r][c];
            puzzle[r][c] = 0;
            if (!hasUniqueSolution(puzzle)) puzzle[r][c] = old;
            else removed++;
            if (removed >= difficulty.removals) break;
        }
        copyInto(puzzle, board);
    }

    private boolean fillSolved(int cell) {
        if (cell == 81) return true;
        int r = cell / 9, c = cell % 9;
        List<Integer> values = new ArrayList<>(List.of(1,2,3,4,5,6,7,8,9));
        Collections.shuffle(values, random);
        for (int value : values) if (valid(solution, r, c, value)) {
            solution[r][c] = value;
            if (fillSolved(cell + 1)) return true;
        }
        solution[r][c] = 0;
        return false;
    }

    private boolean hasUniqueSolution(int[][] grid) { return countSolutions(copy(grid), 0, 2) == 1; }

    private int countSolutions(int[][] grid, int cell, int limit) {
        if (cell == 81) return 1;
        int r = cell / 9, c = cell % 9;
        if (grid[r][c] != 0) return countSolutions(grid, cell + 1, limit);
        int total = 0;
        for (int value = 1; value <= 9; value++) if (valid(grid, r, c, value)) {
            grid[r][c] = value;
            total += countSolutions(grid, cell + 1, limit);
            grid[r][c] = 0;
            if (total >= limit) return total;
        }
        return total;
    }

    private boolean valid(int[][] grid, int row, int col, int value) {
        for (int c = 0; c < 9; c++) if (c != col && grid[row][c] == value) return false;
        for (int r = 0; r < 9; r++) if (r != row && grid[r][col] == value) return false;
        int br = row / 3 * 3, bc = col / 3 * 3;
        for (int r = br; r < br + 3; r++) for (int c = bc; c < bc + 3; c++) if ((r != row || c != col) && grid[r][c] == value) return false;
        return true;
    }

    private boolean solved() {
        for (int r = 0; r < 9; r++) for (int c = 0; c < 9; c++) if (board[r][c] != solution[r][c]) return false;
        return true;
    }

    private int cellSize() { return Math.min(48, Math.max(32, (MinecraftClient.getInstance().getWindow().getScaledHeight() - 145) / 9)); }
    private int left(int cell) { return cx() - cell * 9 / 2; }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        var tr = MinecraftClient.getInstance().textRenderer;
        long seconds = Math.max(0L, (System.nanoTime() - startedNanos) / 1_000_000_000L);
        drawHeader(c, "SUDOKU", "Time: " + String.format("%02d:%02d", seconds / 60, seconds % 60) + " • Mistakes: " + mistakes);
        int cell = cellSize(), sx = left(cell), sy = 74;
        c.fill(sx - 3, sy - 3, sx + cell * 9 + 3, sy + cell * 9 + 3, 0xFF20252A);
        for (int r = 0; r < 9; r++) for (int col = 0; col < 9; col++) {
            int x = sx + col * cell, y = sy + r * cell;
            boolean given = puzzle[r][col] != 0, active = selected == r * 9 + col;
            c.fill(x + 1, y + 1, x + cell - 1, y + cell - 1, active ? 0xFF45657A : given ? 0xFF56616B : 0xFF303840);
            int value = board[r][col];
            if (value > 0) c.drawCenteredTextWithShadow(tr, Text.literal(Integer.toString(value)).formatted(Formatting.BOLD), x + cell / 2, y + cell / 2 - 5, given ? 0xFFFFFFFF : 0xFF55CCFF);
            if (col % 3 == 2 && col != 8) c.fill(x + cell - 1, sy, x + cell + 1, sy + cell * 9, 0xFFBFC7CE);
            if (r % 3 == 2 && r != 8) c.fill(sx, y + cell - 1, sx + cell * 9, y + cell + 1, 0xFFBFC7CE);
        }
        c.drawCenteredTextWithShadow(tr, Text.literal("Click a cell • 1–9 enter • Backspace clear • R restart"), cx(), sy + cell * 9 + 12, 0xFFAAAAAA);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0 || finished) return true;
        int cell = cellSize(), sx = left(cell), sy = 74;
        if (!inside(mx, my, sx, sy, cell * 9, cell * 9)) return true;
        int col = (int) ((mx - sx) / cell), row = (int) ((my - sy) / cell);
        if (puzzle[row][col] == 0) selected = row * 9 + col;
        return true;
    }

    @Override
    public void keyPressed(int key, int scan, int modifiers) {
        if (key == GLFW.GLFW_KEY_R) { begin(); return; }
        if (key == GLFW.GLFW_KEY_F1) { difficulty = Difficulty.EASY; begin(); return; }
        if (key == GLFW.GLFW_KEY_F2) { difficulty = Difficulty.NORMAL; begin(); return; }
        if (key == GLFW.GLFW_KEY_F3) { difficulty = Difficulty.HARD; begin(); return; }
        if (finished || selected < 0) return;
        int row = selected / 9, col = selected % 9;
        if (key >= GLFW.GLFW_KEY_1 && key <= GLFW.GLFW_KEY_9) {
            int value = key - GLFW.GLFW_KEY_0;
            if (valid(board, row, col, value) && solution[row][col] == value) board[row][col] = value;
            else { mistakes++; status = "That number is not valid for this cell."; }
            markMove();
            if (solved()) finishWin(Math.max(1, 100_000 - (int) ((System.nanoTime() - startedNanos) / 1_000_000L) - mistakes * 500));
        } else if (key == GLFW.GLFW_KEY_BACKSPACE || key == GLFW.GLFW_KEY_DELETE || key == GLFW.GLFW_KEY_0) {
            if (puzzle[row][col] == 0) board[row][col] = 0;
        }
    }

    private static void clear(int[][] grid) { for (int r = 0; r < 9; r++) for (int c = 0; c < 9; c++) grid[r][c] = 0; }
    private static void copyInto(int[][] source, int[][] target) { for (int r = 0; r < 9; r++) System.arraycopy(source[r], 0, target[r], 0, 9); }
    private static int[][] copy(int[][] source) { int[][] out = new int[9][9]; copyInto(source, out); return out; }
}
