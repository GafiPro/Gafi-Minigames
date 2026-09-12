package com.gafipro.minigames.game.board;

import com.gafipro.minigames.core.WordBank;
import com.gafipro.minigames.game.BaseGame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Word-bank-backed Hangman with exact guess tracking and six-miss defeat. */
public final class HangmanGame extends BaseGame {
    private String word;
    private boolean[] revealed;
    private String guessed;
    private int misses;

    @Override public String id() { return "hangman"; }
    @Override public String title() { return "Hangman"; }
    @Override public String category() { return "Board"; }

    @Override public void start() {
        word = WordBank.random(random);
        revealed = new boolean[word.length()];
        guessed = "";
        misses = 0;
        metrics.accuracyPercent(100);
        status = "Guess letters • 6 misses allowed";
    }

    private boolean complete() { for (boolean value : revealed) if (!value) return false; return true; }

    @Override public void render(DrawContext c, int mx, int my, float delta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        drawHeader(c, "HANGMAN", status + " • Misses: " + misses + "/6");
        StringBuilder shown = new StringBuilder();
        for (int i = 0; i < word.length(); i++) shown.append(revealed[i] ? word.charAt(i) : '_').append(' ');
        c.drawCenteredTextWithShadow(mc.textRenderer, Text.literal(shown.toString()), cx(), 102, 0xFFFFFFFF);
        c.drawCenteredTextWithShadow(mc.textRenderer, Text.literal("Guessed: " + guessed), cx(), 140, 0xFFBBBBBB);
    }

    @Override public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_R) { begin(); return; }
        if (finished || keyCode < GLFW.GLFW_KEY_A || keyCode > GLFW.GLFW_KEY_Z) return;
        char guess = (char) ('A' + keyCode - GLFW.GLFW_KEY_A);
        if (guessed.indexOf(guess) >= 0) return;
        guessed += guess;
        markMove();
        boolean found = false;
        for (int i = 0; i < word.length(); i++) if (word.charAt(i) == guess) { revealed[i] = true; found = true; }
        if (!found) misses++;
        int attempts = guessed.length();
        metrics.accuracyPercent(attempts == 0 ? 100 : (guessed.length() - misses) * 100.0 / attempts);
        if (complete()) finishWin(1000 + Math.max(0, 6 - misses) * 50);
        else if (misses >= 6) finish(0);
        else status = "Guess letters • 6 misses allowed";
    }
}
