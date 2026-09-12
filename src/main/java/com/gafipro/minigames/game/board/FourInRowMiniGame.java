package com.gafipro.minigames.game.board;

import com.gafipro.minigames.game.BaseGame;
import com.gafipro.minigames.game.GameState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/** Compact 5x5 connect-four variant with a lightweight tactical AI. */
public final class FourInRowMiniGame extends BaseGame {
    private static final int SIZE = 5;
    private final int[][] board = new int[SIZE][SIZE];
    private boolean playerTurn = true;

    @Override public String id() { return "four_in_row_mini"; }
    @Override public String title() { return "Four-in-a-Row Mini"; }
    @Override public String category() { return "Board"; }

    @Override public void start() {
        for (int[] row : board) java.util.Arrays.fill(row, 0);
        playerTurn = true;
        status = "Connect four on the compact board.";
    }

    private int drop(int col, int p) {
        if (col < 0 || col >= SIZE) return -1;
        for (int row = SIZE - 1; row >= 0; row--) if (board[row][col] == 0) { board[row][col] = p; return row; }
        return -1;
    }

    private boolean won(int p) {
        for (int y = 0; y < SIZE; y++) for (int x = 0; x < SIZE; x++) if (board[y][x] == p)
            for (int[] d : new int[][]{{1,0},{0,1},{1,1},{1,-1}}) {
                int count = 1;
                for (int k = 1; k < 4; k++) { int xx = x + d[0] * k, yy = y + d[1] * k; if (xx < 0 || yy < 0 || xx >= SIZE || yy >= SIZE || board[yy][xx] != p) break; count++; }
                if (count >= 4) return true;
            }
        return false;
    }

    private boolean full() { for (int c = 0; c < SIZE; c++) if (board[0][c] == 0) return false; return true; }

    private int chooseAi() {
        List<Integer> legal = new ArrayList<>();
        for (int c = 0; c < SIZE; c++) if (board[0][c] == 0) legal.add(c);
        if (legal.isEmpty()) return -1;
        for (int c : legal) { int r = drop(c, 2); boolean w = won(2); if (r >= 0) board[r][c] = 0; if (w) return c; }
        for (int c : legal) { int r = drop(c, 1); boolean w = won(1); if (r >= 0) board[r][c] = 0; if (w) return c; }
        if (legal.contains(2)) return 2;
        return legal.get(random.nextInt(legal.size()));
    }

    @Override public void tick() { super.tick(); }

    @Override public void render(DrawContext c, int mx, int my, float delta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        drawHeader(c, "FOUR-IN-A-ROW MINI", status);
        int cell = Math.min(48, Math.max(32, (mc.getWindow().getScaledHeight() - 150) / SIZE));
        int sx = cx() - cell * SIZE / 2, sy = 78;
        c.fill(sx - 5, sy - 5, sx + cell * SIZE + 5, sy + cell * SIZE + 5, 0xFF2453A6);
        for (int y = 0; y < SIZE; y++) for (int x = 0; x < SIZE; x++) {
            int v = board[y][x];
            int color = v == 1 ? 0xFFE74C3C : v == 2 ? 0xFFF1C40F : 0xFF202A35;
            c.fill(sx + x * cell + 4, sy + y * cell + 4, sx + x * cell + cell - 4, sy + y * cell + cell - 4, color);
        }
    }

    @Override public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0 || finished || !playerTurn) return true;
        int cell = Math.min(48, Math.max(32, (MinecraftClient.getInstance().getWindow().getScaledHeight() - 150) / SIZE));
        int sx = cx() - cell * SIZE / 2, sy = 78;
        int col = (int) ((mx - sx) / cell);
        if (drop(col, 1) < 0) return true;
        markMove();
        if (won(1)) { finishWin(1000); return true; }
        if (full()) { finishDraw(500); return true; }
        playerTurn = false;
        int aiCol = chooseAi();
        if (aiCol >= 0) drop(aiCol, 2);
        if (won(2)) finish(0);
        else if (full()) finishDraw(500);
        else playerTurn = true;
        return true;
    }

    @Override public void keyPressed(int keyCode, int scanCode, int modifiers) { if (keyCode == GLFW.GLFW_KEY_R) begin(); }
}
