package com.gafipro.minigames.game;

import com.gafipro.minigames.core.GameStats;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public abstract class BaseGame implements Game {
    protected boolean finished;
    protected int score;
    protected int ticks;
    protected boolean recorded;

    @Override public void start() { }
    @Override public void tick() { ticks++; }
    @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) { }
    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) { return false; }
    @Override public void keyPressed(int keyCode, int scanCode, int modifiers) { }
    @Override public boolean isFinished() { return finished; }
    @Override public void close() {
        if (!finished || recorded) return;
        GameStats.record(id(), score(), score() > 0);
        recorded = true;
    }
    @Override public int score() { return score; }

    protected void drawHeader(DrawContext c, String text, String subtitle) {
        var mc = net.minecraft.client.MinecraftClient.getInstance();
        var tr = mc.textRenderer;
        int cx = mc.getWindow().getScaledWidth() / 2;
        c.drawCenteredTextWithShadow(tr, Text.literal(text).formatted(Formatting.BOLD, Formatting.AQUA), cx, 42, 0xFFFFFFFF);
        c.drawCenteredTextWithShadow(tr, Text.literal(subtitle).formatted(Formatting.GRAY), cx, 56, 0xFFFFFFFF);
    }

    protected boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    protected void finish(int finalScore) {
        this.score = Math.max(0, finalScore);
        this.finished = true;
    }
}
