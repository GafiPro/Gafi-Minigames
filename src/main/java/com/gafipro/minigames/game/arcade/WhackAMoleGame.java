package com.gafipro.minigames.game.arcade;

import com.gafipro.minigames.game.BaseGame;
import com.gafipro.minigames.game.GameState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

/** Fixed-duration Whack-A-Mole with monotonic deadlines and explicit accuracy tracking. */
public final class WhackAMoleGame extends BaseGame {
    private int target;
    private int hits;
    private int misses;
    private long targetUntilNanos;
    private long endNanos;

    @Override public String id() { return "whack_a_mole"; }
    @Override public String title() { return "Whack-A-Mole"; }
    @Override public String category() { return "Arcade"; }

    static boolean completed(int hits) { return hits > 0; }

    @Override public void start() {
        target = random.nextInt(12);
        hits = 0;
        misses = 0;
        endNanos = System.nanoTime() + 20_000_000_000L;
        nextTarget();
        status = "Hit the mole before it moves.";
    }

    private void nextTarget() {
        target = random.nextInt(12);
        targetUntilNanos = System.nanoTime() + 250_000_000L + random.nextLong(350_000_001L);
    }

    private void miss() {
        misses++;
        updateAccuracy();
    }

    private void updateAccuracy() {
        int attempts = hits + misses;
        metrics.accuracyPercent(attempts == 0 ? 100 : hits * 100.0 / attempts);
    }

    @Override public void tick() {
        super.tick();
        if (finished || state != GameState.PLAYING) return;
        long now = System.nanoTime();
        if (now >= endNanos) {
            score = Math.max(0, hits * 100 - misses * 15);
            if (completed(hits)) finishWin(score);
            else finish(score);
            return;
        }
        if (now >= targetUntilNanos) {
            miss();
            nextTarget();
        }
    }

    @Override public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0 || finished) return true;
        int cell = 55, ox = cx() - 110, oy = 85;
        int x = (int) ((mx - ox) / cell), y = (int) ((my - oy) / cell);
        if (x < 0 || y < 0 || x >= 4 || y >= 3) return true;
        if (y * 4 + x == target) {
            hits++;
            score = Math.max(0, hits * 100 - misses * 15);
            markMove();
            nextTarget();
        } else {
            miss();
            score = Math.max(0, hits * 100 - misses * 15);
        }
        status = "Hit the mole before it moves • Hits: " + hits + " • Misses: " + misses;
        return true;
    }

    @Override public void render(DrawContext c, int mouseX, int mouseY, float delta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        long remaining = Math.max(0, (endNanos - System.nanoTime()) / 1_000_000_000L);
        drawHeader(c, "WHACK-A-MOLE", "Hits: " + hits + " • Misses: " + misses + " • Time: " + remaining + "s");
        int cell = 55, ox = cx() - 110, oy = 85;
        for (int i = 0; i < 12; i++) {
            int x = ox + (i % 4) * cell, y = oy + (i / 4) * cell;
            c.fill(x + 2, y + 2, x + cell - 2, y + cell - 2, 0xFF333B43);
            if (i == target) {
                c.fill(x + 12, y + 12, x + cell - 12, y + cell - 12, 0xFFE4A23B);
                c.drawCenteredTextWithShadow(mc.textRenderer, Text.literal("●").formatted(Formatting.GOLD), x + cell / 2, y + 14, 0xFFFFFFFF);
            }
        }
        c.drawCenteredTextWithShadow(mc.textRenderer, Text.literal("Score: " + Math.max(0, score)), cx(), 270, 0xFFFFFFFF);
    }

    @Override public void keyPressed(int keyCode, int scanCode, int modifiers) { if (keyCode == GLFW.GLFW_KEY_R) begin(); }
}
