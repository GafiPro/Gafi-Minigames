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

    private boolean isTimeFocused() { return game.id().equals("reaction_test") || game.id().equals("typing_speed"); }
    private boolean isAccuracyFocused() { return game.id().equals("color_rush") || game.id().equals("typing_speed") || game.id().equals("hangman") || game.id().equals("whack_a_mole"); }

    @Override public void render(DrawContext c, int mx, int my, float d) {
        renderInGameBackground(c);
        int compact = height < 300 ? 1 : 0;
        int panelWidth = Math.min(380, Math.max(250, width - 12));
        int panelTop = compact == 1 ? 8 : Math.max(16, height / 2 - 135);
        int panelBottom = compact == 1 ? height - 8 : Math.min(height - 16, panelTop + 270);
        int x = width / 2;
        c.fill(x - panelWidth / 2, panelTop, x + panelWidth / 2, panelBottom, 0xFF20262D);

        GameState state = game instanceof BaseGame bg ? bg.state() : null;
        String headline = state == null ? "RESULTS" : switch (state) {
            case WON -> "YOU WIN";
            case DRAW -> "DRAW";
            case LOST -> "GAME OVER";
            default -> "RESULTS";
        };
        int headlineColor = state == GameState.WON ? 0xFF55DD88 : state == GameState.DRAW ? 0xFFFFCC55 : state == GameState.LOST ? 0xFFFF8888 : 0xFFFFFFFF;
        int line = panelTop + 14;
        c.drawCenteredTextWithShadow(textRenderer, Text.literal(headline).formatted(Formatting.BOLD), x, line, headlineColor);
        line += 18;
        c.drawCenteredTextWithShadow(textRenderer, Text.literal(game.title()).formatted(Formatting.BOLD), x, line, 0xFFFFFFFF);
        line += compact == 1 ? 20 : 24;

        if (game instanceof BaseGame bg) {
            var m = bg.metrics();
            c.drawCenteredTextWithShadow(textRenderer, Text.literal("Score: " + m.score()), x, line, 0xFFFFCC55);
            line += 17;
            if (compact == 0) {
                if (m.moves() > 0) { c.drawCenteredTextWithShadow(textRenderer, Text.literal("Moves: " + m.moves()), x, line, 0xFFBBBBBB); line += 17; }
                if (m.elapsedMillis() > 0) {
                    String label = game.id().equals("reaction_test") ? "Reaction time: " : "Time: ";
                    c.drawCenteredTextWithShadow(textRenderer, Text.literal(label + formatTime(m.elapsedMillis())), x, line, 0xFFBBBBBB); line += 17;
                }
                if (m.level() > 0) { c.drawCenteredTextWithShadow(textRenderer, Text.literal("Level: " + m.level()), x, line, 0xFFBBBBBB); line += 17; }
                if (m.mistakes() > 0) { c.drawCenteredTextWithShadow(textRenderer, Text.literal("Mistakes: " + m.mistakes()), x, line, 0xFFBBBBBB); line += 17; }
                if (isAccuracyFocused()) { c.drawCenteredTextWithShadow(textRenderer, Text.literal(String.format("Accuracy: %.0f%%", m.accuracyPercent())), x, line, 0xFFBBBBBB); line += 17; }
            }
        } else {
            c.drawCenteredTextWithShadow(textRenderer, Text.literal("Score: " + game.score()), x, line, 0xFFFFCC55);
        }

        if (compact == 0) {
            int lifetimeY = panelBottom - 82;
            String lifetime = "Games: " + GameStats.games(game.id())
                    + " • Wins: " + GameStats.wins(game.id())
                    + " • Losses: " + GameStats.losses(game.id())
                    + " • Draws: " + GameStats.draws(game.id());
            c.drawCenteredTextWithShadow(textRenderer, Text.literal(lifetime), x, lifetimeY, 0xFFAAAAAA);

            StringBuilder records = new StringBuilder();
            if (isTimeFocused()) records.append("Best time: ").append(formatTime(GameStats.bestTime(game.id())));
            else records.append("Best score: ").append(GameStats.best(game.id()));
            records.append(" • Best streak: ").append(GameStats.bestStreak(game.id()));
            if (GameStats.highestLevel(game.id()) > 0) records.append(" • Best level: ").append(GameStats.highestLevel(game.id()));
            if (isAccuracyFocused()) records.append(String.format(" • Avg accuracy: %.0f%%", GameStats.accuracy(game.id())));
            c.drawCenteredTextWithShadow(textRenderer, Text.literal(records.toString()), x, lifetimeY + 19, 0xFF888888);
            c.drawCenteredTextWithShadow(textRenderer, Text.literal("R / Enter  Play again     Esc  Games"), x, panelBottom - 20, 0xFFFFFFFF);
        } else {
            c.drawCenteredTextWithShadow(textRenderer, Text.literal("R / Enter  Play again     Esc  Games"), x, panelBottom - 18, 0xFFFFFFFF);
        }
    }

    private String formatTime(long millis) {
        if (millis <= 0) return "—";
        long total = millis / 1000;
        long minutes = total / 60;
        long seconds = total % 60;
        return minutes > 0 ? String.format("%02d:%02d", minutes, seconds) : String.format("%.2fs", millis / 1000.0);
    }

    private void replay() { MinecraftClient.getInstance().setScreen(new GameScreen(parent, GameFactory.create(game.id()))); }

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
