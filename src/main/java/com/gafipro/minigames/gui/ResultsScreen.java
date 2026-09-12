package com.gafipro.minigames.gui;

import com.gafipro.minigames.core.GameFactory;
import com.gafipro.minigames.core.GameStats;
import com.gafipro.minigames.game.BaseGame;
import com.gafipro.minigames.game.Game;
import com.gafipro.minigames.game.GameState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public final class ResultsScreen extends Screen {
    private final Screen parent;
    private final Game game;

    public ResultsScreen(Screen parent, Game game) { super(Text.literal("Results")); this.parent = parent; this.game = game; }

    @Override public void render(DrawContext c, int mx, int my, float d) {
        renderInGameBackground(c);
        int x = width / 2, y = height / 2 - 80;
        c.fill(x - 175, y - 90, x + 175, y + 135, 0xFF20262D);
        GameState state = game instanceof BaseGame bg ? bg.state() : null;
        String headline = switch (state) {
            case WON -> "YOU WIN";
            case DRAW -> "DRAW";
            case LOST -> "GAME OVER";
            default -> game.score() > 0 ? "NICE RUN!" : "RESULTS";
        };
        int headlineColor = state == GameState.WON ? 0xFF55DD88 : state == GameState.DRAW ? 0xFFFFCC55 : 0xFFFF8888;
        c.drawCenteredTextWithShadow(textRenderer, Text.literal(headline).formatted(Formatting.BOLD), x, y - 62, headlineColor);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal(game.title()).formatted(Formatting.BOLD), x, y - 42, 0xFFFFFFFF);
        if (game instanceof BaseGame bg) {
            var m = bg.metrics();
            int line = y - 15;
            if (m.score() > 0) { c.drawCenteredTextWithShadow(textRenderer, Text.literal("Score: " + m.score()), x, line, 0xFFFFCC55); line += 20; }
            if (m.moves() > 0) { c.drawCenteredTextWithShadow(textRenderer, Text.literal("Moves: " + m.moves()), x, line, 0xFFBBBBBB); line += 18; }
            if (m.elapsedMillis() > 0) { c.drawCenteredTextWithShadow(textRenderer, Text.literal("Time: " + formatTime(m.elapsedMillis())), x, line, 0xFFBBBBBB); line += 18; }
            if (m.level() > 0) { c.drawCenteredTextWithShadow(textRenderer, Text.literal("Level: " + m.level()), x, line, 0xFFBBBBBB); line += 18; }
            if (m.mistakes() > 0) { c.drawCenteredTextWithShadow(textRenderer, Text.literal("Mistakes: " + m.mistakes()), x, line, 0xFFBBBBBB); line += 18; }
            if (m.accuracyPercent() < 100) { c.drawCenteredTextWithShadow(textRenderer, Text.literal("Accuracy: " + String.format("%.0f%%", (double) m.accuracyPercent())), x, line, 0xFFBBBBBB); }
        } else {
            c.drawCenteredTextWithShadow(textRenderer, Text.literal("Score: " + game.score()), x, y - 15, 0xFFFFCC55);
        }
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("Games: " + GameStats.games(game.id()) + " • Wins: " + GameStats.wins(game.id()) + " • Best: " + GameStats.best(game.id())), x, y + 76, 0xFFAAAAAA);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("R / Enter  Play again     Esc  Games"), x, y + 105, 0xFFFFFFFF);
    }

    private String formatTime(long millis) { long total = millis / 1000; return String.format("%02d:%02d", total / 60, total % 60); }

    private void replay() {
        MinecraftClient.getInstance().setScreen(new GameScreen(parent, GameFactory.create(game.id())));
    }

    @Override public boolean keyPressed(KeyInput input) {
        int key = input.key();
        if (key == GLFW.GLFW_KEY_ESCAPE) { close(); return true; }
        if (key == GLFW.GLFW_KEY_R || key == GLFW.GLFW_KEY_ENTER) { replay(); return true; }
        return super.keyPressed(input);
    }

    @Override public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() == GLFW.GLFW_MOUSE_BUTTON_1) { replay(); return true; }
        return super.mouseClicked(click, doubled);
    }

    @Override public void close() { MinecraftClient.getInstance().setScreen(parent); }
}
