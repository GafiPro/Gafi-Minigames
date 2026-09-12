package com.gafipro.minigames.game.board;

import com.gafipro.minigames.game.BaseGame;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.concurrent.ThreadLocalRandom;

public final class RockPaperScissorsGame extends BaseGame {
    private final String[] choices = {"ROCK", "PAPER", "SCISSORS"};
    private int selected = -1;
    private int opponent = -1;
    private String result = "Choose your move";

    @Override public String id() { return "rock_paper_scissors"; }
    @Override public String title() { return "Rock Paper Scissors"; }
    @Override public String category() { return "Quick"; }
    @Override public void start() { selected = -1; opponent = -1; result = "Choose your move"; }
    @Override public void tick() { }
    @Override public void render(DrawContext c,int mouseX,int mouseY,float delta){
        var mc=MinecraftClient.getInstance();var tr=mc.textRenderer;int cx=mc.getWindow().getScaledWidth()/2;
        drawHeader(c,"ROCK PAPER SCISSORS","Fast AI match");
        c.drawCenteredTextWithShadow(tr,Text.literal(result).formatted(Formatting.BOLD),cx,82,0xFFFFFFFF);
        if(opponent>=0)c.drawCenteredTextWithShadow(tr,Text.literal("AI: "+choices[opponent]),cx,101,0xFFFFAA55);
        int w=92,g=8,sx=cx-(w*3+g*2)/2, y=125;
        for(int i=0;i<3;i++){int x=sx+i*(w+g);boolean h=inside(mouseX,mouseY,x,y,w,42);c.fill(x,y,x+w,y+42,h?0xFF3E6570:0xFF30363D);c.drawCenteredTextWithShadow(tr,Text.literal(choices[i]).formatted(Formatting.BOLD),x+w/2,y+14,0xFFFFFFFF);}
    }
    @Override public boolean mouseClicked(double mx,double my,int button){
        if(button!=0)return true;int cx=MinecraftClient.getInstance().getWindow().getScaledWidth()/2,w=92,g=8,sx=cx-(w*3+g*2)/2,y=125;
        for(int i=0;i<3;i++){int x=sx+i*(w+g);if(inside(mx,my,x,y,w,42)){selected=i;opponent=ThreadLocalRandom.current().nextInt(3);int outcome=(selected-opponent+3)%3;if(outcome==0){score=1;result="Draw! Click again";}else if(outcome==1){score=2;result="You win! Click again";}else{score=0;result="You lose. Click again";}return true;}}
        return true;
    }
}
