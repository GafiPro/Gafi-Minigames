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

    @Override public void render(DrawContext c, int mx, int my, float d) {
        renderInGameBackground(c);
        int x = width / 2;
        int panelWidth = Math.min(340, Math.max(220, width - 20));
        int panelHeight = Math.min(170, Math.max(120, height - 30));
        int top = Math.max(10, (height - panelHeight) / 2);
        int bottom = Math.min(height - 10, top + panelHeight);
        c.fill(x - panelWidth / 2, top, x + panelWidth / 2, bottom, 0xFF20262D);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal(result), x, top + 28, 0xFF55CC88);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal(match.host + "  vs  " + match.opponent), x, top + 52, 0xFFFFFFFF);
        String action = requested
                ? (width < 360 ? "Waiting for opponent..." : "Rematch requested — waiting for opponent...")
                : (width < 360 ? "R/Enter = rematch • Esc = games" : "R / Enter  Request rematch     Esc  Games");
        c.drawCenteredTextWithShadow(textRenderer, Text.literal(action), x, bottom - 28, 0xFFAAAAAA);
    }

    private void request() { if(!requested){requested=true;MultiplayerManager.requestRematch(match);} }
    @Override public boolean keyPressed(KeyInput input){int key=input.key();if(key==GLFW.GLFW_KEY_ESCAPE){MinecraftClient.getInstance().setScreen(parent);return true;}if(key==GLFW.GLFW_KEY_R||key==GLFW.GLFW_KEY_ENTER){request();return true;}return super.keyPressed(input);}
    @Override public boolean mouseClicked(Click click,boolean doubled){if(click.button()==GLFW.GLFW_MOUSE_BUTTON_1){request();return true;}return super.mouseClicked(click,doubled);}
    @Override public void close(){MinecraftClient.getInstance().setScreen(parent);}
}
