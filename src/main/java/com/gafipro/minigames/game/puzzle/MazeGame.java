package com.gafipro.minigames.game.puzzle;

import com.gafipro.minigames.game.BaseGame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Procedural perfect maze generated with randomized depth-first search. */
public final class MazeGame extends BaseGame {
    private int cols = 15, rows = 9;
    private boolean[][] openRight, openDown;
    private int playerX, playerY;
    private int moveCount;

    @Override public String id() { return "maze"; }
    @Override public String title() { return "Maze"; }
    @Override public String category() { return "Puzzle"; }

    @Override public void start() {
        generate();
        playerX = playerY = moveCount = 0;
        status = "Reach the green goal • Moves: 0 • R Restart • 3/4/5 Size";
    }

    private void generate() {
        openRight = new boolean[cols][rows];
        openDown = new boolean[cols][rows];
        boolean[][] visited = new boolean[cols][rows];
        ArrayDeque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{0, 0});
        visited[0][0] = true;
        while (!stack.isEmpty()) {
            int[] cell = stack.peek();
            int x = cell[0], y = cell[1];
            List<int[]> candidates = new ArrayList<>(4);
            if (x > 0 && !visited[x - 1][y]) candidates.add(new int[]{x - 1, y});
            if (x + 1 < cols && !visited[x + 1][y]) candidates.add(new int[]{x + 1, y});
            if (y > 0 && !visited[x][y - 1]) candidates.add(new int[]{x, y - 1});
            if (y + 1 < rows && !visited[x][y + 1]) candidates.add(new int[]{x, y + 1});
            if (candidates.isEmpty()) { stack.pop(); continue; }
            int[] next = candidates.get(random.nextInt(candidates.size()));
            int nx = next[0], ny = next[1];
            if (nx == x + 1) openRight[x][y] = true;
            else if (nx == x - 1) openRight[nx][ny] = true;
            else if (ny == y + 1) openDown[x][y] = true;
            else openDown[nx][ny] = true;
            visited[nx][ny] = true;
            stack.push(next);
        }
    }

    private boolean canMove(int dx, int dy) {
        if (dx == 1) return playerX + 1 < cols && openRight[playerX][playerY];
        if (dx == -1) return playerX > 0 && openRight[playerX - 1][playerY];
        if (dy == 1) return playerY + 1 < rows && openDown[playerX][playerY];
        return playerY > 0 && openDown[playerX][playerY - 1];
    }

    private void move(int dx, int dy) {
        if (finished || !canMove(dx, dy)) return;
        playerX += dx; playerY += dy; moveCount++; markMove();
        status = "Reach the green goal • Moves: " + moveCount + " • R Restart";
        if (playerX == cols - 1 && playerY == rows - 1) finishWin(Math.max(100, 5000 - moveCount * 10));
    }

    @Override public void keyPressed(int key, int scan, int modifiers) {
        if (key == GLFW.GLFW_KEY_R) { begin(); return; }
        if (finished) return;
        if (key == GLFW.GLFW_KEY_3) { cols = 11; rows = 7; begin(); return; }
        if (key == GLFW.GLFW_KEY_4) { cols = 15; rows = 9; begin(); return; }
        if (key == GLFW.GLFW_KEY_5) { cols = 21; rows = 13; begin(); return; }
        if (key == GLFW.GLFW_KEY_UP || key == GLFW.GLFW_KEY_W) move(0, -1);
        else if (key == GLFW.GLFW_KEY_DOWN || key == GLFW.GLFW_KEY_S) move(0, 1);
        else if (key == GLFW.GLFW_KEY_LEFT || key == GLFW.GLFW_KEY_A) move(-1, 0);
        else if (key == GLFW.GLFW_KEY_RIGHT || key == GLFW.GLFW_KEY_D) move(1, 0);
    }

    @Override public void render(DrawContext c, int mx, int my, float delta) {
        var mc = MinecraftClient.getInstance();
        drawHeader(c, "MAZE", status);
        int cell = Math.max(10, Math.min(30, (mc.getWindow().getScaledHeight() - 135) / rows));
        int width = cols * cell, height = rows * cell;
        int ox = cx() - width / 2, oy = 76;
        c.fill(ox, oy, ox + width, oy + height, 0xFF12161A);
        for (int x = 0; x < cols; x++) for (int y = 0; y < rows; y++) {
            int px = ox + x * cell, py = oy + y * cell;
            if (x == cols - 1 && y == rows - 1) c.fill(px + 2, py + 2, px + cell - 2, py + cell - 2, 0xFF55CC88);
            if (x == playerX && y == playerY) c.fill(px + 4, py + 4, px + cell - 4, py + cell - 4, 0xFF55AAFF);
            if (x + 1 < cols && openRight[x][y]) c.fill(px + cell - 1, py, px + cell + 1, py + cell, 0xFF273039);
            if (y + 1 < rows && openDown[x][y]) c.fill(px, py + cell - 1, px + cell, py + cell + 1, 0xFF273039);
        }
        for (int x = 0; x <= cols; x++) c.fill(ox + x * cell - 1, oy, ox + x * cell + 1, oy + height, 0xFF53616C);
        for (int y = 0; y <= rows; y++) c.fill(ox, oy + y * cell - 1, ox + width, oy + y * cell + 1, 0xFF53616C);
        c.drawCenteredTextWithShadow(mc.textRenderer, Text.literal("WASD / Arrow Keys • R Restart • 3/4/5 Size"), cx(), oy + height + 10, 0xFFAAAAAA);
    }
}
