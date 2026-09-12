package com.gafipro.minigames.gui;

import com.gafipro.minigames.core.GameStats;
import com.gafipro.minigames.game.Game;
import com.gafipro.minigames.game.arcade.ReactionTestGame;
import com.gafipro.minigames.game.arcade.WhackAMoleGame;
import com.gafipro.minigames.game.board.ConnectFourGame;
import com.gafipro.minigames.game.board.RockPaperScissorsGame;
import com.gafipro.minigames.game.board.TicTacToeGame;
import com.gafipro.minigames.game.puzzle.Game2048;
import com.gafipro.minigames.game.puzzle.MinesweeperGame;
import com.gafipro.minigames.game.puzzle.MemoryMatchGame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public final class GamesScreen extends Screen {
    private final Screen parent;
    private final List<Card> cards = new ArrayList<>();
    private int page;

    private record Card(String title, String category, String description, int color, java.util.function.Supplier<Game> factory) {}

    public GamesScreen(Screen parent) {
        super(Text.literal("Gafi Minigames"));
        this.parent = parent;
        GameStats.load();
        cards.add(new Card("Reaction Test", "Arcade", "Wait for the signal, then react as fast as possible.", 0xFF55FFFF, ReactionTestGame::new));
        cards.add(new Card("Whack-A-Mole", "Arcade", "Hit the target before it moves. Build your combo.", 0xFFFFAA55, WhackAMoleGame::new));
        cards.add(new Card("Memory Match", "Puzzle", "Reveal cards and match every pair.", 0xFFFF55FF, MemoryMatchGame::new));
        cards.add(new Card("Minesweeper", "Puzzle", "Clear the board without hitting a mine.", 0xFFFF5555, MinesweeperGame::new));
        cards.add(new Card("2048", "Puzzle", "Merge matching tiles and chase a new high score.", 0xFFFFFF55, Game2048::new));
        cards.add(new Card("Tic-Tac-Toe", "Board", "Classic 3x3 strategy against a friend or the AI.", 0xFF55FF55, TicTacToeGame::new));
        cards.add(new Card("Connect Four", "Board", "Drop pieces and connect four in a row.", 0xFF5555FF, ConnectFourGame::new));
        cards.add(new Card("Rock Paper Scissors", "Quick", "Pick your move and beat the opponent.", 0xFFFFFFFF, RockPaperScissorsGame::new));
    }

    @Override
    protected void init() {
        page = 0;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderInGameBackground(context);
        int x = width / 2;
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("GAFI MINIGAMES").formatted(Formatting.BOLD, Formatting.AQUA), x, 18, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Choose a game").formatted(Formatting.GRAY), x, 32, 0xFFFFFFFF);

        int cols = 2;
        int rows = 4;
        int cardW = 170;
        int cardH = 65;
        int gap = 8;
        int startX = x - (cols * cardW + gap) / 2;
        int startY = 48;
        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            int col = i % cols;
            int row = i / cols;
            int cx = startX + col * (cardW + gap);
            int cy = startY + row * (cardH + gap);
            boolean hover = mouseX >= cx && mouseX < cx + cardW && mouseY >= cy && mouseY < cy + cardH;
            context.fill(cx, cy, cx + cardW, cy + cardH, hover ? 0xFF303840 : 0xFF20262D);
            context.fill(cx, cy, cx + 4, cy + cardH, card.color);
            context.drawTextWithShadow(textRenderer, Text.literal(card.title).formatted(Formatting.BOLD), cx + 11, cy + 8, 0xFFFFFFFF);
            context.drawTextWithShadow(textRenderer, Text.literal(card.category).formatted(Formatting.GRAY), cx + 11, cy + 21, 0xFFFFFFFF);
            String desc = card.description.length() > 41 ? card.description.substring(0, 40) + "…" : card.description;
            context.drawTextWithShadow(textRenderer, Text.literal(desc).formatted(Formatting.DARK_GRAY), cx + 11, cy + 35, 0xFFFFFFFF);
            int best = GameStats.best(idFor(card));
            if (best > 0) context.drawTextWithShadow(textRenderer, Text.literal("Best: " + best), cx + 11, cy + 50, 0xFFFFD966);
        }

        int footerY = startY + rows * (cardH + gap) + 3;
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Local progress is saved automatically").formatted(Formatting.DARK_GRAY), x, footerY + 13, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Esc  Back").formatted(Formatting.GRAY), x, footerY + 27, 0xFFFFFFFF);
    }

    private String idFor(Card c) { return c.title.toLowerCase().replace(" ", "_").replace("-", "_"); }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return true;
        int x = width / 2;
        int cardW = 170, cardH = 65, gap = 8, startX = x - (2 * cardW + gap) / 2, startY = 48;
        for (int i = 0; i < cards.size(); i++) {
            int cx = startX + (i % 2) * (cardW + gap);
            int cy = startY + (i / 2) * (cardH + gap);
            if (mouseX >= cx && mouseX < cx + cardW && mouseY >= cy && mouseY < cy + cardH) {
                Game game = cards.get(i).factory.get();
                MinecraftClient.getInstance().setScreen(new GameScreen(this, game));
                return true;
            }
        }
        return true;
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }
}
