package com.gafipro.minigames.game.puzzle;

import com.gafipro.minigames.game.BaseGame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Focused implementations for the puzzle games that share lightweight board-style UI. */
public final class PuzzleCollection {
    private PuzzleCollection() {}

    private static final int PANEL = 0xFF303840;
    private static final int OFF = 0xFF3B424A;
    private static final int FG = 0xFFFFFFFF;

    public static final class SlidingPuzzle extends BaseGame {
        private int size = 4;
        private int[] board;
        private int empty;
        private int moves;

        @Override public String id() { return "sliding_puzzle"; }
        @Override public String title() { return "Sliding Puzzle"; }
        @Override public String category() { return "Puzzle"; }

        @Override public void start() {
            board = new int[size * size];
            for (int i = 0; i < board.length; i++) board[i] = i;
            empty = board.length - 1;
            moves = 0;
            int previous = -1;
            for (int i = 0; i < 500; i++) {
                List<Integer> options = neighbors(empty);
                options.remove((Integer) previous);
                int next = options.get(random.nextInt(options.size()));
                swap(empty, next);
                previous = empty;
                empty = next;
            }
            if (solved()) { swap(empty, neighbors(empty).get(0)); empty = neighbors(empty).get(0); }
            status = "Click a tile next to the empty space • Moves: 0 • R Restart";
        }

        private List<Integer> neighbors(int index) {
            List<Integer> result = new ArrayList<>(4);
            int x = index % size, y = index / size;
            if (x > 0) result.add(index - 1);
            if (x + 1 < size) result.add(index + 1);
            if (y > 0) result.add(index - size);
            if (y + 1 < size) result.add(index + size);
            return result;
        }

        private void swap(int a, int b) { int t = board[a]; board[a] = board[b]; board[b] = t; }
        private boolean solved() { for (int i = 0; i < board.length; i++) if (board[i] != i) return false; return true; }

        @Override public boolean mouseClicked(double mx, double my, int button) {
            if (button != 0 || finished) return true;
            int cell = Math.min(58, Math.max(32, (MinecraftClient.getInstance().getWindow().getScaledHeight() - 145) / size));
            int ox = cx() - size * cell / 2, oy = 82;
            int x = (int) ((mx - ox) / cell), y = (int) ((my - oy) / cell);
            if (x < 0 || y < 0 || x >= size || y >= size) return true;
            int index = y * size + x;
            if (!neighbors(empty).contains(index)) return true;
            swap(empty, index);
            empty = index;
            moves++;
            markMove();
            status = "Click a tile next to the empty space • Moves: " + moves + " • R Restart";
            if (solved()) finishWin(Math.max(100, 10000 - moves * 12));
            return true;
        }

        @Override public void render(DrawContext c, int mx, int my, float delta) {
            drawHeader(c, "SLIDING PUZZLE", status);
            int cell = Math.min(58, Math.max(32, (MinecraftClient.getInstance().getWindow().getScaledHeight() - 145) / size));
            int ox = cx() - size * cell / 2, oy = 82;
            for (int i = 0; i < board.length; i++) {
                int x = ox + (i % size) * cell, y = oy + (i / size) * cell;
                int value = board[i];
                c.fill(x + 1, y + 1, x + cell - 2, y + cell - 2, value == board.length - 1 ? OFF : 0xFF4B6A88);
                if (value != board.length - 1) c.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer,
                        Text.literal(String.valueOf(value + 1)), x + cell / 2, y + cell / 2 - 5, FG);
            }
        }

        @Override public void keyPressed(int key, int scan, int modifiers) {
            if (key == GLFW.GLFW_KEY_R) begin();
            else if (key == GLFW.GLFW_KEY_3 || key == GLFW.GLFW_KEY_4 || key == GLFW.GLFW_KEY_5) {
                size = key == GLFW.GLFW_KEY_3 ? 3 : key == GLFW.GLFW_KEY_4 ? 4 : 5;
                begin();
            }
        }
    }

    public static final class LightsOut extends BaseGame {
        private final boolean[][] on = new boolean[5][5];
        private int moves;

        @Override public String id() { return "lights_out"; }
        @Override public String title() { return "Lights Out"; }
        @Override public String category() { return "Puzzle"; }

        @Override public void start() {
            for (int x = 0; x < 5; x++) Arrays.fill(on[x], false);
            moves = 0;
            // Build every board from the solved state by applying legal presses, guaranteeing solvability.
            for (int i = 0, presses = 8 + random.nextInt(14); i < presses; i++) toggle(random.nextInt(5), random.nextInt(5));
            status = "Turn every tile dark • Moves: 0 • R Restart";
        }

        private void toggle(int x, int y) {
            int[][] dirs = {{0,0},{1,0},{-1,0},{0,1},{0,-1}};
            for (int[] d : dirs) {
                int nx = x + d[0], ny = y + d[1];
                if (nx >= 0 && ny >= 0 && nx < 5 && ny < 5) on[nx][ny] = !on[nx][ny];
            }
        }
        private boolean solved() { for (boolean[] row : on) for (boolean v : row) if (v) return false; return true; }

        @Override public boolean mouseClicked(double mx, double my, int button) {
            if (button != 0 || finished) return true;
            int cell = Math.min(52, Math.max(34, (MinecraftClient.getInstance().getWindow().getScaledHeight() - 145) / 5));
            int ox = cx() - cell * 5 / 2, oy = 82;
            int x = (int) ((mx - ox) / cell), y = (int) ((my - oy) / cell);
            if (x < 0 || y < 0 || x >= 5 || y >= 5) return true;
            toggle(x, y);
            moves++;
            markMove();
            status = "Turn every tile dark • Moves: " + moves + " • R Restart";
            if (solved()) finishWin(Math.max(100, 1200 - moves * 20));
            return true;
        }

        @Override public void render(DrawContext c, int mx, int my, float delta) {
            drawHeader(c, "LIGHTS OUT", status);
            int cell = Math.min(52, Math.max(34, (MinecraftClient.getInstance().getWindow().getScaledHeight() - 145) / 5));
            int ox = cx() - cell * 5 / 2, oy = 82;
            for (int y = 0; y < 5; y++) for (int x = 0; x < 5; x++) {
                c.fill(ox + x * cell + 1, oy + y * cell + 1, ox + x * cell + cell - 2, oy + y * cell + cell - 2,
                        on[x][y] ? 0xFFF1C40F : OFF);
            }
        }

        @Override public void keyPressed(int key, int scan, int modifiers) { if (key == GLFW.GLFW_KEY_R) begin(); }
    }

    public static final class Nonogram extends BaseGame {
        private int size;
        private boolean[][] solution, filled, markedWrong;
        private int[][] rowClues, colClues;
        private int mistakes;

        @Override public String id() { return "nonogram"; }
        @Override public String title() { return "Nonogram"; }
        @Override public String category() { return "Puzzle"; }

        @Override public void start() {
            size = 10;
            solution = new boolean[size][size];
            filled = new boolean[size][size];
            markedWrong = new boolean[size][size];
            mistakes = 0;
            do {
                for (int y = 0; y < size; y++) for (int x = 0; x < size; x++) solution[x][y] = random.nextDouble() < 0.45;
            } while (emptySolution());
            rowClues = cluesForRows();
            colClues = cluesForCols();
            status = "Left click fill • Right click mark empty • Mistakes: 0";
        }

        private boolean emptySolution() { for (boolean[] row : solution) for (boolean v : row) if (v) return false; return true; }
        private int[][] cluesForRows() { int[][] c = new int[size][]; for (int y = 0; y < size; y++) c[y] = lineClues(y, true); return c; }
        private int[][] cluesForCols() { int[][] c = new int[size][]; for (int x = 0; x < size; x++) c[x] = lineClues(x, false); return c; }
        private int[] lineClues(int index, boolean row) {
            List<Integer> values = new ArrayList<>();
            int run = 0;
            for (int i = 0; i < size; i++) {
                boolean v = row ? solution[i][index] : solution[index][i];
                if (v) run++; else if (run > 0) { values.add(run); run = 0; }
            }
            if (run > 0) values.add(run);
            if (values.isEmpty()) values.add(0);
            return values.stream().mapToInt(Integer::intValue).toArray();
        }

        private boolean complete() { for (int x = 0; x < size; x++) for (int y = 0; y < size; y++) if (filled[x][y] != solution[x][y]) return false; return true; }

        @Override public boolean mouseClicked(double mx, double my, int button) {
            if (finished) return true;
            int cell = Math.min(29, Math.max(18, (MinecraftClient.getInstance().getWindow().getScaledWidth() - 120) / (size + 3)));
            int ox = cx() - (cell * (size + 3)) / 2 + cell * 3, oy = 82;
            int x = (int) ((mx - ox) / cell), y = (int) ((my - oy) / cell);
            if (x < 0 || y < 0 || x >= size || y >= size) return true;
            if (button == 0) filled[x][y] = !filled[x][y];
            else if (button == 1) markedWrong[x][y] = !markedWrong[x][y];
            if (filled[x][y] != solution[x][y]) { mistakes++; markedWrong[x][y] = true; status = "That cell does not match the solution • Mistakes: " + mistakes; }
            if (complete()) finishWin(Math.max(100, 5000 - mistakes * 100));
            return true;
        }

        @Override public void render(DrawContext c, int mx, int my, float delta) {
            var mc = MinecraftClient.getInstance();
            drawHeader(c, "NONOGRAM", status);
            int cell = Math.min(29, Math.max(18, (mc.getWindow().getScaledWidth() - 120) / (size + 3)));
            int ox = cx() - (cell * (size + 3)) / 2 + cell * 3, oy = 82;
            for (int y = 0; y < size; y++) for (int x = 0; x < size; x++) {
                int px = ox + x * cell, py = oy + y * cell;
                int tile = filled[x][y] ? 0xFF4B6A88 : OFF;
                if (markedWrong[x][y]) tile = 0xFF8E3B3B;
                c.fill(px + 1, py + 1, px + cell - 2, py + cell - 2, tile);
            }
            for (int i = 0; i < size; i++) {
                c.drawTextWithShadow(mc.textRenderer, Text.literal(join(rowClues[i])), cx() - cell * (size + 3) / 2, oy + i * cell + 6, FG);
                c.drawTextWithShadow(mc.textRenderer, Text.literal(join(colClues[i])), ox + i * cell + 4, oy - 18, FG);
            }
        }

        private String join(int[] values) { StringBuilder b = new StringBuilder(); for (int i = 0; i < values.length; i++) { if (i > 0) b.append(' '); b.append(values[i]); } return b.toString(); }
        @Override public void keyPressed(int key, int scan, int modifiers) { if (key == GLFW.GLFW_KEY_R) begin(); }
    }

    public static final class Match3 extends BaseGame {
        private static final int SIZE = 8;
        private final int[][] grid = new int[SIZE][SIZE];
        private int selectedX = -1, selectedY = -1;
        private int movesLeft;

        @Override public String id() { return "match_3"; }
        @Override public String title() { return "Match-3"; }
        @Override public String category() { return "Puzzle"; }

        @Override public void start() {
            movesLeft = 30;
            for (int x = 0; x < SIZE; x++) for (int y = 0; y < SIZE; y++) {
                do { grid[x][y] = random.nextInt(6); } while (createsImmediateMatch(x, y));
            }
            selectedX = selectedY = -1;
            status = "Swap adjacent tiles to make matches • Moves: 30";
        }

        private boolean createsImmediateMatch(int x, int y) {
            if (x >= 2 && grid[x - 1][y] == grid[x - 2][y] && grid[x - 1][y] == grid[x][y]) return true;
            return y >= 2 && grid[x][y - 1] == grid[x][y - 2] && grid[x][y - 1] == grid[x][y];
        }

        private boolean adjacent(int x1, int y1, int x2, int y2) { return Math.abs(x1 - x2) + Math.abs(y1 - y2) == 1; }
        private void swap(int x1, int y1, int x2, int y2) { int t = grid[x1][y1]; grid[x1][y1] = grid[x2][y2]; grid[x2][y2] = t; }

        private boolean resolveMatches() {
            boolean anyCascade = false;
            while (true) {
                boolean[][] remove = findMatches();
                boolean any = false;
                for (int x = 0; x < SIZE; x++) for (int y = 0; y < SIZE; y++) if (remove[x][y]) any = true;
                if (!any) break;
                anyCascade = true;
                int count = 0;
                for (int x = 0; x < SIZE; x++) for (int y = 0; y < SIZE; y++) if (remove[x][y]) { grid[x][y] = -1; count++; }
                score += count * 100;
                for (int x = 0; x < SIZE; x++) {
                    int write = SIZE - 1;
                    for (int y = SIZE - 1; y >= 0; y--) if (grid[x][y] >= 0) grid[x][write--] = grid[x][y];
                    while (write >= 0) grid[x][write--] = random.nextInt(6);
                }
            }
            return anyCascade;
        }

        private boolean[][] findMatches() {
            boolean[][] remove = new boolean[SIZE][SIZE];
            for (int y = 0; y < SIZE; y++) {
                int run = 1;
                for (int x = 1; x <= SIZE; x++) {
                    if (x < SIZE && grid[x][y] == grid[x - 1][y]) run++;
                    else { if (run >= 3) for (int i = 1; i <= run; i++) remove[x - i][y] = true; run = 1; }
                }
            }
            for (int x = 0; x < SIZE; x++) {
                int run = 1;
                for (int y = 1; y <= SIZE; y++) {
                    if (y < SIZE && grid[x][y] == grid[x][y - 1]) run++;
                    else { if (run >= 3) for (int i = 1; i <= run; i++) remove[x][y - i] = true; run = 1; }
                }
            }
            return remove;
        }

        private boolean createsMatchAfterSwap(int x1, int y1, int x2, int y2) { swap(x1,y1,x2,y2); boolean result = false; boolean[][] r = findMatches(); for (boolean[] row : r) for (boolean v : row) if (v) result = true; swap(x1,y1,x2,y2); return result; }

        @Override public boolean mouseClicked(double mx, double my, int button) {
            if (button != 0 || finished || movesLeft <= 0) return true;
            int cell = Math.min(48, Math.max(28, (MinecraftClient.getInstance().getWindow().getScaledHeight() - 145) / SIZE));
            int ox = cx() - SIZE * cell / 2, oy = 80;
            int x = (int) ((mx - ox) / cell), y = (int) ((my - oy) / cell);
            if (x < 0 || y < 0 || x >= SIZE || y >= SIZE) return true;
            if (selectedX < 0) { selectedX = x; selectedY = y; return true; }
            if (!adjacent(selectedX, selectedY, x, y)) { selectedX = x; selectedY = y; return true; }
            if (createsMatchAfterSwap(selectedX, selectedY, x, y)) {
                swap(selectedX, selectedY, x, y);
                resolveMatches();
                movesLeft--;
                markMove();
                score += 50;
            }
            selectedX = selectedY = -1;
            status = "Swap adjacent tiles to make matches • Moves: " + movesLeft + " • Score: " + score;
            if (movesLeft <= 0) finish(Math.max(0, score));
            return true;
        }

        @Override public void render(DrawContext c, int mx, int my, float delta) {
            drawHeader(c, "MATCH-3", status);
            int cell = Math.min(48, Math.max(28, (MinecraftClient.getInstance().getWindow().getScaledHeight() - 145) / SIZE));
            int ox = cx() - SIZE * cell / 2, oy = 80;
            for (int x = 0; x < SIZE; x++) for (int y = 0; y < SIZE; y++) {
                int tile = switch (grid[x][y]) { case 0 -> 0xFF4D79A8; case 1 -> 0xFF8C4BAA; case 2 -> 0xFFCC7A2D; case 3 -> 0xFF3F9B76; case 4 -> 0xFFB84B4B; default -> 0xFF6B6B6B; };
                if (x == selectedX && y == selectedY) tile = 0xFFF1C40F;
                c.fill(ox + x * cell + 2, oy + y * cell + 2, ox + x * cell + cell - 2, oy + y * cell + cell - 2, tile);
            }
        }
        @Override public void keyPressed(int key, int scan, int modifiers) { if (key == GLFW.GLFW_KEY_R) begin(); }
    }

    public static final class WordSearch extends BaseGame {
        private static final String[] WORDS = {"MINECRAFT","CREEPER","DIAMOND","REDSTONE","VILLAGER","PICKAXE","NETHER","ENDER","PORTAL","ZOMBIE","SKELETON","DRAGON","FOREST","DESERT","OCEAN","VILLAGE","FURNACE","CRAFTING","AXOLOTL","GOLEM","BEACON","ELYTRA","SHIELD","TRIDENT","MUSHROOM","WATER","LAVA","CAVE","SPAWNER","ENCHANT"};
        private final char[][] grid = new char[12][12];
        private final List<String> targets = new ArrayList<>();
        private int found;

        @Override public String id() { return "word_search"; }
        @Override public String title() { return "Word Search"; }
        @Override public String category() { return "Puzzle"; }

        @Override public void start() {
            for (int y = 0; y < 12; y++) Arrays.fill(grid[y], '.');
            targets.clear(); found = 0;
            List<String> pool = new ArrayList<>(List.of(WORDS));
            Collections.shuffle(pool, random);
            for (String word : pool) if (place(word) && targets.size() < 8) targets.add(word);
            for (int y = 0; y < 12; y++) for (int x = 0; x < 12; x++) if (grid[x][y] == '.') grid[x][y] = (char) ('A' + random.nextInt(26));
            status = "Find the words • Found: 0 / " + targets.size();
        }

        private boolean place(String word) {
            int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}};
            for (int tries = 0; tries < 60; tries++) {
                int dxdy = random.nextInt(dirs.length), x = random.nextInt(12), y = random.nextInt(12), dx = dirs[dxdy][0], dy = dirs[dxdy][1];
                int endX = x + dx * (word.length() - 1), endY = y + dy * (word.length() - 1);
                if (endX < 0 || endY < 0 || endX >= 12 || endY >= 12) continue;
                boolean ok = true;
                for (int i = 0; i < word.length(); i++) { char existing = grid[x + i * dx][y + i * dy]; if (existing != '.' && existing != word.charAt(i)) { ok = false; break; } }
                if (!ok) continue;
                for (int i = 0; i < word.length(); i++) grid[x + i * dx][y + i * dy] = word.charAt(i);
                return true;
            }
            return false;
        }

        @Override public boolean mouseClicked(double mx, double my, int button) {
            if (button != 0 || finished) return true;
            int cell = Math.min(28, Math.max(18, (MinecraftClient.getInstance().getWindow().getScaledWidth() - 40) / 12));
            int ox = cx() - 6 * cell, oy = 78;
            int x = (int) ((mx - ox) / cell), y = (int) ((my - oy) / cell);
            if (x < 0 || y < 0 || x >= 12 || y >= 12) return true;
            // Selecting a start and end cell is deliberately simple; validate every straight line between them.
            if (startX < 0) { startX = x; startY = y; return true; }
            String word = readLine(startX, startY, x, y);
            if (targets.contains(word) && !selectedWords.contains(word)) { selectedWords.add(word); found++; score += word.length() * 100; }
            startX = startY = -1;
            status = "Find the words • Found: " + found + " / " + targets.size();
            if (found == targets.size()) finishWin(score);
            return true;
        }

        private int startX = -1, startY = -1;
        private final List<String> selectedWords = new ArrayList<>();

        private String readLine(int x1, int y1, int x2, int y2) {
            int dx = Integer.compare(x2, x1), dy = Integer.compare(y2, y1);
            int len = Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1)) + 1;
            if (!(x1 == x2 || y1 == y2 || Math.abs(x2 - x1) == Math.abs(y2 - y1))) return "";
            StringBuilder b = new StringBuilder(len);
            for (int i = 0; i < len; i++) b.append(grid[x1 + dx * i][y1 + dy * i]);
            return b.toString();
        }

        @Override public void render(DrawContext c, int mx, int my, float delta) {
            var mc = MinecraftClient.getInstance();
            drawHeader(c, "WORD SEARCH", status);
            int cell = Math.min(28, Math.max(18, (mc.getWindow().getScaledWidth() - 40) / 12));
            int ox = cx() - 6 * cell, oy = 78;
            for (int y = 0; y < 12; y++) for (int x = 0; x < 12; x++) {
                c.fill(ox + x * cell, oy + y * cell, ox + x * cell + cell - 2, oy + y * cell + cell - 2, PANEL);
                c.drawCenteredTextWithShadow(mc.textRenderer, Text.literal(String.valueOf(grid[x][y])), ox + x * cell + cell / 2, oy + y * cell + 5, FG);
            }
            c.drawTextWithShadow(mc.textRenderer, Text.literal(String.join(", ", targets)), ox, oy + 12 * cell + 8, FG);
        }

        @Override public void keyPressed(int key, int scan, int modifiers) { if (key == GLFW.GLFW_KEY_R) begin(); }
    }

    public static final class SpotDifference extends BaseGame {
        private final boolean[][] differences = new boolean[4][4];
        private final boolean[][] found = new boolean[4][4];
        private int foundCount;

        @Override public String id() { return "spot_difference"; }
        @Override public String title() { return "Spot the Difference"; }
        @Override public String category() { return "Puzzle"; }

        @Override public void start() {
            for (int y = 0; y < 4; y++) for (int x = 0; x < 4; x++) { differences[x][y] = false; found[x][y] = false; }
            foundCount = 0;
            for (int i = 0; i < 5; i++) differences[random.nextInt(4)][random.nextInt(4)] = true;
            status = "Find the five changed cells • Found: 0 / 5";
        }

        @Override public boolean mouseClicked(double mx, double my, int button) {
            if (button != 0 || finished) return true;
            int cell = 42, ox = cx() - 170 + 168, oy = 88;
            int x = (int) ((mx - ox) / cell), y = (int) ((my - oy) / cell);
            if (x < 0 || y < 0 || x >= 4 || y >= 4 || found[x][y]) return true;
            found[x][y] = true;
            if (differences[x][y]) { foundCount++; score += 200; }
            else { score = Math.max(0, score - 50); found[x][y] = false; }
            status = "Find the five changed cells • Found: " + foundCount + " / 5";
            if (foundCount == 5) finishWin(score);
            return true;
        }

        @Override public void render(DrawContext c, int mx, int my, float delta) {
            drawHeader(c, "SPOT THE DIFFERENCE", status);
            int cell = 42, left = cx() - 170, oy = 88;
            for (int board = 0; board < 2; board++) for (int y = 0; y < 4; y++) for (int x = 0; x < 4; x++) {
                int px = left + board * 168 + x * cell, py = oy + y * cell;
                int base = ((x + y + board) & 1) == 0 ? 0xFF66727C : 0xFF52606A;
                if (board == 1 && found[x][y] && differences[x][y]) base = 0xFF55CC88;
                c.fill(px + 1, py + 1, px + cell - 2, py + cell - 2, base);
            }
        }

        @Override public void keyPressed(int key, int scan, int modifiers) { if (key == GLFW.GLFW_KEY_R) begin(); }
    }

    public static final class SequenceMemory extends BaseGame {
        private final List<Integer> sequence = new ArrayList<>();
        private int inputIndex;
        private boolean showing;
        private int flash = -1;
        private long showUntil;

        @Override public String id() { return "sequence_memory"; }
        @Override public String title() { return "Sequence Memory"; }
        @Override public String category() { return "Puzzle"; }

        @Override public void start() {
            sequence.clear();
            sequence.add(random.nextInt(16));
            inputIndex = 0;
            showing = true;
            flash = sequence.get(0);
            showUntil = System.nanoTime() + 500_000_000L;
            status = "Watch the sequence.";
        }

        @Override public void tick() {
            super.tick();
            if (showing && System.nanoTime() >= showUntil) { showing = false; flash = -1; status = "Repeat the sequence."; }
        }

        @Override public boolean mouseClicked(double mx, double my, int button) {
            if (button != 0 || finished || showing) return true;
            int cell = 48, ox = cx() - 96, oy = 84;
            int x = (int) ((mx - ox) / cell), y = (int) ((my - oy) / cell);
            if (x < 0 || y < 0 || x >= 4 || y >= 4) return true;
            int value = y * 4 + x;
            if (value != sequence.get(inputIndex)) { status = "Sequence failed."; finish(score); return true; }
            inputIndex++;
            if (inputIndex >= sequence.size()) {
                score += sequence.size() * 100;
                if (sequence.size() >= 12) { finishWin(score); return true; }
                sequence.add(random.nextInt(16));
                inputIndex = 0;
                showing = true;
                flash = sequence.get(sequence.size() - 1);
                showUntil = System.nanoTime() + Math.max(250_000_000L, 550_000_000L - sequence.size() * 20_000_000L);
                status = "Watch the sequence.";
            }
            return true;
        }

        @Override public void render(DrawContext c, int mx, int my, float delta) {
            drawHeader(c, "SEQUENCE MEMORY", "Length: " + sequence.size() + " • " + status);
            int cell = 48, ox = cx() - 96, oy = 84;
            for (int i = 0; i < 16; i++) {
                int x = ox + (i % 4) * cell, y = oy + (i / 4) * cell;
                c.fill(x + 1, y + 1, x + cell - 2, y + cell - 2, i == flash ? 0xFFFFFFFF : OFF);
            }
        }

        @Override public void keyPressed(int key, int scan, int modifiers) { if (key == GLFW.GLFW_KEY_R) begin(); }
    }
}
