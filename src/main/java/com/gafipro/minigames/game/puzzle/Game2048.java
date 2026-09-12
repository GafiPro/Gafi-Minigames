package com.gafipro.minigames.game.puzzle;

import com.gafipro.minigames.game.BaseGame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/** Correct 2048 implementation with non-remerging move resolution. */
public final class Game2048 extends BaseGame {
    private final int size;
    private int[][] board;
    private int points;
    private boolean reachedGoal;
    private boolean continueAfterGoal;

    public Game2048() { this(false); }
    public Game2048(boolean extreme) { size = extreme ? 5 : 4; }

    @Override public String id() { return size == 5 ? "2048_extreme" : "2048"; }
    @Override public String title() { return size == 5 ? "2048 Extreme" : "2048"; }
    @Override public String category() { return "Puzzle"; }

    @Override
    public void start() {
        board = new int[size][size];
        points = 0;
        reachedGoal = false;
        continueAfterGoal = true;
        status = "Arrow keys / WASD • R to restart";
        spawn();
        spawn();
    }

    private void spawn() {
        List<int[]> empty = new ArrayList<>();
        for (int y = 0; y < size; y++) for (int x = 0; x < size; x++) if (board[y][x] == 0) empty.add(new int[]{x, y});
        if (empty.isEmpty()) return;
        int[] p = empty.get(random.nextInt(empty.size()));
        board[p[1]][p[0]] = random.nextInt(10) == 0 ? 4 : 2;
    }

    private int[] sourceLine(int index, int direction) {
        int[] line = new int[size];
        for (int i = 0; i < size; i++) {
            int x = direction == 2 || direction == 3 ? index : i;
            int y = direction == 0 || direction == 1 ? index : i;
            if (direction == 1) y = size - 1 - i;
            if (direction == 3) x = size - 1 - i;
            line[i] = board[y][x];
        }
        return line;
    }

    private int[] mergeLine(int[] source) {
        int[] compact = new int[size];
        int count = 0;
        for (int value : source) if (value != 0) compact[count++] = value;
        int[] result = new int[size];
        int write = 0;
        for (int i = 0; i < count; i++) {
            if (i + 1 < count && compact[i] == compact[i + 1]) {
                result[write++] = compact[i] * 2;
                points += compact[i] * 2;
                i++;
            } else {
                result[write++] = compact[i];
            }
        }
        return result;
    }

    private boolean playMove(int direction) {
        int[][] before = copyBoard(board);
        for (int line = 0; line < size; line++) {
            int[] merged = mergeLine(sourceLine(line, direction));
            for (int i = 0; i < size; i++) {
                int x = direction == 2 || direction == 3 ? line : i;
                int y = direction == 0 || direction == 1 ? line : i;
                if (direction == 1) y = size - 1 - i;
                if (direction == 3) x = size - 1 - i;
                board[y][x] = merged[i];
            }
        }
        boolean changed = !same(before, board);
        if (!changed) return false;
        spawn();
        if (!reachedGoal && containsGoal()) {
            reachedGoal = true;
            status = "2048 reached! Keep going or press R to restart.";
            if (!continueAfterGoal) finishWin(points);
        }
        if (!canMove()) finish(reachedGoal ? points : points);
        return true;
    }

    private boolean containsGoal() {
        for (int[] row : board) for (int value : row) if (value >= 2048) return true;
        return false;
    }

    private boolean canMove() {
        for (int y = 0; y < size; y++) for (int x = 0; x < size; x++) {
            if (board[y][x] == 0) return true;
            if (x + 1 < size && board[y][x] == board[y][x + 1]) return true;
            if (y + 1 < size && board[y][x] == board[y + 1][x]) return true;
        }
        return false;
    }

    private static int[][] copyBoard(int[][] source) {
        int[][] copy = new int[source.length][];
        for (int i = 0; i < source.length; i++) copy[i] = source[i].clone();
        return copy;
    }

    private static boolean same(int[][] a, int[][] b) {
        for (int y = 0; y < a.length; y++) for (int x = 0; x < a.length; x++) if (a[y][x] != b[y][x]) return false;
        return true;
    }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        var tr = mc.textRenderer;
        drawHeader(c, title(), "Score: " + points + " • Moves: " + metrics.moves());
        int cell = Math.min(54, Math.max(34, (mc.getWindow().getScaledHeight() - 120) / size));
        int left = cx() - cell * size / 2;
        int top = 76;
        c.fill(left - 5, top - 5, left + cell * size + 5, top + cell * size + 5, 0xFF353535);
        for (int y = 0; y < size; y++) for (int x = 0; x < size; x++) {
            int value = board[y][x];
            int px = left + x * cell, py = top + y * cell;
            c.fill(px + 2, py + 2, px + cell - 2, py + cell - 2, tileColor(value));
            if (value > 0) c.drawCenteredTextWithShadow(tr, Text.literal(Integer.toString(value)).formatted(Formatting.BOLD), px + cell / 2, py + cell / 2 - 5, 0xFFFFFFFF);
        }
    }

    private int tileColor(int value) {
        if (value == 0) return 0xFF4A4A4A;
        if (value >= 2048) return 0xFFE3A83B;
        int shade = Math.max(70, 180 - Integer.numberOfTrailingZeros(value) * 7);
        return 0xFF000000 | (shade << 16) | ((shade - 20) << 8) | Math.max(20, shade - 40);
    }

    @Override
    public void keyPressed(int key, int scan, int mod) {
        if (key == GLFW.GLFW_KEY_R) {
            begin();
            return;
        }
        if (finished) return;
        int direction = switch (key) {
            case GLFW.GLFW_KEY_UP, GLFW.GLFW_KEY_W -> 0;
            case GLFW.GLFW_KEY_DOWN, GLFW.GLFW_KEY_S -> 1;
            case GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_A -> 2;
            case GLFW.GLFW_KEY_RIGHT, GLFW.GLFW_KEY_D -> 3;
            default -> -1;
        };
        if (direction >= 0 && playMove(direction)) markMove();
    }

    @Override public boolean mouseClicked(double x, double y, int button) { return true; }
}
