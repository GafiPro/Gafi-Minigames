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
        int x=width/2,y=height/2-40;
        c.fill(x-170,y-70,x+170,y+105,0xFF20262D);
        c.drawCenteredTextWithShadow(textRenderer,Text.literal(result),x,y-38,0xFF55CC88);
        c.drawCenteredTextWithShadow(textRenderer,Text.literal(match.host+"  vs  "+match.opponent),x,y-12,0xFFFFFFFF);
        String action=requested?"Rematch requested — waiting for opponent...":"R / Enter  Request rematch     Esc  Games";
        c.drawCenteredTextWithShadow(textRenderer,Text.literal(action),x,y+36,0xFFAAAAAA);
    }

    private void request() { if(!requested){requested=true;MultiplayerManager.requestRematch(match);} }
    @Override public boolean keyPressed(KeyInput input){int key=input.key();if(key==GLFW.GLFW_KEY_ESCAPE){MinecraftClient.getInstance().setScreen(parent);return true;}if(key==GLFW.GLFW_KEY_R||key==GLFW.GLFW_KEY_ENTER){request();return true;}return super.keyPressed(input);}
    @Override public boolean mouseClicked(Click click,boolean doubled){if(click.button()==GLFW.GLFW_MOUSE_BUTTON_1){request();return true;}return super.mouseClicked(click,doubled);}
    @Override public void close(){MinecraftClient.getInstance().setScreen(parent);}
}
