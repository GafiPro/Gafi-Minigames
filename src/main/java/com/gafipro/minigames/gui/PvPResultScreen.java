package com.gafipro.minigames.gui;

import com.gafipro.minigames.multiplayer.MultiplayerManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class PvPResultScreen extends Screen {
    private final Screen parent;
    private final MultiplayerManager.Match match;
    private final String result;
    private boolean requested;

    public PvPResultScreen(Screen parent, MultiplayerManager.Match match, String result) {
        super(Text.literal("PvP Result"));
        this.parent = parent;
        this.match = match;
        this.result = result;
    }

    private boolean compact() { return width < 360 || height < 240; }
    private int panelWidth() { return Math.min(340, Math.max(220, width - 20)); }
    private int panelHeight() { return Math.min(170, Math.max(120, height - 30)); }
    private int top() { int h = panelHeight(); return Math.max(10, (height - h) / 2); }
    private int bottom() { return Math.min(height - 10, top() + panelHeight()); }
    private int buttonY() { return bottom() - 34; }
    private int buttonBottom() { return bottom() - 8; }
    private int rematchLeft() { return width / 2 - (compact() ? 125 : 145); }
    private int rematchRight() { return width / 2 - 5; }
    private int gamesLeft() { return width / 2 + 5; }
    private int gamesRight() { return width / 2 + (compact() ? 125 : 145); }

    @Override public void render(DrawContext c, int mx, int my, float d) {
        renderInGameBackground(c);
        int x = width / 2, panelTop = top(), panelBottom = bottom();
        c.fill(x - panelWidth() / 2, panelTop, x + panelWidth() / 2, panelBottom, 0xFF20262D);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal(result), x, panelTop + 28, 0xFF55CC88);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal(match.host + "  vs  " + match.opponent), x, panelTop + 52, 0xFFFFFFFF);
        if (requested) c.drawCenteredTextWithShadow(textRenderer, Text.literal("Waiting for opponent..."), x, panelTop + 76, 0xFFAAAAAA);
        int buttonsBottom = buttonBottom();
        drawButton(c, rematchLeft(), buttonY(), rematchRight(), buttonsBottom, requested ? "WAITING..." : "REMATCH", mx, my, !requested);
        drawButton(c, gamesLeft(), buttonY(), gamesRight(), buttonsBottom, "GAMES", mx, my, false);
    }

    private void drawButton(DrawContext c, int left, int top, int right, int bottom, String label, int mx, int my, boolean active) {
        boolean hover = inside(mx, my, left, top, right - left, bottom - top);
        c.fill(left, top, right, bottom, hover && active ? 0xFF355C7D : 0xFF2A3138);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal(label), (left + right) / 2, top + 7, 0xFFFFFFFF);
    }

    private boolean inside(double mx, double my, int x, int y, int w, int h) { return mx >= x && mx < x + w && my >= y && my < y + h; }
    private void request() { if (!requested) { requested = true; MultiplayerManager.requestRematch(match); } }

    @Override public boolean keyPressed(KeyInput input) {
        int key = input.key();
        if (key == GLFW.GLFW_KEY_ESCAPE) { close(); return true; }
        if (key == GLFW.GLFW_KEY_R || key == GLFW.GLFW_KEY_ENTER) { request(); return true; }
        return super.keyPressed(input);
    }

    @Override public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() != GLFW.GLFW_MOUSE_BUTTON_1) return super.mouseClicked(click, doubled);
        int buttonsBottom = buttonBottom();
        if (inside(click.x(), click.y(), rematchLeft(), buttonY(), rematchRight() - rematchLeft(), buttonsBottom - buttonY())) { request(); return true; }
        if (inside(click.x(), click.y(), gamesLeft(), buttonY(), gamesRight() - gamesLeft(), buttonsBottom - buttonY())) { close(); return true; }
        return true;
    }

    @Override public void close() { MinecraftClient.getInstance().setScreen(parent); }
}
