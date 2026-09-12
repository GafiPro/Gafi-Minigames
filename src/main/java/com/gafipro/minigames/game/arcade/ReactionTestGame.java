package com.gafipro.minigames.game.arcade;

import com.gafipro.minigames.game.BaseGame;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.concurrent.ThreadLocalRandom;

public final class ReactionTestGame extends BaseGame {
    private enum State { WAITING, READY, DONE }
    private State state = State.WAITING;
    private long readyAt;
    private long reactionMs;

    @Override public String id() { return "reaction_test"; }
    @Override public String title() { return "Reaction Test"; }
    @Override public String category() { return "Arcade"; }

    @Override public void start() {
        state = State.WAITING;
        readyAt = System.currentTimeMillis() + ThreadLocalRandom.current().nextLong(1500, 4500);
    }

    @Override public void tick() {
        if (state == State.WAITING && System.currentTimeMillis() >= readyAt) {
            state = State.READY;
            readyAt = System.nanoTime();
        }
    }

    @Override public void render(DrawContext c, int mouseX, int mouseY, float delta) {
        var tr = net.minecraft.client.MinecraftClient.getInstance().textRenderer;
        int x = net.minecraft.client.MinecraftClient.getInstance().getWindow().getScaledWidth() / 2;
        drawHeader(c, "REACTION TEST", "Click the panel when it becomes GREEN");
        int bx = x - 115, by = 88, bw = 230, bh = 90;
        int color = state == State.READY ? 0xFF35C759 : 0xFF6B3030;
        c.fill(bx, by, bx + bw, by + bh, color);
        String msg = state == State.WAITING ? "WAIT..." : state == State.READY ? "CLICK!" : reactionMs + " ms";
        c.drawCenteredTextWithShadow(tr, Text.literal(msg).formatted(Formatting.BOLD), x, by + 36, 0xFFFFFFFF);
    }

    @Override public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0) return true;
        int x = net.minecraft.client.MinecraftClient.getInstance().getWindow().getScaledWidth() / 2;
        int bx = x - 115, by = 88;
        if (!inside(mx, my, bx, by, 230, 90)) return true;
        if (state == State.WAITING) {
            finish(1);
        } else if (state == State.READY) {
            reactionMs = (System.nanoTime() - readyAt) / 1_000_000L;
            score = (int)Math.max(1, 1000 - reactionMs);
            state = State.DONE;
            finished = true;
        }
        return true;
    }
}
