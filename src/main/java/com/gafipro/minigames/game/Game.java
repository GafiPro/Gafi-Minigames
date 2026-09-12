package com.gafipro.minigames.game;

import net.minecraft.client.gui.DrawContext;

public interface Game {
    String id();
    String title();
    String category();
    void start();
    void tick();
    void render(DrawContext context, int mouseX, int mouseY, float delta);
    boolean mouseClicked(double mouseX, double mouseY, int button);
    void keyPressed(int keyCode, int scanCode, int modifiers);
    boolean isFinished();
    void close();
    int score();
}
