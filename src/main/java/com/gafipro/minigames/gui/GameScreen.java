package com.gafipro.minigames.gui;

import com.gafipro.minigames.game.Game;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public final class GameScreen extends Screen {
    private final Screen parent;
    private final Game game;

    public GameScreen(Screen parent, Game game) {
        super(Text.literal(game.title()));
        this.parent = parent;
        this.game = game;
        game.start();
    }

    @Override
    protected void init() { }

    @Override
    public void tick() {
        game.tick();
        if (game.isFinished()) {
            game.close();
            // Returning to the launcher after a finished game keeps the experience simple
            // and avoids accidentally interacting with the real player inventory.
            MinecraftClient.getInstance().setScreen(new ResultsScreen(parent, game));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderInGameBackground(context);
        int left = width / 2 - 182;
        int top = Math.max(30, height / 2 - 105);
        int right = width / 2 + 182;
        int bottom = top + 210;
        context.fill(left, top, right, bottom, 0xFF171B20);
        context.fill(left + 4, top + 4, right - 4, bottom - 4, 0xFF252B32);
        game.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("ESC  Exit"), width / 2, bottom + 10, 0xFFAAAAAA);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return game.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            close();
            return true;
        }
        game.keyPressed(keyCode, scanCode, modifiers);
        return true;
    }

    @Override
    public void close() {
        game.close();
        MinecraftClient.getInstance().setScreen(parent);
    }
}
