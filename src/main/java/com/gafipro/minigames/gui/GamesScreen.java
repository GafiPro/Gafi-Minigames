package com.gafipro.minigames.gui;

import com.gafipro.minigames.core.GameCatalog;
import com.gafipro.minigames.core.GameFactory;
import com.gafipro.minigames.core.GameStats;
import com.gafipro.minigames.core.MinigamesSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;
import java.util.*;
import java.util.stream.Collectors;

public final class GamesScreen extends Screen {
    private final Screen parent;
    private final List<GameCatalog.Entry> all = GameCatalog.all();
    private String search = "";
    private GameCatalog.Category category = null;
    private int page;
    private boolean favoritesOnly;
    private boolean searchFocused;

    public GamesScreen(Screen parent) {
        super(Text.literal("Gafi Minigames"));
        this.parent = parent;
        MinigamesSettings.load();
        GameStats.load();
    }

    @Override protected void init() { page = 0; }

    private List<GameCatalog.Entry> filtered() {
        String q = search.toLowerCase(Locale.ROOT);
        return all.stream().filter(e ->
                (category == null || e.category() == category)
                        && (q.isBlank()
                        || e.title().toLowerCase(Locale.ROOT).contains(q)
                        || e.description().toLowerCase(Locale.ROOT).contains(q)
                        || e.category().name().toLowerCase(Locale.ROOT).contains(q))
                        && (!favoritesOnly || MinigamesSettings.favorite(e.id())))
                .collect(Collectors.toList());
    }

    private int tabWidth() { return width < 380 ? 48 : width < 500 ? 64 : 78; }
    private int tabGap() { return width < 380 ? 3 : width < 500 ? 4 : 7; }
    private int tabsLeft() { return Math.max(4, (width - 6 * tabWidth() - 5 * tabGap()) / 2); }
    private int columns() { return width >= 470 ? 2 : 1; }
    private int cardWidth() { return columns() == 2 ? Math.min(210, (width - 34 - 8) / 2) : Math.max(180, width - 30); }
    private int cardHeight() { return height < 300 ? 54 : 66; }
    private int rows() { return height < 300 ? 2 : 3; }
    private int perPage() { return columns() * rows(); }
    private int cardGap() { return 8; }
    private int cardsLeft() { return width / 2 - (columns() * cardWidth() + (columns() - 1) * cardGap()) / 2; }
    private int cardsTop() { return height < 300 ? 62 : 66; }

    @Override public void render(DrawContext c, int mx, int my, float delta) {
        renderInGameBackground(c);
        int cx = width / 2;
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("GAFI MINIGAMES").formatted(Formatting.BOLD, Formatting.AQUA), cx, 8, 0xFFFFFFFF);
        String searchText = searchFocused ? "Search: " + search + "_" : (search.isBlank() ? "Your Minecraft arcade" : "Search: " + search);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal(searchText).formatted(searchFocused ? Formatting.WHITE : Formatting.GRAY), cx, 22, 0xFFFFFFFF);

        String[] tabs = {"ALL", "ARCADE", "PUZZLE", "BOARD", "ENDLESS", "★ FAV"};
        int tw = tabWidth(), tg = tabGap(), tx = tabsLeft();
        for (int i = 0; i < tabs.length; i++) {
            int x = tx + i * (tw + tg);
            boolean on = (i == 0 && category == null && !favoritesOnly)
                    || (i == 1 && category == GameCatalog.Category.ARCADE)
                    || (i == 2 && category == GameCatalog.Category.PUZZLE)
                    || (i == 3 && category == GameCatalog.Category.BOARD)
                    || (i == 4 && category == GameCatalog.Category.ENDLESS)
                    || (i == 5 && favoritesOnly);
            c.fill(x, 33, x + tw, 51, on ? 0xFF355C7D : 0xFF252B31);
            c.drawCenteredTextWithShadow(textRenderer, Text.literal(tabs[i]), x + tw / 2, 38, 0xFFFFFFFF);
        }

        List<GameCatalog.Entry> list = filtered();
        int per = perPage(), start = page * per;
        int pages = Math.max(1, (list.size() + per - 1) / per);
        if (page >= pages) page = pages - 1;

        int cw = cardWidth(), ch = cardHeight(), g = cardGap(), cols = columns(), left = cardsLeft();
        for (int j = 0; j < per && start + j < list.size(); j++) {
            GameCatalog.Entry e = list.get(start + j);
            int row = j / cols, col = j % cols;
            int x = left + col * (cw + g), y = cardsTop() + row * (ch + g);
            boolean hov = inside(mx, my, x, y, cw, ch);
            c.fill(x, y, x + cw, y + ch, hov ? 0xFF323A43 : 0xFF20262D);
            c.fill(x, y, x + 4, y + ch, colorFor(e.category()));
            c.drawTextWithShadow(textRenderer, Text.literal((MinigamesSettings.favorite(e.id()) ? "★ " : "") + e.title()).formatted(Formatting.BOLD), x + 10, y + 7, 0xFFFFFFFF);
            c.drawTextWithShadow(textRenderer, Text.literal(e.category().name()).formatted(Formatting.GRAY), x + 10, y + 21, 0xFFFFFFFF);
            String desc = e.description().length() > 42 ? e.description().substring(0, 41) + "…" : e.description();
            c.drawTextWithShadow(textRenderer, Text.literal(desc).formatted(Formatting.DARK_GRAY), x + 10, y + 34, 0xFFFFFFFF);
            String modes = e.pvp() ? "PvP" : e.ai() ? "AI" : "Solo";
            c.drawTextWithShadow(textRenderer, Text.literal(modes + " • Best " + GameStats.best(e.id())), x + 10, y + ch - 12, 0xFFFFD166);
        }

        int foot = height - 24;
        c.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Page " + (page + 1) + " / " + pages + " • Enter: search • F: favorite"),
                cx, foot, 0xFFAAAAAA);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("←/→ page • Esc: back"), cx, foot + 12, 0xFF888888);
    }

    private boolean inside(double a, double b, int x, int y, int w, int h) {
        return a >= x && a < x + w && b >= y && b < y + h;
    }

    private int colorFor(GameCatalog.Category c) {
        return switch (c) {
            case ARCADE -> 0xFF55CC88;
            case PUZZLE -> 0xFFAA77DD;
            case BOARD -> 0xFF5599DD;
            case ENDLESS -> 0xFFE67E22;
        };
    }

    @Override public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x(), my = click.y();
        if (inside(mx, my, width / 2 - 110, 14, 220, 14)) {
            searchFocused = true;
            return true;
        }
        if (click.button() != GLFW.GLFW_MOUSE_BUTTON_1) return true;

        String[] tabs = {"ALL", "ARCADE", "PUZZLE", "BOARD", "ENDLESS", "★ FAV"};
        int tw = tabWidth(), tg = tabGap(), tx = tabsLeft();
        for (int i = 0; i < tabs.length; i++) {
            int x = tx + i * (tw + tg);
            if (inside(mx, my, x, 33, tw, 18)) {
                category = i == 0 || i == 5 ? null : GameCatalog.Category.values()[i - 1];
                favoritesOnly = i == 5;
                page = 0;
                searchFocused = false;
                return true;
            }
        }

        List<GameCatalog.Entry> list = filtered();
        int per = perPage(), start = page * per, cw = cardWidth(), ch = cardHeight(), g = cardGap(), cols = columns(), left = cardsLeft();
        for (int j = 0; j < per && start + j < list.size(); j++) {
            int col = j % cols, row = j / cols, x = left + col * (cw + g), y = cardsTop() + row * (ch + g);
            if (inside(mx, my, x, y, cw, ch)) {
                GameCatalog.Entry e = list.get(start + j);
                MinigamesSettings.touchRecent(e.id());
                MinecraftClient.getInstance().setScreen(new GameScreen(this, GameFactory.create(e.id())));
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override public boolean charTyped(char chr, int modifiers) {
        if (!searchFocused) return super.charTyped(chr, modifiers);
        if (!Character.isISOControl(chr) && search.length() < 32) {
            search += chr;
            page = 0;
        }
        return true;
    }

    @Override public boolean keyPressed(KeyInput input) {
        int key = input.key();
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        if (key == GLFW.GLFW_KEY_ENTER) {
            searchFocused = !searchFocused;
            return true;
        }
        if (searchFocused) {
            if (key == GLFW.GLFW_KEY_BACKSPACE) {
                if (!search.isEmpty()) search = search.substring(0, search.length() - 1);
                page = 0;
                return true;
            }
            if (key == GLFW.GLFW_KEY_LEFT || key == GLFW.GLFW_KEY_RIGHT) return true;
            return super.keyPressed(input);
        }
        if (key == GLFW.GLFW_KEY_LEFT) { page = Math.max(0, page - 1); return true; }
        if (key == GLFW.GLFW_KEY_RIGHT) { page++; return true; }
        if (key == GLFW.GLFW_KEY_F) {
            List<GameCatalog.Entry> l = filtered();
            if (!l.isEmpty()) MinigamesSettings.toggleFavorite(l.get(Math.min(page * perPage(), l.size() - 1)).id());
            return true;
        }
        return super.keyPressed(input);
    }

    @Override public void close() { MinecraftClient.getInstance().setScreen(parent); }
}
