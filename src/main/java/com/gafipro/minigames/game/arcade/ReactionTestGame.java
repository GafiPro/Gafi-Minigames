package com.gafipro.minigames.game.arcade;

import com.gafipro.minigames.game.BaseGame;
import com.gafipro.minigames.game.GameState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

/** Precise reaction test using monotonic nanosecond timing. */
public final class ReactionTestGame extends BaseGame {
    private enum Phase { WAITING, READY, DONE }
    private Phase phase = Phase.WAITING;
    private long waitUntilNanos;
    private long readyAtNanos;
    private long reactionMs;

    @Override public String id() { return "reaction_test"; }
    @Override public String title() { return "Reaction Test"; }
    @Override public String category() { return "Arcade"; }

    @Override public void start() {
        phase = Phase.WAITING;
        reactionMs = 0;
        long delay = 1_500_000_000L + random.nextLong(3_000_000_001L);
        waitUntilNanos = System.nanoTime() + delay;
        readyAtNanos = 0;
        metrics.accuracyPercent(100);
        status = "Wait for GREEN — do not click early.";
    }

    @Override public void tick() {
        super.tick();
        if (phase == Phase.WAITING && System.nanoTime() >= waitUntilNanos) {
            phase = Phase.READY;
            readyAtNanos = System.nanoTime();
            status = "CLICK NOW!";
        }
    }

    @Override public void render(DrawContext c, int mouseX, int mouseY, float delta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        drawHeader(c, "REACTION TEST", status);
        int x = mc.getWindow().getScaledWidth() / 2;
        int bx = x - 115, by = 88;
        int color = phase == Phase.READY ? 0xFF35C759 : 0xFF6B3030;
        c.fill(bx, by, bx + 230, by + 90, color);
        String msg = phase == Phase.WAITING ? "WAIT" : phase == Phase.READY ? "CLICK!" : reactionMs + " ms";
        c.drawCenteredTextWithShadow(mc.textRenderer, Text.literal(msg).formatted(Formatting.BOLD), x, by + 36, 0xFFFFFFFF);
    }

    @Override public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0 || finished) return true;
        int x = MinecraftClient.getInstance().getWindow().getScaledWidth() / 2;
        if (!inside(mx, my, x - 115, 88, 230, 90)) return true;
        if (phase == Phase.WAITING) {
            status = "False start.";
            phase = Phase.DONE;
            finish(0);
        } else if (phase == Phase.READY) {
            reactionMs = Math.max(0, (System.nanoTime() - readyAtNanos) / 1_000_000L);
            score = Math.max(1, 2_000 - (int) reactionMs * 4);
            metrics.elapsedNanos(Math.max(0, System.nanoTime() - readyAtNanos));
            status = "Reaction: " + reactionMs + " ms";
            phase = Phase.DONE;
            finishWin(score);
        }
        return true;
    }

    @Override public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_R) begin();
    }
}
