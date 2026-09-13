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
    private boolean compact() { return height < 300 || width < 420; }
    private int panelWidth() { return Math.min(380, Math.max(250, width - 12)); }
    private int panelTop() { return compact() ? 8 : Math.max(16, height / 2 - 135); }
    private int panelBottom() { return compact() ? height - 8 : Math.min(height - 16, panelTop() + 270); }
    private int buttonY() { return panelBottom() - (compact() ? 34 : 26); }
    private int replayLeft() { return width / 2 - (compact() ? 130 : 150); }
    private int replayRight() { return width / 2 - (compact() ? 6 : 10); }
    private int gamesLeft() { return width / 2 + (compact() ? 6 : 10); }
    private int gamesRight() { return width / 2 + (compact() ? 130 : 150); }

    @Override public void render(DrawContext c, int mx, int my, float d) {
        renderInGameBackground(c);
        int x = width / 2;
        int top = panelTop(), bottom = panelBottom(), pw = panelWidth();
        c.fill(x - pw / 2, top, x + pw / 2, bottom, 0xFF20262D);

        GameState state = game instanceof BaseGame bg ? bg.state() : null;
        String headline = state == null ? "RESULTS" : switch (state) {
            case WON -> "YOU WIN";
            case DRAW -> "DRAW";
            case LOST -> "GAME OVER";
            default -> "RESULTS";
        };
        int headlineColor = state == GameState.WON ? 0xFF55DD88 : state == GameState.DRAW ? 0xFFFFCC55 : state == GameState.LOST ? 0xFFFF8888 : 0xFFFFFFFF;
        int line = top + 14;
        c.drawCenteredTextWithShadow(textRenderer, Text.literal(headline).formatted(Formatting.BOLD), x, line, headlineColor);
        line += 18;
        c.drawCenteredTextWithShadow(textRenderer, Text.literal(game.title()).formatted(Formatting.BOLD), x, line, 0xFFFFFFFF);
        line += compact() ? 20 : 24;

        if (game instanceof BaseGame bg) {
            var m = bg.metrics();
            c.drawCenteredTextWithShadow(textRenderer, Text.literal("Score: " + m.score()), x, line, 0xFFFFCC55);
            line += 17;
            if (!compact()) {
                if (m.moves() > 0) { c.drawCenteredTextWithShadow(textRenderer, Text.literal("Moves: " + m.moves()), x, line, 0xFFBBBBBB); line += 17; }
                if (m.elapsedMillis() > 0) {
                    String label = game.id().equals("reaction_test") ? "Reaction time: " : "Time: ";
                    c.drawCenteredTextWithShadow(textRenderer, Text.literal(label + formatTime(m.elapsedMillis())), x, line, 0xFFBBBBBB); line += 17;
                }
                if (m.level() > 0) { c.drawCenteredTextWithShadow(textRenderer, Text.literal("Level: " + m.level()), x, line, 0xFFBBBBBB); line += 17; }
                if (m.mistakes() > 0) { c.drawCenteredTextWithShadow(textRenderer, Text.literal("Mistakes: " + m.mistakes()), x, line, 0xFFBBBBBB); line += 17; }
                if (isAccuracyFocused()) { c.drawCenteredTextWithShadow(textRenderer, Text.literal(String.format("Accuracy: %.0f%%", m.accuracyPercent())), x, line, 0xFFBBBBBB); }
            }
        } else {
            c.drawCenteredTextWithShadow(textRenderer, Text.literal("Score: " + game.score()), x, line, 0xFFFFCC55);
        }

        if (!compact()) {
            int lifetimeY = bottom - 82;
            String lifetime = "Games: " + GameStats.games(game.id()) + " • Wins: " + GameStats.wins(game.id()) + " • Losses: " + GameStats.losses(game.id()) + " • Draws: " + GameStats.draws(game.id());
            c.drawCenteredTextWithShadow(textRenderer, Text.literal(lifetime), x, lifetimeY, 0xFFAAAAAA);
            StringBuilder records = new StringBuilder();
            if (isTimeFocused()) records.append("Best time: ").append(formatTime(GameStats.bestTime(game.id())));
            else records.append("Best score: ").append(GameStats.best(game.id()));
            records.append(" • Best streak: ").append(GameStats.bestStreak(game.id()));
            if (GameStats.highestLevel(game.id()) > 0) records.append(" • Best level: ").append(GameStats.highestLevel(game.id()));
            if (isAccuracyFocused()) records.append(String.format(" • Avg accuracy: %.0f%%", GameStats.accuracy(game.id())));
            c.drawCenteredTextWithShadow(textRenderer, Text.literal(records.toString()), x, lifetimeY + 19, 0xFF888888);
        }
        drawButton(c, replayLeft(), buttonY(), replayRight(), panelBottom() - 8, "PLAY AGAIN", mx, my, true);
        drawButton(c, gamesLeft(), buttonY(), gamesRight(), panelBottom() - 8, "GAMES", mx, my, false);
    }

    private void drawButton(DrawContext c, int left, int top, int right, int bottom, String label, int mx, int my, boolean active) {
        boolean hover = inside(mx, my, left, top, right - left, bottom - top);
        c.fill(left, top, right, bottom, hover ? 0xFF355C7D : 0xFF2A3138);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal(label).formatted(active ? Formatting.WHITE : Formatting.GRAY), (left + right) / 2, top + 7, 0xFFFFFFFF);
    }

    private boolean inside(double mx, double my, int x, int y, int w, int h) { return mx >= x && mx < x + w && my >= y && my < y + h; }
    private String formatTime(long millis) { if (millis <= 0) return "—"; long total = millis / 1000; long minutes = total / 60; long seconds = total % 60; return minutes > 0 ? String.format("%02d:%02d", minutes, seconds) : String.format("%.2fs", millis / 1000.0); }
    private void replay() { MinecraftClient.getInstance().setScreen(new GameScreen(parent, GameFactory.create(game.id()))); }

    @Override public boolean keyPressed(KeyInput input) {
        int key = input.key();
        if (key == GLFW.GLFW_KEY_ESCAPE) { close(); return true; }
        if (key == GLFW.GLFW_KEY_R || key == GLFW.GLFW_KEY_ENTER) { replay(); return true; }
        return super.keyPressed(input);
    }

    @Override public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() != GLFW.GLFW_MOUSE_BUTTON_1) return super.mouseClicked(click, doubled);
        if (inside(click.x(), click.y(), replayLeft(), buttonY(), replayRight() - replayLeft(), panelBottom() - 8 - buttonY())) { replay(); return true; }
        if (inside(click.x(), click.y(), gamesLeft(), buttonY(), gamesRight() - gamesLeft(), panelBottom() - 8 - buttonY())) { close(); return true; }
        return true;
    }

    @Override public void close() { MinecraftClient.getInstance().setScreen(parent); }
}
