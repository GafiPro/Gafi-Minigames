package com.gafipro.minigames.game.board;

import com.gafipro.minigames.game.BaseGame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/** Connect Four with gravity-correct moves and depth-limited minimax AI. */
public final class ConnectFourGame extends BaseGame {
    private static final int ROWS = 6, COLS = 7;
    private final int[][] board = new int[ROWS][COLS];
    private Difficulty difficulty = Difficulty.NORMAL;
    private boolean playerTurn = true;
    private int aiDelay;

    private enum Difficulty { EASY, NORMAL, HARD }

    @Override public String id() { return "connect_four"; }
    @Override public String title() { return "Connect Four"; }
    @Override public String category() { return "Board"; }

    @Override
    public void start() {
        for (int r = 0; r < ROWS; r++) for (int c = 0; c < COLS; c++) board[r][c] = 0;
        playerTurn = true;
        aiDelay = 0;
        status = "Normal • You are Red • 1 Easy  2 Normal  3 Hard";
    }

    @Override
    public void tick() {
        super.tick();
        if (!finished && !playerTurn && --aiDelay <= 0) {
            int column = chooseAiMove();
            if (drop(column, 2)) {
                markMove();
                if (hasWon(2)) { status = "AI wins."; finish(0); }
                else if (full()) { status = "Draw."; finishDraw(500); }
                else { playerTurn = true; status = "Your turn."; }
            }
        }
    }

    private int chooseAiMove() {
        List<Integer> legal = legalColumns(board);
        if (legal.isEmpty()) return -1;
        if (difficulty == Difficulty.EASY) return legal.get(random.nextInt(legal.size()));
        if (difficulty == Difficulty.NORMAL) {
            for (int c : legal) if (wouldWin(board, c, 2)) return c;
            for (int c : legal) if (wouldWin(board, c, 1)) return c;
            int[] preference = {3, 2, 4, 1, 5, 0, 6};
            for (int c : preference) if (legal.contains(c)) return c;
            return legal.get(random.nextInt(legal.size()));
        }
        int bestScore = Integer.MIN_VALUE, best = legal.get(0);
        for (int c : legal) {
            int[][] copy = copy(board);
            drop(copy, c, 2);
            int value = minimax(copy, Math.max(1, 6), false, Integer.MIN_VALUE, Integer.MAX_VALUE);
            if (value > bestScore) { bestScore = value; best = c; }
        }
        return best;
    }

    private int minimax(int[][] state, int depth, boolean maximizing, int alpha, int beta) {
        if (hasWon(state, 2)) return 100_000 + depth;
        if (hasWon(state, 1)) return -100_000 - depth;
        List<Integer> legal = legalColumns(state);
        if (depth == 0 || legal.isEmpty()) return evaluate(state);
        if (maximizing) {
            int best = Integer.MIN_VALUE;
            for (int c : legal) {
                int[][] next = copy(state); drop(next, c, 2);
                best = Math.max(best, minimax(next, depth - 1, false, alpha, beta));
                alpha = Math.max(alpha, best); if (alpha >= beta) break;
            }
            return best;
        }
        int best = Integer.MAX_VALUE;
        for (int c : legal) {
            int[][] next = copy(state); drop(next, c, 1);
            best = Math.min(best, minimax(next, depth - 1, true, alpha, beta));
            beta = Math.min(beta, best); if (alpha >= beta) break;
        }
        return best;
    }

    private int evaluate(int[][] state) {
        int score = 0;
        for (int r = 0; r < ROWS; r++) if (state[r][3] == 2) score += 6; else if (state[r][3] == 1) score -= 6;
        score += windows(state, 2) * 4;
        score -= windows(state, 1) * 5;
        return score;
    }

    private int windows(int[][] state, int player) {
        int score = 0;
        for (int r = 0; r < ROWS; r++) for (int c = 0; c < COLS; c++) {
            score += window(state, r, c, 1, 0, player);
            score += window(state, r, c, 0, 1, player);
            score += window(state, r, c, 1, 1, player);
            score += window(state, r, c, 1, -1, player);
        }
        return score;
    }

    private int window(int[][] state, int r, int c, int dr, int dc, int player) {
        int mine = 0, empty = 0;
        for (int i = 0; i < 4; i++) {
            int rr = r + dr * i, cc = c + dc * i;
            if (rr < 0 || rr >= ROWS || cc < 0 || cc >= COLS) return 0;
            if (state[rr][cc] == player) mine++; else if (state[rr][cc] == 0) empty++; else return 0;
        }
        return mine == 3 && empty == 1 ? 20 : mine == 2 && empty == 2 ? 5 : 0;
    }

    private boolean wouldWin(int[][] state, int col, int player) {
        int row = drop(state, col, player); if (row < 0) return false;
        boolean won = hasWon(state, player); state[row][col] = 0; return won;
    }

    private boolean drop(int col, int player) { return drop(board, col, player) >= 0; }
    private static int drop(int[][] state, int col, int player) {
        if (col < 0 || col >= COLS) return -1;
        for (int r = ROWS - 1; r >= 0; r--) if (state[r][col] == 0) { state[r][col] = player; return r; }
        return -1;
    }

    private boolean hasWon(int player) { return hasWon(board, player); }
    private static boolean hasWon(int[][] state, int player) {
        for (int r = 0; r < ROWS; r++) for (int c = 0; c < COLS; c++) if (state[r][c] == player)
            for (int[] d : new int[][]{{1,0},{0,1},{1,1},{1,-1}}) {
                int count = 1;
                for (int k = 1; k < 4; k++) {
                    int rr = r + d[0] * k, cc = c + d[1] * k;
                    if (rr < 0 || rr >= ROWS || cc < 0 || cc >= COLS || state[rr][cc] != player) break;
                    count++;
                }
                if (count >= 4) return true;
            }
        return false;
    }

    private boolean full() { return legalColumns(board).isEmpty(); }
    private static List<Integer> legalColumns(int[][] state) { List<Integer> out = new ArrayList<>(); for (int c = 0; c < COLS; c++) if (state[0][c] == 0) out.add(c); return out; }
    private static int[][] copy(int[][] source) { int[][] out = new int[ROWS][COLS]; for (int r = 0; r < ROWS; r++) out[r] = source[r].clone(); return out; }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        var tr = MinecraftClient.getInstance().textRenderer;
        drawHeader(c, "CONNECT FOUR", status);
        int cell = Math.min(44, Math.max(32, (MinecraftClient.getInstance().getWindow().getScaledHeight() - 150) / ROWS));
        int sx = cx() - cell * COLS / 2, sy = 76;
        c.fill(sx - 5, sy - 5, sx + cell * COLS + 5, sy + cell * ROWS + 5, 0xFF2453A6);
        for (int r = 0; r < ROWS; r++) for (int col = 0; col < COLS; col++) {
            int x = sx + col * cell, y = sy + r * cell;
            int value = board[r][col];
            int color = value == 1 ? 0xFFE74C3C : value == 2 ? 0xFFF1C40F : 0xFF202A35;
            c.fill(x + 4, y + 4, x + cell - 4, y + cell - 4, color);
        }
        for (int col = 0; col < COLS; col++) c.drawCenteredTextWithShadow(tr, Text.literal(Integer.toString(col + 1)), sx + col * cell + cell / 2, sy + cell * ROWS + 7, 0xFFAAAAAA);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0 || finished || !playerTurn) return true;
        int cell = Math.min(44, Math.max(32, (MinecraftClient.getInstance().getWindow().getScaledHeight() - 150) / ROWS));
        int sx = cx() - cell * COLS / 2, sy = 76;
        int col = (int) ((mx - sx) / cell);
        if (col < 0 || col >= COLS) return true;
        if (drop(col, 1)) {
            markMove();
            if (hasWon(1)) { status = "You win!"; finishWin(1000); }
            else if (full()) { status = "Draw."; finishDraw(500); }
            else { playerTurn = false; aiDelay = difficulty == Difficulty.HARD ? 3 : 6; status = "Opponent's turn..."; }
        }
        return true;
    }

    @Override
    public void keyPressed(int key, int scan, int modifiers) {
        if (key == GLFW.GLFW_KEY_R) { begin(); return; }
        if (finished) return;
        if (key == GLFW.GLFW_KEY_1) { difficulty = Difficulty.EASY; begin(); }
        else if (key == GLFW.GLFW_KEY_2) { difficulty = Difficulty.NORMAL; begin(); }
        else if (key == GLFW.GLFW_KEY_3) { difficulty = Difficulty.HARD; begin(); }
    }
}
