package com.gafipro.minigames.game.endless;

import com.gafipro.minigames.game.BaseGame;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public final class EndlessCollection {
    private EndlessCollection() {}
    private static final int FG=0xFFFFFFFF, BG=0xFF171B20, PLAYER=0xFF55CC88, HAZARD=0xFFE74C3C;

    public static final class Snake extends BaseGame {
        private int n=18,px,py,fx,fy,dx=1,dy=0;private final List<int[]> body=new ArrayList<>();private int step;
        @Override public String id(){return "snake";}@Override public String title(){return "Snake";}@Override public String category(){return "Endless";}
        @Override public void start(){body.clear();px=9;py=9;body.add(new int[]{px,py});spawn();step=0;}
        private void spawn(){do{fx=random.nextInt(n);fy=random.nextInt(n);}while(occupied(fx,fy));}
        private boolean occupied(int x,int y){for(int[]p:body)if(p[0]==x&&p[1]==y)return true;return false;}
        @Override public void tick(){super.tick();if(finished)return;if(ticks%Math.max(2,8-body.size()/3)==0){px+=dx;py+=dy;if(px<0||py<0||px>=n||py>=n||occupied(px,py)){finish(score);return;}body.add(new int[]{px,py});if(px==fx&&py==fy){score+=100;spawn();}else body.remove(0);}}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"SNAKE","Arrow keys / WASD • Score: "+score);int s=18,ox=cx()-n*s/2,oy=76;c.fill(ox-2,oy-2,ox+n*s+2,oy+n*s+2,BG);for(int[]p:body)c.fill(ox+p[0]*s,oy+p[1]*s,ox+p[0]*s+s-2,oy+p[1]*s+s-2,PLAYER);c.fill(ox+fx*s,oy+fy*s,ox+fx*s+s-2,oy+fy*s+s-2,HAZARD);}
        @Override public void keyPressed(int k,int sc,int m){if(k==GLFW.GLFW_KEY_LEFT||k==GLFW.GLFW_KEY_A){if(dx!=1){dx=-1;dy=0;}}else if(k==GLFW.GLFW_KEY_RIGHT||k==GLFW.GLFW_KEY_D){if(dx!=-1){dx=1;dy=0;}}else if(k==GLFW.GLFW_KEY_UP||k==GLFW.GLFW_KEY_W){if(dy!=1){dx=0;dy=-1;}}else if(k==GLFW.GLFW_KEY_DOWN||k==GLFW.GLFW_KEY_S){if(dy!=-1){dx=0;dy=1;}}}
        @Override public boolean mouseClicked(double x,double y,int b){return true;}
    }

    public static final class DinoRun extends BaseGame {
        private double py=0,vy=0;private int obstacle=350;private long start;
        @Override public String id(){return "dino_run";}@Override public String title(){return "Dino Run";}@Override public String category(){return "Endless";}
        @Override public void start(){py=0;vy=0;obstacle=300;start=System.currentTimeMillis();}
        @Override public void tick(){super.tick();if(finished)return;py+=vy;vy-=0.35;if(py<0){py=0;vy=0;}obstacle-=5;if(obstacle<0){obstacle=320+random.nextInt(120);if(py<28){finish(score);return;}score+=100;}}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"DINO RUN","Space / Up to jump • Score: "+score);int ground=210;c.fill(cx()-180,ground,cx()+180,ground+4,FG);c.fill(cx()-140,(int)(ground-32-py),cx()-110,(int)(ground-py),PLAYER);c.fill(cx()+obstacle-180,ground-28,cx()+obstacle-160,ground,HAZARD);}
        @Override public void keyPressed(int k,int sc,int m){if((k==GLFW.GLFW_KEY_SPACE||k==GLFW.GLFW_KEY_UP)&&py==0)vy=7.2;}
        @Override public boolean mouseClicked(double x,double y,int b){return true;}
    }

    public static final class FlappyBlock extends BaseGame {
        private double y=130,vy=0;private final List<Integer> gates=new ArrayList<>();private int frame;
        @Override public String id(){return "flappy_block";}@Override public String title(){return "Flappy Block";}@Override public String category(){return "Endless";}
        @Override public void start(){y=130;vy=0;gates.clear();gates.add(300);frame=0;}
        @Override public void tick(){super.tick();if(finished)return;frame++;vy+=0.28;y+=vy;for(int i=0;i<gates.size();i++)gates.set(i,gates.get(i)-3);if(frame%90==0)gates.add(360);if(y<45||y>240){finish(score);return;}if(!gates.isEmpty()&&gates.get(0)<cx()-170){gates.remove(0);score+=100;}if(!gates.isEmpty()&&gates.get(0)>-15&&gates.get(0)<20&&y<100){finish(score);}}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"FLAPPY BLOCK","Space / click to flap • Score: "+score);int px=cx()-120;c.fill(px,(int)y,px+22,(int)y+22,PLAYER);for(int gx:gates){int x=cx()+gx-180;int gap=randomGap(gx);c.fill(x,70,x+38,gap,HAZARD);c.fill(x,gap+75,x+38,240,HAZARD);}}
        private int randomGap(int x){return 110+(Math.abs(x*31)%45);}
        @Override public void keyPressed(int k,int s,int m){if(k==GLFW.GLFW_KEY_SPACE)vy=-5.5;}
        @Override public boolean mouseClicked(double x,double y,int b){if(b==0)vy=-5.5;return true;}
    }

    public static final class FallingBlocks extends BaseGame {
        private final int w=10,h=18;private int[][]b=new int[w][h];private int[][]piece={{1,1,1,1}};private int px,py;private long last;
        @Override public String id(){return "falling_blocks";}@Override public String title(){return "Falling Blocks";}@Override public String category(){return "Endless";}
        @Override public void start(){b=new int[w][h];spawn();last=System.currentTimeMillis();}
        private void spawn(){px=3+random.nextInt(3);py=0;piece=random.nextBoolean()?new int[][]{{1,1,1,1}}:new int[][]{{1,1},{1,1}};if(collide(px,py,piece))finish(score);}
        private boolean collide(int x,int y,int[][]p){for(int yy=0;yy<p.length;yy++)for(int xx=0;xx<p[yy].length;xx++)if(p[yy][xx]!=0&&(x+xx<0||x+xx>=w||y+yy>=h||(y+yy>=0&&b[x+xx][y+yy]!=0)))return true;return false;}
        private void lock(){for(int yy=0;yy<piece.length;yy++)for(int xx=0;xx<piece[yy].length;xx++)if(piece[yy][xx]!=0)b[px+xx][py+yy]=1;for(int y=h-1;y>=0;y--){boolean full=true;for(int x=0;x<w;x++)if(b[x][y]==0)full=false;if(full){for(int yy=y;yy>0;yy--)for(int x=0;x<w;x++)b[x][yy]=b[x][yy-1];score+=100;y++;}}spawn();}
        private void rotate(){int H=piece.length,W=piece[0].length;int[][]r=new int[W][H];for(int y=0;y<H;y++)for(int x=0;x<W;x++)r[x][H-1-y]=piece[y][x];if(!collide(px,py,r))piece=r;}
        @Override public void tick(){super.tick();if(finished)return;if(System.currentTimeMillis()-last>450){last=System.currentTimeMillis();if(collide(px,py+1,piece))lock();else py++;}}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"FALLING BLOCKS","Arrows move • Up rotate • Score: "+score);int s=20,ox=cx()-w*s/2,oy=70;for(int y=0;y<h;y++)for(int x=0;x<w;x++)c.fill(ox+x*s,oy+y*s,ox+x*s+s-2,oy+y*s+s-2,b[x][y]!=0?0xFF55AADD:0xFF252B31);for(int yy=0;yy<piece.length;yy++)for(int xx=0;xx<piece[yy].length;xx++)if(piece[yy][xx]!=0)c.fill(ox+(px+xx)*s,oy+(py+yy)*s,ox+(px+xx)*s+s-2,oy+(py+yy)*s+s-2,0xFFE67E22);}
        @Override public void keyPressed(int k,int sc,int m){if(finished)return;if(k==GLFW.GLFW_KEY_LEFT&&!collide(px-1,py,piece))px--;if(k==GLFW.GLFW_KEY_RIGHT&&!collide(px+1,py,piece))px++;if(k==GLFW.GLFW_KEY_DOWN&&!collide(px,py+1,piece))py++;if(k==GLFW.GLFW_KEY_UP)rotate();}
        @Override public boolean mouseClicked(double x,double y,int b){return true;}
    }

    public static final class Breakout extends BaseGame {
        private double x=0,y=120,dx=3,dy=-3;private int paddle=0;private boolean[][]br=new boolean[8][5];
        @Override public String id(){return "breakout";}@Override public String title(){return "Breakout";}@Override public String category(){return "Endless";}
        @Override public void start(){for(boolean[]r:br)Arrays.fill(r,true);x=0;y=170;dx=3;dy=-3;paddle=0;}
        @Override public void tick(){super.tick();if(finished)return;x+=dx;y+=dy;if(x<-165||x>165){dx=-dx;}if(y<70)dy=-dy;if(y>220){if(Math.abs(x-paddle)>45){finish(score);return;}dy=-Math.abs(dy);y=219;}int bx=(int)((x+160)/40),by=(int)((y-70)/25);if(bx>=0&&bx<8&&by>=0&&by<5&&br[bx][by]){br[bx][by]=false;score+=50;dy=-dy;if(score>=2000)finishWin(score);}}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"BREAKOUT","A / D or arrows • Score: "+score);for(int yy=0;yy<5;yy++)for(int xx=0;xx<8;xx++)if(br[xx][yy])c.fill(cx()-160+xx*40,70+yy*25,cx()-122+xx*40,91+yy*25,0xFFE67E22);c.fill(cx()+paddle-45,225,cx()+paddle+45,235,PLAYER);c.fill(cx()+(int)x-5,cy()+(int)y-cy(),cx()+(int)x+5,cy()+(int)y-cy()+10,FG);}
        @Override public void keyPressed(int k,int s,int m){if(k==GLFW.GLFW_KEY_LEFT||k==GLFW.GLFW_KEY_A)paddle-=12;if(k==GLFW.GLFW_KEY_RIGHT||k==GLFW.GLFW_KEY_D)paddle+=12;paddle=Math.max(-130,Math.min(130,paddle));}
        @Override public boolean mouseClicked(double x,double y,int b){if(b==0)paddle=(int)x-cx();return true;}
    }

    public static final class Pong extends BaseGame {
        private double ballX,ballY,dx=3,dy=2,paddleY;private int cpu;
        @Override public String id(){return "pong";}@Override public String title(){return "Pong";}@Override public String category(){return "Endless";}
        @Override public void start(){ballX=0;ballY=0;paddleY=0;cpu=0;}
        @Override public void tick(){super.tick();if(finished)return;ballX+=dx;ballY+=dy;if(ballY<-85||ballY>85)dy=-dy;double ai=Math.max(-75,Math.min(75,ballY));paddleY+=(ai-paddleY)*0.08;if(ballX<-155){if(Math.abs(ballY-paddleY)<20)dx=-dx;else {finish(score);return;}}if(ballX>155){dx=-Math.abs(dx);score+=50;}if(Math.abs(ballX)<8&&Math.abs(ballY)<20)dx=-dx;}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"PONG","W/S or Up/Down • Score: "+score);c.fill(cx()-160,cy()-20,cx()-150,cy()+20,PLAYER);c.fill(cx()+150,(int)(cy()+paddleY-20),cx()+160,(int)(cy()+paddleY+20),0xFF55AADD);c.fill((int)(cx()+ballX-5),(int)(cy()+ballY-5),(int)(cx()+ballX+5),(int)(cy()+ballY+5),FG);}
        @Override public void keyPressed(int k,int s,int m){if(k==GLFW.GLFW_KEY_UP||k==GLFW.GLFW_KEY_W)paddleY-=12;if(k==GLFW.GLFW_KEY_DOWN||k==GLFW.GLFW_KEY_S)paddleY+=12;paddleY=Math.max(-75,Math.min(75,paddleY));}
        @Override public boolean mouseClicked(double x,double y,int b){paddleY=y-cy();return true;}
    }

    public static class Frogger extends BaseGame {
        private int lane=5,row=9;private int[] cars={0,60,120,190};
        @Override public String id(){return "frogger";}@Override public String title(){return "Frogger";}@Override public String category(){return "Endless";}
        @Override public void start(){row=9;lane=5;}
        @Override public void tick(){super.tick();if(finished)return;for(int i=0;i<cars.length;i++)cars[i]=(cars[i]+2)%340;if(row>1&&row<9)for(int x:cars)if(Math.abs(x-(cx()-150))<15){finish(score);return;}}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"FROGGER","Arrow keys / WASD • Reach the top.");int s=32,ox=cx()-160,oy=72;for(int y=0;y<10;y++){c.fill(ox,oy+y*s,ox+320,oy+y*s+s-2,y==0?0xFF3A8A58:0xFF303840);}c.fill(ox+lane*s+7,oy+row*s+6,ox+lane*s+s-9,oy+row*s+s-9,PLAYER);for(int car:cars)c.fill(ox+car,oy+96,ox+car+28,oy+124,HAZARD);}
        @Override public void keyPressed(int k,int s,int m){int nr=row,nl=lane;if(k==GLFW.GLFW_KEY_UP||k==GLFW.GLFW_KEY_W)nr--;if(k==GLFW.GLFW_KEY_DOWN||k==GLFW.GLFW_KEY_S)nr++;if(k==GLFW.GLFW_KEY_LEFT||k==GLFW.GLFW_KEY_A)nl--;if(k==GLFW.GLFW_KEY_RIGHT||k==GLFW.GLFW_KEY_D)nl++;row=Math.max(0,Math.min(9,nr));lane=Math.max(0,Math.min(9,nl));score+=10;if(row==0)finishWin(score+500);}
        @Override public boolean mouseClicked(double x,double y,int b){return true;}
    }

    public static class Avoider extends BaseGame {
        private double px;private final List<double[]>h=new ArrayList<>();
        @Override public String id(){return "avoider";}@Override public String title(){return "Avoider";}@Override public String category(){return "Endless";}
        @Override public void start(){px=cx();h.clear();for(int i=0;i<5;i++)h.add(new double[]{cx()-150+random.nextInt(300),60+random.nextInt(150),1+random.nextDouble()*2});}
        @Override public void tick(){super.tick();if(finished)return;for(double[]a:h){a[1]+=a[2];if(a[1]>250){a[0]=cx()-150+random.nextInt(300);a[1]=65;score+=20;}}if(Math.abs(px-h.get(0)[0])<18&&Math.abs(210-h.get(0)[1])<18)finish(score);}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"AVOIDER","A / D or arrows • Score: "+score);c.fill((int)px-10,200,(int)px+10,220,PLAYER);for(double[]a:h)c.fill((int)a[0]-8,(int)a[1]-8,(int)a[0]+8,(int)a[1]+8,HAZARD);}
        @Override public void keyPressed(int k,int s,int m){if(k==GLFW.GLFW_KEY_LEFT||k==GLFW.GLFW_KEY_A)px-=15;if(k==GLFW.GLFW_KEY_RIGHT||k==GLFW.GLFW_KEY_D)px+=15;px=Math.max(cx()-165,Math.min(cx()+165,px));}
        @Override public boolean mouseClicked(double x,double y,int b){if(b==0)px=x;return true;}
    }

    public static final class TowerClimber extends BaseGame {
        private double px=0,py=0,vy=0;private final List<double[]> platforms=new ArrayList<>();
        @Override public String id(){return "tower_climber";}@Override public String title(){return "Tower Climber";}@Override public String category(){return "Endless";}
        @Override public void start(){platforms.clear();for(int i=0;i<12;i++)platforms.add(new double[]{-140+random.nextInt(280),220-i*45});}
        @Override public void tick(){super.tick();if(finished)return;vy-=0.25;py-=vy;for(double[]p:platforms)if(vy<0&&py>p[1]-10&&py<p[1]+10&&Math.abs(px-p[0])<45)vy=7;if(py>230)finish(score);if(py<80){score+=100;for(double[]p:platforms)p[1]+=45;py+=45;}}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"TOWER CLIMBER","Left / Right to steer • Score: "+score);for(double[]p:platforms)c.fill((int)(cx()+p[0]-40),(int)p[1],(int)(cx()+p[0]+40),(int)p[1]+8,0xFFE67E22);c.fill((int)(cx()+px-10),(int)(190-py),(int)(cx()+px+10),(int)(210-py),PLAYER);}
        @Override public void keyPressed(int k,int s,int m){if(k==GLFW.GLFW_KEY_LEFT||k==GLFW.GLFW_KEY_A)px-=12;if(k==GLFW.GLFW_KEY_RIGHT||k==GLFW.GLFW_KEY_D)px+=12;px=Math.max(-150,Math.min(150,px));}
        @Override public boolean mouseClicked(double x,double y,int b){return true;}
    }

    public static final class EndlessDodger extends Avoider {
        @Override public String id(){return "endless_dodger";}@Override public String title(){return "Endless Dodger";}@Override public String category(){return "Endless";}
    }
}
