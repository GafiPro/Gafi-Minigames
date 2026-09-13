package com.gafipro.minigames.game.endless;

import com.gafipro.minigames.game.BaseGame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

public final class PongGame extends BaseGame {
    private double ballX,ballY,vx,vy,playerY,cpuY,targetY;
    private int playerScore,cpuScore; private long aiNext,lastSimNanos;
    @Override public String id(){return "pong";} @Override public String title(){return "Pong";} @Override public String category(){return "Endless";}
    @Override public void start(){playerScore=cpuScore=0;resetBall(random.nextBoolean()?1:-1);playerY=0;cpuY=0;targetY=0;long now=System.nanoTime();aiNext=now;lastSimNanos=now;}
    private double fieldHalfWidth(){return Math.max(120,Math.min(180,MinecraftClient.getInstance().getWindow().getScaledWidth()/2.0-12));}
    private double paddleX(boolean left){return left?-(fieldHalfWidth()-5):(fieldHalfWidth()-5);}
    private void resetBall(int dir){ballX=0;ballY=random.nextInt(100)-50;vx=dir*190.0;vy=(random.nextBoolean()?1:-1)*(102.0+random.nextDouble()*102.0);}
    @Override public void tick(){
        super.tick();if(finished)return;
        long now=System.nanoTime();double dt=Math.max(0.0,Math.min(0.10,(now-lastSimNanos)/1_000_000_000.0));lastSimNanos=now;
        ballX+=vx*dt;ballY+=vy*dt;
        if(ballY<-92||ballY>92){vy=-vy;ballY=Math.max(-92,Math.min(92,ballY));}
        if(now>=aiNext){targetY=ballY+(random.nextDouble()*28-14);aiNext=now+(70+random.nextInt(100))*1_000_000L;}
        double cpuSpeed=162.0;double delta=targetY-cpuY;cpuY+=Math.max(-cpuSpeed*dt,Math.min(cpuSpeed*dt,delta));
        double leftP=paddleX(true),rightP=paddleX(false);
        if(vx<0&&ballX>leftP-14&&ballX<leftP-0&&Math.abs(ballY-playerY)<23){ballX=leftP+6;vx=Math.abs(vx)+5.0;vy+=(ballY-playerY)*4.0;}
        if(vx>0&&ballX<rightP+14&&ballX>rightP&&Math.abs(ballY-cpuY)<23){ballX=rightP-6;vx=-Math.abs(vx)-5.0;vy+=(ballY-cpuY)*3.2;}
        double goalX=fieldHalfWidth()+8;
        if(ballX<-goalX){cpuScore++;resetBall(1);}else if(ballX>goalX){playerScore++;resetBall(-1);}
        score=Math.max(0,playerScore*100-cpuScore*25);
        if(playerScore>=5)finishWin(score);else if(cpuScore>=5)finish(score);
    }
    @Override public void keyPressed(int k,int s,int m){if(k==GLFW.GLFW_KEY_R){begin();return;}if(finished)return;if(k==GLFW.GLFW_KEY_UP||k==GLFW.GLFW_KEY_W)playerY-=14;if(k==GLFW.GLFW_KEY_DOWN||k==GLFW.GLFW_KEY_S)playerY+=14;playerY=Math.max(-72,Math.min(72,playerY));}
    @Override public boolean mouseClicked(double x,double y,int b){if(b!=0||finished)return true;playerY=y-MinecraftClient.getInstance().getWindow().getScaledHeight()/2.0;playerY=Math.max(-72,Math.min(72,playerY));return true;}
    @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"PONG","W/S or Up/Down • You "+playerScore+" - "+cpuScore+" CPU");int cy=MinecraftClient.getInstance().getWindow().getScaledHeight()/2;int ox=cx();int left=(int)paddleX(true),right=(int)paddleX(false);c.fill(ox+left,(int)(cy+playerY-22),ox+left+9,(int)(cy+playerY+22),0xFF55CC88);c.fill(ox+right,(int)(cy+cpuY-22),ox+right+9,(int)(cy+cpuY+22),0xFF55AADD);c.fill((int)(ox+ballX-5),(int)(cy+ballY-5),(int)(ox+ballX+5),(int)(cy+ballY+5),0xFFFFFFFF);}
}
