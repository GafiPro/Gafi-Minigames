package com.gafipro.minigames.game;

import com.gafipro.minigames.core.GameStats;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Random;

public abstract class BaseGame implements Game {
    protected final Random random = new Random();
    protected boolean finished;
    protected int score;
    protected int ticks;
    protected boolean recorded;
    protected boolean won;
    protected String status = "";

    @Override public void start() { }
    @Override public void tick() { if (!finished) ticks++; }
    @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) { }
    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) { return false; }
    @Override public void keyPressed(int keyCode, int scanCode, int modifiers) { }
    @Override public boolean isFinished() { return finished; }

    @Override public void close() {
        if (!finished || recorded) return;
        GameStats.record(id(), score(), won);
        recorded = true;
    }

    @Override public int score() { return score; }
    public String status() { return status; }

    protected void finish(int finalScore) {
        score = Math.max(0, finalScore);
        finished = true;
    }

    protected void finishWin(int finalScore) {
        won = true;
        finish(finalScore);
    }

    protected void drawHeader(DrawContext c, String title, String subtitle) {
        MinecraftClient mc = MinecraftClient.getInstance();
        int cx = mc.getWindow().getScaledWidth() / 2;
        c.drawCenteredTextWithShadow(mc.textRenderer, Text.literal(title).formatted(Formatting.BOLD, Formatting.AQUA), cx, 34, 0xFFFFFFFF);
        c.drawCenteredTextWithShadow(mc.textRenderer, Text.literal(subtitle).formatted(Formatting.GRAY), cx, 48, 0xFFFFFFFF);
        if (!status.isEmpty()) c.drawCenteredTextWithShadow(mc.textRenderer, Text.literal(status).formatted(Formatting.WHITE), cx, 62, 0xFFFFFFFF);
    }

    protected boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    protected int cx() { return MinecraftClient.getInstance().getWindow().getScaledWidth() / 2; }
    protected int cy() { return MinecraftClient.getInstance().getWindow().getScaledHeight() / 2; }
    protected int rgb(int r,int g,int b){return 0xFF000000 | (r<<16) | (g<<8) | b;}
}
