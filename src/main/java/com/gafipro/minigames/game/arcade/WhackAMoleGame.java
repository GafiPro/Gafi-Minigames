package com.gafipro.minigames.game.arcade;

import com.gafipro.minigames.game.BaseGame;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.concurrent.ThreadLocalRandom;

public final class WhackAMoleGame extends BaseGame {
    private int target = 0;
    private int hits;
    private int misses;
    private int rounds;
    private int timer;

    @Override public String id() { return "whack_a_mole"; }
    @Override public String title() { return "Whack-A-Mole"; }
    @Override public String category() { return "Arcade"; }
    @Override public void start() { nextTarget(); }
    @Override public void tick() {
        ticks++;
        timer--;
        if (timer <= 0) {
            misses++;
            nextTarget();
        }
        if (rounds >= 25) finish(hits * 10 - misses * 2);
    }
    private void nextTarget() {
        target = ThreadLocalRandom.current().nextInt(0, 27);
        timer = Math.max(7, 18 - rounds / 4);
        rounds++;
    }
    @Override public void render(DrawContext c, int mouseX, int mouseY, float delta) {
        var mc=net.minecraft.client.MinecraftClient.getInstance(); var tr=mc.textRenderer; int cx=mc.getWindow().getScaledWidth()/2;
        drawHeader(c,"WHACK-A-MOLE","Hit the mole before it moves • Round "+Math.min(rounds,25)+"/25");
        int sx=cx-117, sy=78, s=76, gap=6;
        for(int i=0;i<27;i++){
            int col=i%9,row=i/9,x=sx+col*(12+gap),y=sy+row*(32+gap);
            // 9 columns keep the board compact inside the generic game panel.
            c.fill(x,y,x+s/2,y+s/2, i==target ? 0xFF55C7FF : 0xFF343A40);
            if(i==target) c.drawCenteredTextWithShadow(tr,Text.literal("●").formatted(Formatting.GOLD),x+s/4,y+10,0xFFFFFFFF);
        }
        c.drawTextWithShadow(tr,Text.literal("Hits: "+hits+"   Misses: "+misses+"   Score: "+Math.max(0,hits*10-misses*2)),cx-105,184,0xFFFFFFFF);
    }
    @Override public boolean mouseClicked(double mx,double my,int button){
        if(button!=0)return true; int cx=net.minecraft.client.MinecraftClient.getInstance().getWindow().getScaledWidth()/2;
        int sx=cx-117,sy=78,s=76,gap=6;
        for(int i=0;i<27;i++){int col=i%9,row=i/9,x=sx+col*(12+gap),y=sy+row*(32+gap);if(inside(mx,my,x,y,s/2,s/2)){if(i==target){hits++;nextTarget();}else{misses++;}return true;}}
        return true;
    }
}
