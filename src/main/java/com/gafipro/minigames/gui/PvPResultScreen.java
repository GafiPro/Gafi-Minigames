package com.gafipro.minigames.gui;

import com.gafipro.minigames.core.GameStats;
import com.gafipro.minigames.multiplayer.MultiplayerManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class PvPResultScreen extends Screen {
    private final Screen parent;private final MultiplayerManager.Match match;private final String result;
    public PvPResultScreen(Screen parent,MultiplayerManager.Match match,String result){super(Text.literal("PvP Result"));this.parent=parent;this.match=match;this.result=result;}
    @Override public void render(DrawContext c,int mx,int my,float d){renderInGameBackground(c);int x=width/2,y=height/2-40;c.fill(x-160,y-65,x+160,y+90,0xFF20262D);c.drawCenteredTextWithShadow(textRenderer,Text.literal(result),x,y-35,0xFF55CC88);c.drawCenteredTextWithShadow(textRenderer,Text.literal(match.host+"  vs  "+match.opponent),x,y-10,0xFFFFFFFF);c.drawCenteredTextWithShadow(textRenderer,Text.literal("R / Enter  Rematch     Esc  Games"),x,y+38,0xFFAAAAAA);}
    @Override public boolean keyPressed(int key,int scan,int mods){if(key==GLFW.GLFW_KEY_ESCAPE){MinecraftClient.getInstance().setScreen(parent);return true;}if(key==GLFW.GLFW_KEY_R||key==GLFW.GLFW_KEY_ENTER){match.finished=false;match.result="";match.remoteMove="";match.rematchLocal=false;match.rematchRemote=false;MinecraftClient.getInstance().setScreen(new MultiplayerGameScreen(parent,match));MultiplayerManager.requestRematch(match);return true;}return true;}
    @Override public void close(){MinecraftClient.getInstance().setScreen(parent);}
}
