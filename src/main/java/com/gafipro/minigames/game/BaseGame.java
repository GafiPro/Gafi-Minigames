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
        if (!recorded) {
            GameStats.record(id(), score(), finished && score() > 0);
            recorded = true;
        }
    }
    @Override public int score() { return score; }

    protected void title(DrawContext c, String text) {
        c.drawCenteredTextWithShadow(textRenderer(c), Text.literal(text).formatted(Formatting.BOLD, Formatting.AQUA), 0, 0, 0xFFFFFFFF);
    }
    protected net.minecraft.client.font.TextRenderer textRenderer(DrawContext c) {
        return net.minecraft.client.MinecraftClient.getInstance().textRenderer;
    }
    protected boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }
    protected void finish(int score, boolean win) {
        this.score = score;
        this.finished = true;
    }
}
