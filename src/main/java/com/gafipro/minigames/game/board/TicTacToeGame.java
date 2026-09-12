package com.gafipro.minigames.game.board;

import com.gafipro.minigames.game.BaseGame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/** Tic-Tac-Toe with a real minimax AI and selectable difficulty. */
public final class TicTacToeGame extends BaseGame {
    private final int[] board = new int[9];
    private boolean playerTurn;
    private Difficulty difficulty = Difficulty.NORMAL;
    private int aiDelay;

    private enum Difficulty { EASY, NORMAL, HARD }

    @Override public String id() { return "tic_tac_toe"; }
    @Override public String title() { return "Tic-Tac-Toe"; }
    @Override public String category() { return "Board"; }

    @Override
    public void start() {
        for (int i = 0; i < board.length; i++) board[i] = 0;
        playerTurn = true;
        aiDelay = 0;
        status = "Normal • You are X • 1 Easy  2 Normal  3 Hard";
    }

    @Override
    public void tick() {
        super.tick();
        if (!finished && !playerTurn && --aiDelay <= 0) aiMove();
    }

    private void aiMove() {
        int move = switch (difficulty) {
            case EASY -> randomLegalMove();
            case NORMAL -> tacticalMove();
            case HARD -> minimaxRoot();
        };
        if (move < 0) { finishDraw(500); status = "Draw"; return; }
        board[move] = -1;
        markMove();
        playerTurn = true;
        resolve();
        if (!finished) status = "Your turn • X";
    }

    private int randomLegalMove() {
        List<Integer> free = legalMoves();
        return free.isEmpty() ? -1 : free.get(random.nextInt(free.size()));
    }

    private int tacticalMove() {
        for (int p : new int[]{-1, 1}) for (int move : legalMoves()) {
            board[move] = p;
            boolean win = wins(p);
            board[move] = 0;
            if (win) return move;
        }
        if (board[4] == 0) return 4;
        int[] corners = {0, 2, 6, 8};
        for (int corner : corners) if (board[corner] == 0) return corner;
        return randomLegalMove();
    }

    private int minimaxRoot() {
        int bestScore = Integer.MIN_VALUE, bestMove = -1;
        for (int move : legalMoves()) {
            board[move] = -1;
            int value = minimax(0, false);
            board[move] = 0;
            if (value > bestScore) { bestScore = value; bestMove = move; }
        }
        return bestMove;
    }

    private int minimax(int depth, boolean maximizing) {
        if (wins(-1)) return 10 - depth;
        if (wins(1)) return depth - 10;
        List<Integer> moves = legalMoves();
        if (moves.isEmpty()) return 0;
        int best = maximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for (int move : moves) {
            board[move] = maximizing ? -1 : 1;
            int score = minimax(depth + 1, !maximizing);
            board[move] = 0;
            best = maximizing ? Math.max(best, score) : Math.min(best, score);
        }
        return best;
    }

    private List<Integer> legalMoves() {
        List<Integer> moves = new ArrayList<>(9);
        for (int i = 0; i < 9; i++) if (board[i] == 0) moves.add(i);
        return moves;
    }

    private boolean wins(int p) {
        int[][] lines = {{0,1,2},{3,4,5},{6,7,8},{0,3,6},{1,4,7},{2,5,8},{0,4,8},{2,4,6}};
        for (int[] line : lines) if (board[line[0]] == p && board[line[1]] == p && board[line[2]] == p) return true;
        return false;
    }

    private void resolve() {
        if (wins(1)) { status = "You win!"; finishWin(1000); return; }
        if (wins(-1)) { status = "AI wins."; finish(0); return; }
        if (legalMoves().isEmpty()) { status = "Draw."; finishDraw(500); }
    }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        var tr = MinecraftClient.getInstance().textRenderer;
        drawHeader(c, "TIC-TAC-TOE", status);
        int cell = Math.min(72, Math.max(48, (MinecraftClient.getInstance().getWindow().getScaledHeight() - 150) / 3));
        int sx = cx() - cell * 3 / 2, sy = 80;
        for (int i = 0; i < 9; i++) {
            int col = i % 3, row = i / 3, x = sx + col * cell, y = sy + row * cell;
            c.fill(x + 2, y + 2, x + cell - 2, y + cell - 2, 0xFF30363D);
            int value = board[i];
            if (value != 0) {
                String symbol = value == 1 ? "X" : "O";
                int color = value == 1 ? 0xFF55FFFF : 0xFFFF7777;
                c.drawCenteredTextWithShadow(tr, Text.literal(symbol).formatted(Formatting.BOLD), x + cell / 2, y + cell / 2 - 5, color);
            }
        }
        c.drawCenteredTextWithShadow(tr, Text.literal("1 Easy   2 Normal   3 Hard   •   R Restart"), cx(), sy + cell * 3 + 12, 0xFFAAAAAA);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0 || finished || !playerTurn) return true;
        int cell = Math.min(72, Math.max(48, (MinecraftClient.getInstance().getWindow().getScaledHeight() - 150) / 3));
        int sx = cx() - cell * 3 / 2, sy = 80;
        for (int i = 0; i < 9; i++) {
            int x = sx + (i % 3) * cell, y = sy + (i / 3) * cell;
            if (inside(mx, my, x, y, cell, cell) && board[i] == 0) {
                board[i] = 1;
                markMove();
                playerTurn = false;
                resolve();
                if (!finished) {
                    status = "Opponent's turn...";
                    aiDelay = difficulty == Difficulty.HARD ? 4 : 7;
                }
                return true;
            }
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
