package com.gafipro.minigames.game.board;

import com.gafipro.minigames.game.BaseGame;
import com.gafipro.minigames.game.GameState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;

/** Local Dots and Boxes with extra-turn scoring and a computer opponent. */
public final class DotsAndBoxesGame extends BaseGame {
    private static final int N = 5;
    private final boolean[][] horizontal = new boolean[N][N - 1];
    private final boolean[][] vertical = new boolean[N - 1][N];
    private final int[][] owner = new int[N - 1][N - 1];
    private int turn;
    private int humanScore;
    private int aiScore;

    @Override public String id() { return "dots_and_boxes"; }
    @Override public String title() { return "Dots and Boxes"; }
    @Override public String category() { return "Board"; }

    @Override public void start() {
        for (boolean[] row : horizontal) Arrays.fill(row, false);
        for (boolean[] row : vertical) Arrays.fill(row, false);
        for (int[] row : owner) Arrays.fill(row, 0);
        turn = 1;
        humanScore = aiScore = 0;
        status = "Your turn • Click a line";
    }

    private boolean boxComplete(int x, int y) {
        return horizontal[y][x] && horizontal[y + 1][x]
                && vertical[y][x] && vertical[y][x + 1];
    }

    private boolean boardFull() {
        for (int[] row : owner) for (int value : row) if (value == 0) return false;
        return true;
    }

    private boolean playLine(int axis, int x, int y, int player) {
        if (axis == 0) {
            if (y < 0 || y >= N || x < 0 || x >= N - 1 || horizontal[y][x]) return false;
            horizontal[y][x] = true;
        } else {
            if (x < 0 || x >= N || y < 0 || y >= N - 1 || vertical[y][x]) return false;
            vertical[y][x] = true;
        }
        boolean claimed = claimBoxes(player);
        if (!claimed) turn = 3 - player;
        return true;
    }

    private boolean claimBoxes(int player) {
        boolean claimed = false;
        for (int y = 0; y < N - 1; y++) for (int x = 0; x < N - 1; x++) {
            if (owner[y][x] == 0 && boxComplete(x, y)) {
                owner[y][x] = player;
                if (player == 1) humanScore++; else aiScore++;
                score = humanScore * 100;
                claimed = true;
            }
        }
        return claimed;
    }

    private int[] findBestAiLine() {
        int[] first = null;
        for (int y = 0; y < N; y++) for (int x = 0; x < N - 1; x++) if (!horizontal[y][x]) {
            if (wouldCompleteBox(0, x, y)) return new int[]{0, x, y};
            if (first == null) first = new int[]{0, x, y};
        }
        for (int y = 0; y < N - 1; y++) for (int x = 0; x < N; x++) if (!vertical[y][x]) {
            if (wouldCompleteBox(1, x, y)) return new int[]{1, x, y};
            if (first == null) first = new int[]{1, x, y};
        }
        return first;
    }

    private boolean wouldCompleteBox(int axis, int x, int y) {
        if (axis == 0) {
            if (y > 0 && !horizontal[y - 1][x] && vertical[y - 1][x] && vertical[y - 1][x + 1]) return true;
            return y < N - 1 && !horizontal[y + 1][x] && vertical[y][x] && vertical[y][x + 1];
        }
        if (x > 0 && !vertical[y][x - 1] && horizontal[y][x - 1] && horizontal[y + 1][x - 1]) return true;
        return x < N - 1 && !vertical[y][x + 1] && horizontal[y][x] && horizontal[y + 1][x];
    }

    private void aiTurn() {
        while (!finished && turn == 2) {
            int[] move = findBestAiLine();
            if (move == null) { end(); return; }
            playLine(move[0], move[1], move[2], 2);
            if (boardFull()) { end(); return; }
        }
        if (!finished) status = "Your turn • Click a line";
    }

    private void end() {
        if (humanScore > aiScore) finishWin(humanScore * 100 + (humanScore - aiScore) * 25);
        else if (humanScore == aiScore) finishDraw(humanScore * 100);
        else finish( humanScore * 100 );
    }

    @Override public void render(DrawContext c, int mx, int my, float delta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        drawHeader(c, "DOTS AND BOXES", status + " • You: " + humanScore + "  CPU: " + aiScore);
        int s = 52, ox = cx() - (N - 1) * s / 2, oy = 75;
        for (int y = 0; y < N; y++) for (int x = 0; x < N; x++)
            c.fill(ox + x * s - 3, oy + y * s - 3, ox + x * s + 3, oy + y * s + 3, 0xFFFFFFFF);
        for (int y = 0; y < N; y++) for (int x = 0; x < N - 1; x++) if (horizontal[y][x])
            c.fill(ox + x * s, oy + y * s - 2, ox + (x + 1) * s, oy + y * s + 2, 0xFFFFFFFF);
        for (int y = 0; y < N - 1; y++) for (int x = 0; x < N; x++) if (vertical[y][x])
            c.fill(ox + x * s - 2, oy + y * s, ox + x * s + 2, oy + (y + 1) * s, 0xFFFFFFFF);
        for (int y = 0; y < N - 1; y++) for (int x = 0; x < N - 1; x++) if (owner[y][x] != 0)
            c.fill(ox + x * s + 7, oy + y * s + 7, ox + (x + 1) * s - 7, oy + (y + 1) * s - 7,
                    owner[y][x] == 1 ? 0x66E74C3C : 0x6655AADD);
        c.drawCenteredTextWithShadow(mc.textRenderer, net.minecraft.text.Text.literal("R restart"), cx(), oy + N * s + 12, 0xFFCCCCCC);
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || finished || turn != 1) return true;
        int s = 52, ox = cx() - (N - 1) * s / 2, oy = 75;
        double gx = (mouseX - ox) / s, gy = (mouseY - oy) / s;
        int x = (int) Math.round(gx), y = (int) Math.round(gy);
        if (x >= 0 && x < N - 1 && y >= 0 && y < N && Math.abs(gy - y) < 0.20) {
            if (playLine(0, x, y, 1)) { markMove(); if (boardFull()) end(); else aiTurn(); }
            return true;
        }
        if (x >= 0 && x < N && y >= 0 && y < N - 1 && Math.abs(gx - x) < 0.20) {
            if (playLine(1, x, y, 1)) { markMove(); if (boardFull()) end(); else aiTurn(); }
        }
        return true;
    }

    @Override public void keyPressed(int keyCode, int scanCode, int modifiers) { if (keyCode == GLFW.GLFW_KEY_R) begin(); }
}
