package com.gafipro.minigames.gui;

import com.gafipro.minigames.game.BaseGame;
import com.gafipro.minigames.game.Game;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class GameScreen extends Screen {
    private final Screen parent;
    private final Game game;

    public GameScreen(Screen parent, Game game) {
        super(Text.literal(game.title()));
        this.parent = parent;
        this.game = game;
        beginGame();
    }

    private void beginGame() { if (game instanceof BaseGame base) base.begin(); else game.start(); }

    @Override protected void init() { }

    @Override public void tick() {
        game.tick();
        if (game.isFinished()) {
            game.close();
            MinecraftClient.getInstance().setScreen(new ResultsScreen(parent, game));
        }
    }

    @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderInGameBackground(context);
        int left=width/2-182, top=Math.max(30,height/2-105), right=width/2+182, bottom=top+210;
        context.fill(left,top,right,bottom,0xFF171B20);
        context.fill(left+4,top+4,right-4,bottom-4,0xFF252B32);
        game.render(context,mouseX,mouseY,delta);
        context.drawCenteredTextWithShadow(textRenderer,Text.literal("R  Restart    ESC  Exit"),width/2,bottom+10,0xFFAAAAAA);
    }

    @Override public boolean mouseClicked(Click click, boolean doubled) {
        return game.mouseClicked(click.x(),click.y(),click.button()) || super.mouseClicked(click,doubled);
    }

    @Override public boolean keyPressed(KeyInput input) {
        int key=input.key();
        if(key==GLFW.GLFW_KEY_R){beginGame();return true;}
        if(key==GLFW.GLFW_KEY_ESCAPE){close();return true;}
        game.keyPressed(key,input.scancode(),input.modifiers());
        return true;
    }

    @Override public void close(){game.close();MinecraftClient.getInstance().setScreen(parent);}
}
