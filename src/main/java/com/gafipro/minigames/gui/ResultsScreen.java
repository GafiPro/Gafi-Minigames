package com.gafipro.minigames.gui;

import com.gafipro.minigames.core.GameFactory;
import com.gafipro.minigames.core.GameStats;
import com.gafipro.minigames.game.Game;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public final class ResultsScreen extends Screen {
    private final Screen parent;private final Game game;
    public ResultsScreen(Screen parent,Game game){super(Text.literal("Results"));this.parent=parent;this.game=game;}
    @Override public void render(DrawContext c,int mx,int my,float d){renderInGameBackground(c);int x=width/2,y=height/2-55;c.fill(x-150,y-65,x+150,y+105,0xFF20262D);String headline=game.score()>0?"NICE RUN!":"GAME OVER";c.drawCenteredTextWithShadow(textRenderer,Text.literal(headline).formatted(Formatting.BOLD,Formatting.AQUA),x,y-40,0xFFFFFFFF);c.drawCenteredTextWithShadow(textRenderer,Text.literal(game.title()).formatted(Formatting.BOLD),x,y-20,0xFFFFFFFF);c.drawCenteredTextWithShadow(textRenderer,Text.literal("Score: "+game.score()).formatted(Formatting.GOLD),x,y+3,0xFFFFFFFF);c.drawCenteredTextWithShadow(textRenderer,Text.literal("Games: "+GameStats.games(game.id())+"  •  Wins: "+GameStats.wins(game.id())),x,y+23,0xFFAAAAAA);c.drawCenteredTextWithShadow(textRenderer,Text.literal("R  Play again    Enter  Play again    Esc  Games"),x,y+63,0xFFFFFFFF);}
    @Override public boolean keyPressed(int key,int scan,int mods){if(key==GLFW.GLFW_KEY_ESCAPE){close();return true;}if(key==GLFW.GLFW_KEY_R||key==GLFW.GLFW_KEY_ENTER){MinecraftClient.getInstance().setScreen(new GameScreen(parent,GameFactory.create(game.id())));return true;}return super.keyPressed(key,scan,mods);}
    @Override public boolean mouseClicked(double mx,double my,int b){if(b==0){MinecraftClient.getInstance().setScreen(new GameScreen(parent,GameFactory.create(game.id())));return true;}return true;}
    @Override public void close(){MinecraftClient.getInstance().setScreen(parent);}
}
