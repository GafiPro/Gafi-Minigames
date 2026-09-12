package com.gafipro.minigames.gui;

import com.gafipro.minigames.core.MinigamesSettings;
import com.gafipro.minigames.game.Game;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class GameScreen extends Screen {
    private final Screen parent;
    private Game game;
    private boolean finishedScreen;
    public GameScreen(Screen parent,Game game){super(Text.literal(game.title()));this.parent=parent;this.game=game;game.start();}
    @Override protected void init(){}
    @Override public void tick(){if(finishedScreen)return;game.tick();if(game.isFinished()){finishedScreen=true;game.close();MinecraftClient.getInstance().setScreen(new ResultsScreen(parent,game));}}
    @Override public void render(DrawContext c,int mouseX,int mouseY,float delta){renderInGameBackground(c);int left=width/2-190,top=Math.max(28,height/2-125),right=width/2+190,bottom=top+235;c.fill(left,top,right,bottom,0xFF14181C);c.fill(left+4,top+4,right-4,bottom-4,0xFF252B31);game.render(c,mouseX,mouseY,delta);c.drawCenteredTextWithShadow(textRenderer,Text.literal("R  Restart     Esc  Exit"),width/2,bottom+10,0xFFAAAAAA);}
    @Override public boolean mouseClicked(double mx,double my,int button){return game.mouseClicked(mx,my,button)||super.mouseClicked(mx,my,button);}
    @Override public boolean keyPressed(int key,int scan,int mods){if(key==GLFW.GLFW_KEY_ESCAPE){close();return true;}if(key==GLFW.GLFW_KEY_R){game.close();game=getFreshGame();game.start();finishedScreen=false;return true;}game.keyPressed(key,scan,mods);return true;}
    private Game getFreshGame(){try{return game.getClass().getDeclaredConstructor().newInstance();}catch(Exception e){throw new IllegalStateException("Unable to restart "+game.id(),e);}}
    @Override public void close(){game.close();MinecraftClient.getInstance().setScreen(parent);}
}
