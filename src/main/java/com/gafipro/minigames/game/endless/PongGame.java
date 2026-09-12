package com.gafipro.minigames.game.endless;

import com.gafipro.minigames.game.BaseGame;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

public final class PongGame extends BaseGame {
    private double ballX,ballY,vx,vy,playerY,cpuY,targetY;
    private int playerScore,cpuScore; private long aiNext;
    @Override public String id(){return "pong";} @Override public String title(){return "Pong";} @Override public String category(){return "Endless";}
    @Override public void start(){playerScore=cpuScore=0;resetBall(random.nextBoolean()?1:-1);playerY=0;cpuY=0;targetY=0;aiNext=System.nanoTime();}
    private void resetBall(int dir){ballX=0;ballY=random.nextInt(100)-50;vx=dir*3.2;vy=(random.nextBoolean()?1:-1)*(1.7+random.nextDouble()*1.7);}
    @Override public void tick(){super.tick();if(finished)return;ballX+=vx;ballY+=vy;if(ballY<-92||ballY>92){vy=-vy;ballY=Math.max(-92,Math.min(92,ballY));}
        long now=System.nanoTime();if(now>=aiNext){targetY=ballY+(random.nextDouble()*28-14);aiNext=now+(70+random.nextInt(100))*1_000_000L;}cpuY+=Math.max(-2.7,Math.min(2.7,(targetY-cpuY)*0.16));
        if(vx<0&&ballX>-154&&ballX<-140&&Math.abs(ballY-playerY)<23){ballX=-139;vx=Math.abs(vx)+0.08;vy+=(ballY-playerY)*0.06;}
        if(vx>0&&ballX<154&&ballX>140&&Math.abs(ballY-cpuY)<23){ballX=139;vx=-Math.abs(vx)-0.08;vy+=(ballY-cpuY)*0.05;}
        if(ballX<-185){cpuScore++;resetBall(1);}else if(ballX>185){playerScore++;score=playerScore*100;if(playerScore>=5)finishWin(score);else resetBall(-1);}score=playerScore*100-cpuScore*25;if(cpuScore>=5)finish(score);}
    @Override public void keyPressed(int k,int s,int m){if(k==GLFW.GLFW_KEY_R){begin();return;}if(k==GLFW.GLFW_KEY_UP||k==GLFW.GLFW_KEY_W)playerY-=12;if(k==GLFW.GLFW_KEY_DOWN||k==GLFW.GLFW_KEY_S)playerY+=12;playerY=Math.max(-72,Math.min(72,playerY));}
    @Override public boolean mouseClicked(double x,double y,int b){if(b==0)playerY=y-net.minecraft.client.MinecraftClient.getInstance().getWindow().getScaledHeight()/2.0;return true;}
    @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"PONG","W/S or Up/Down • You "+playerScore+" - "+cpuScore+" CPU");int cy=net.minecraft.client.MinecraftClient.getInstance().getWindow().getScaledHeight()/2;int ox=cx();c.fill(ox-180,(int)(cy+playerY-22),ox-171,(int)(cy+playerY+22),0xFF55CC88);c.fill(ox+171,(int)(cy+cpuY-22),ox+180,(int)(cy+cpuY+22),0xFF55AADD);c.fill((int)(ox+ballX-5),(int)(cy+ballY-5),(int)(ox+ballX+5),(int)(cy+ballY+5),0xFFFFFFFF);}
}
