package com.gafipro.minigames.gui;

import com.gafipro.minigames.game.Game;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class ResultsScreen extends Screen {
    private final Screen parent;
    private final Game game;
    private long ticks;

    public ResultsScreen(Screen parent, Game game) {
        super(Text.literal("Result"));
        this.parent = parent;
        this.game = game;
    }

    @Override public void tick() { if (++ticks > 40) ticks = 40; }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderInGameBackground(context);
        int x = width / 2;
        int y = height / 2 - 60;
        context.fill(x - 125, y - 50, x + 125, y + 65, 0xFF20262D);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("GAME OVER").formatted(Formatting.BOLD, Formatting.AQUA), x, y - 28, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(game.title()).formatted(Formatting.BOLD), x, y - 10, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Score: " + game.score()).formatted(Formatting.GOLD), x, y + 10, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Click to play again  •  Esc for menu").formatted(Formatting.GRAY), x, y + 38, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            try {
                net.minecraft.client.MinecraftClient.getInstance().setScreen(new GameScreen(parent, game.getClass().getDeclaredConstructor().newInstance()));
            } catch (Exception ignored) {
                MinecraftClient.getInstance().setScreen(parent);
            }
            return true;
        }
        return true;
    }

    @Override public void close() { MinecraftClient.getInstance().setScreen(parent); }
}
