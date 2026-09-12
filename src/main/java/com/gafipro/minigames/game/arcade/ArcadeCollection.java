package com.gafipro.minigames.game.arcade;

import com.gafipro.minigames.core.WordBank;
import com.gafipro.minigames.game.BaseGame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Compact arcade games that remain small enough to share one implementation file. */
public final class ArcadeCollection {
    private ArcadeCollection() {}
    private static final int[] COLORS={0xFFE74C3C,0xFF3498DB,0xFF2ECC71,0xFFF1C40F};
    private static final String[] COLOR_NAMES={"RED","BLUE","GREEN","YELLOW"};

    public static final class ColorRush extends BaseGame {
        private int word,ink,round,streak,mistakes,correct;
        private long deadline;
        @Override public String id(){return "color_rush";} @Override public String title(){return "Color Rush";} @Override public String category(){return "Arcade";}
        @Override public void start(){round=streak=mistakes=correct=0;nextRound();}
        private void nextRound(){word=random.nextInt(4);ink=random.nextInt(4);if(random.nextBoolean()&&ink==word)ink=(ink+1)%4;deadline=System.nanoTime()+4_000_000_000L;status="Choose the ink color.";}
        private void accuracy(){int attempts=correct+mistakes;metrics.accuracyPercent(attempts==0?100:correct*100.0/attempts);}
        private void answer(int v){if(v==ink){correct++;streak++;score+=100+streak*20;round++;accuracy();if(round>=12)finishWin(score);else nextRound();}else{mistakes++;streak=0;round++;accuracy();if(round>=12)finish(score);else nextRound();}}
        @Override public void tick(){super.tick();if(!finished&&System.nanoTime()>=deadline){mistakes++;round++;accuracy();if(round>=12)finish(score);else nextRound();}}
        @Override public boolean mouseClicked(double mx,double my,int b){if(b!=0||finished)return true;for(int i=0;i<4;i++){int x=cx()-150+i*100;if(inside(mx,my,x,135,85,55)){answer(i);return true;}}return true;}
        @Override public void keyPressed(int k,int s,int m){if(k==GLFW.GLFW_KEY_R)begin();else if(k>=GLFW.GLFW_KEY_1&&k<=GLFW.GLFW_KEY_4)answer(k-GLFW.GLFW_KEY_1);}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"COLOR RUSH","Choose the ink color • Round "+(round+1)+" / 12 • Mistakes: "+mistakes);c.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer,Text.literal(COLOR_NAMES[word]),cx(),88,COLORS[ink]);for(int i=0;i<4;i++){int x=cx()-150+i*100;c.fill(x,135,x+85,190,COLORS[i]);c.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer,Text.literal(COLOR_NAMES[i]),x+42,154,0xFFFFFFFF);}}
    }

    public static final class PatternCopy extends BaseGame {
        private final boolean[][] target=new boolean[4][4],selected=new boolean[4][4]; private boolean showing; private long hideAt; private int round,needed;
        @Override public String id(){return "pattern_copy";} @Override public String title(){return "Pattern Copy";} @Override public String category(){return "Arcade";}
        @Override public void start(){round=0;pattern();}
        private void pattern(){for(int x=0;x<4;x++)for(int y=0;y<4;y++){target[x][y]=selected[x][y]=false;}needed=4+random.nextInt(Math.min(7,round+4));List<Integer> p=new ArrayList<>();for(int i=0;i<16;i++)p.add(i);Collections.shuffle(p,random);for(int i=0;i<needed;i++){int q=p.get(i);target[q%4][q/4]=true;}showing=true;hideAt=System.nanoTime()+1_500_000_000L;status="Memorize the pattern.";}
        private boolean correct(){for(int x=0;x<4;x++)for(int y=0;y<4;y++)if(target[x][y]!=selected[x][y])return false;return true;}
        @Override public void tick(){super.tick();if(showing&&System.nanoTime()>=hideAt){showing=false;status="Recreate the pattern and press Enter.";}}
        @Override public boolean mouseClicked(double mx,double my,int b){if(b!=0||finished||showing)return true;int s=50,ox=cx()-100,oy=90,x=(int)((mx-ox)/s),y=(int)((my-oy)/s);if(x>=0&&y>=0&&x<4&&y<4)selected[x][y]=!selected[x][y];return true;}
        @Override public void keyPressed(int k,int s,int m){if(k==GLFW.GLFW_KEY_R)begin();else if(k==GLFW.GLFW_KEY_ENTER&&!showing&&!finished){if(correct()){round++;score+=needed*100;if(round>=6)finishWin(score);else pattern();}else finish(score);}}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"PATTERN COPY",showing?"Memorize the pattern.":"Recreate the pattern and press Enter.");int s=50,ox=cx()-100,oy=90;for(int x=0;x<4;x++)for(int y=0;y<4;y++)c.fill(ox+x*s+2,oy+y*s+2,ox+x*s+s-2,oy+y*s+s-2,(showing&&target[x][y])||(!showing&&selected[x][y])?0xFF55AAFF:0xFF3B424A);}
    }

    public static final class FastClick extends BaseGame {
        private int clicks,tx,ty; private long end,firstClickNanos,lastClickNanos,totalIntervalNanos;
        @Override public String id(){return "fast_click";} @Override public String title(){return "Fast Click";} @Override public String category(){return "Arcade";}
        @Override public void start(){clicks=0;end=System.nanoTime()+15_000_000_000L;firstClickNanos=lastClickNanos=totalIntervalNanos=0;move();}
        private void move(){tx=cx()-120+random.nextInt(241);ty=100+random.nextInt(120);}
        @Override public void tick(){super.tick();if(!finished&&System.nanoTime()>=end){metrics.accuracyPercent(clicks/20.0*100.0);if(clicks>=20)finishWin(score);else finish(score);}}
        @Override public boolean mouseClicked(double mx,double my,int b){if(b!=0||finished)return true;if(Math.hypot(mx-tx,my-ty)<=22){long now=System.nanoTime();if(firstClickNanos==0)firstClickNanos=now;if(lastClickNanos!=0)totalIntervalNanos+=now-lastClickNanos;lastClickNanos=now;clicks++;score=clicks*100;metrics.accuracyPercent(Math.min(100.0,clicks/20.0*100.0));metrics.elapsedNanos(now-firstClickNanos);if(clicks>=20)finishWin(score);else move();}return true;}
        @Override public void render(DrawContext c,int mx,int my,float d){String avg=clicks<2?"-":String.format("%.0f ms",totalIntervalNanos/1_000_000.0/(clicks-1));drawHeader(c,"FAST CLICK","20 targets • Clicks: "+clicks+" • Avg interval: "+avg);c.fill(tx-22,ty-22,tx+22,ty+22,0xFFE74C3C);c.fill(tx-4,ty-4,tx+4,ty+4,0xFFFFFFFF);}
        @Override public void keyPressed(int k,int s,int m){if(k==GLFW.GLFW_KEY_R)begin();}
    }

    public static final class SafeTile extends BaseGame {
        private int safe,round; private boolean showing; private long hide;
        @Override public String id(){return "safe_tile";} @Override public String title(){return "Safe Tile";} @Override public String category(){return "Arcade";}
        @Override public void start(){round=0;newRound();}
        private void newRound(){safe=random.nextInt(25);showing=true;hide=System.nanoTime()+1_200_000_000L;status="Memorize the safe tile.";}
        @Override public void tick(){super.tick();if(showing&&System.nanoTime()>=hide){showing=false;status="Find the safe tile.";}}
        @Override public boolean mouseClicked(double mx,double my,int b){if(b!=0||finished||showing)return true;int s=45,ox=cx()-112,oy=85,x=(int)((mx-ox)/s),y=(int)((my-oy)/s);if(x>=0&&y>=0&&x<5&&y<5){if(y*5+x==safe){round++;score+=100+round*25;if(round>=10)finishWin(score);else newRound();}else finish(score);}return true;}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"SAFE TILE",status);int s=45,ox=cx()-112,oy=85;for(int i=0;i<25;i++){int x=ox+(i%5)*s,y=oy+(i/5)*s;c.fill(x+2,y+2,x+s-2,y+s-2,showing&&i==safe?0xFF55CC88:0xFF3B424A);}}
        @Override public void keyPressed(int k,int s,int m){if(k==GLFW.GLFW_KEY_R)begin();}
    }

    public static final class TargetPractice extends BaseGame {
        private double x,y,dx,dy;private int hits;private long end,lastNanos;
        @Override public String id(){return "target_practice";}@Override public String title(){return "Target Practice";}@Override public String category(){return "Arcade";}
        @Override public void start(){x=cx();y=130;dx=145;dy=105;hits=0;end=System.nanoTime()+15_000_000_000L;lastNanos=System.nanoTime();}
        @Override public void tick(){super.tick();if(finished)return;long now=System.nanoTime();if(now>=end){finishWin(score);return;}double dt=Math.min(0.05,Math.max(0,(now-lastNanos)/1_000_000_000.0));lastNanos=now;x+=dx*dt;y+=dy*dt;if(x<cx()-150||x>cx()+150)dx=-dx;if(y<90||y>235)dy=-dy;}
        @Override public boolean mouseClicked(double mx,double my,int b){if(b==0&&!finished&&Math.hypot(mx-x,my-y)<=25){hits++;score=hits*100;dx*=1.02;dy*=1.02;markMove();}return true;}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"TARGET PRACTICE","Hits: "+hits+" • Score: "+score);c.fill((int)x-18,(int)y-18,(int)x+18,(int)y+18,0xFFE74C3C);c.fill((int)x-5,(int)y-5,(int)x+5,(int)y+5,0xFFFFFFFF);}
        @Override public void keyPressed(int k,int s,int m){if(k==GLFW.GLFW_KEY_R)begin();}
    }

    public static final class MathRush extends BaseGame {
        private int a,b,op,answer,round,correct,total;private final int[] options=new int[4];private long deadline;
        @Override public String id(){return "math_rush";}@Override public String title(){return "Math Rush";}@Override public String category(){return "Arcade";}
        @Override public void start(){round=correct=total=0;next();}
        private void next(){a=2+random.nextInt(18);b=2+random.nextInt(18);op=random.nextInt(3);answer=op==0?a+b:op==1?a-b:a*b;options[0]=answer;for(int i=1;i<4;i++){int v;do v=answer+random.nextInt(25)-12;while(v==answer||contains(v,i));options[i]=v;}for(int i=3;i>0;i--){int j=random.nextInt(i+1);int t=options[i];options[i]=options[j];options[j]=t;}deadline=System.nanoTime()+4_500_000_000L;status="Choose the correct answer.";}
        private boolean contains(int v,int n){for(int i=0;i<n;i++)if(options[i]==v)return true;return false;}
        private void record(boolean ok){total++;if(ok)correct++;metrics.accuracyPercent(correct*100.0/total);}
        private void pick(int i){if(i<0||i>=4||finished)return;record(options[i]==answer);if(options[i]!=answer){finish(score);return;}round++;score+=100;if(round>=10)finishWin(score);else next();}
        @Override public void tick(){super.tick();if(!finished&&System.nanoTime()>=deadline){record(false);finish(score);}}
        @Override public boolean mouseClicked(double mx,double my,int b){if(b!=0)return true;for(int i=0;i<4;i++){int x=cx()-140+(i%2)*145,y=125+(i/2)*65;if(inside(mx,my,x,y,130,55)){pick(i);return true;}}return true;}
        @Override public void keyPressed(int k,int s,int m){if(k==GLFW.GLFW_KEY_R){begin();return;}if(k>=GLFW.GLFW_KEY_1&&k<=GLFW.GLFW_KEY_4)pick(k-GLFW.GLFW_KEY_1);}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"MATH RUSH","Question "+(round+1)+" / 10 • Accuracy: "+String.format("%.0f%%",metrics.accuracyPercent()));c.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer,Text.literal(a+(op==0?" + ":op==1?" - ":" × ")+b+" = ?"),cx(),88,0xFFFFFFFF);for(int i=0;i<4;i++){int x=cx()-140+(i%2)*145,y=125+(i/2)*65;c.fill(x,y,x+130,y+55,0xFF3A424A);c.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer,Text.literal(String.valueOf(options[i])),x+65,y+20,0xFFFFFFFF);}}
    }

    public static final class WordScramble extends BaseGame {
        private String word,scramble,input="";private int round;private long deadline;
        @Override public String id(){return "word_scramble";}@Override public String title(){return "Word Scramble";}@Override public String category(){return "Arcade";}
        @Override public void start(){round=0;next();}
        private void next(){word=WordBank.random(random);List<Character> a=new ArrayList<>();for(char c:word.toCharArray())a.add(c);do{Collections.shuffle(a,random);StringBuilder b=new StringBuilder();a.forEach(b::append);scramble=b.toString();}while(scramble.equals(word)&&word.length()>1);input="";deadline=System.nanoTime()+8_000_000_000L;status="Unscramble and press Enter.";}
        @Override public void tick(){super.tick();if(System.nanoTime()>=deadline)finish(score);}
        @Override public void keyPressed(int k,int s,int m){if(k==GLFW.GLFW_KEY_R){begin();return;}if(finished)return;if(k==GLFW.GLFW_KEY_BACKSPACE&&!input.isEmpty())input=input.substring(0,input.length()-1);else if(k==GLFW.GLFW_KEY_ENTER){if(input.equalsIgnoreCase(word)){round++;score+=100;metrics.accuracyPercent(100);if(round>=8)finishWin(score);else next();}else{metrics.accuracyPercent(round==0?0:round*100.0/(round+1));finish(score);}}else if(k>=GLFW.GLFW_KEY_A&&k<=GLFW.GLFW_KEY_Z&&input.length()<24)input+=(char)('A'+k-GLFW.GLFW_KEY_A);}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"WORD SCRAMBLE","Unscramble: "+scramble);c.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer,Text.literal(input+"_"),cx(),130,0xFFFFFFFF);c.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer,Text.literal("Round "+(round+1)+" / 8"),cx(),170,0xFFAAAAAA);}
    }

    public static final class TypingSpeed extends BaseGame {
        private String target,input="";private long start;
        @Override public String id(){return "typing_speed";}@Override public String title(){return "Typing Speed";}@Override public String category(){return "Arcade";}
        @Override public void start(){String a=WordBank.random(random),b=WordBank.random(random),c=WordBank.random(random),d=WordBank.random(random);target=(a+" "+b+" "+c+" "+d);input="";start=System.nanoTime();metrics.accuracyPercent(100);status="Type exactly.";}
        @Override public void keyPressed(int k,int s,int m){if(k==GLFW.GLFW_KEY_R){begin();return;}if(finished)return;if(k==GLFW.GLFW_KEY_BACKSPACE&&!input.isEmpty())input=input.substring(0,input.length()-1);else if(k==GLFW.GLFW_KEY_SPACE)input+=" ";else if(k>=GLFW.GLFW_KEY_A&&k<=GLFW.GLFW_KEY_Z)input+=(char)('A'+k-GLFW.GLFW_KEY_A);if(input.length()<=target.length()){int correct=0;for(int i=0;i<input.length();i++)if(input.charAt(i)==target.charAt(i))correct++;metrics.accuracyPercent(input.isEmpty()?100:correct*100.0/input.length());}if(input.equals(target)){long elapsed=System.nanoTime()-start;double ms=Math.max(1,elapsed/1_000_000L);double wpm=(target.length()/5.0)/(ms/60000.0);score=(int)Math.round(wpm*10);metrics.elapsedNanos(elapsed);finishWin(score);}}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"TYPING SPEED","Type the phrase exactly • WPM measured on completion • Accuracy: "+String.format("%.0f%%",metrics.accuracyPercent()));c.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer,Text.literal(target),cx(),90,0xFF55FFFF);c.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer,Text.literal(input+"_"),cx(),130,0xFFFFFFFF);}
    }

    public static final class SimonSays extends BaseGame {
        private final List<Integer> seq=new ArrayList<>();private int showIndex,inputIndex;private boolean showing;private long next;private int flash=-1;
        @Override public String id(){return "simon_says";}@Override public String title(){return "Simon Says";}@Override public String category(){return "Arcade";}
        @Override public void start(){seq.clear();seq.add(random.nextInt(4));beginShow();}
        private void beginShow(){showIndex=0;inputIndex=0;showing=true;next=System.nanoTime()+350_000_000L;flash=-1;status="Watch the sequence.";}
        @Override public void tick(){super.tick();if(showing&&System.nanoTime()>=next){if(showIndex<seq.size()){flash=seq.get(showIndex++);next=System.nanoTime()+450_000_000L;}else{flash=-1;showing=false;status="Repeat the sequence."}}}
        private void input(int v){if(showing||finished)return;if(v!=seq.get(inputIndex)){finish(score);return;}if(++inputIndex==seq.size()){if(seq.size()>=8){score+=1000;finishWin(score);return;}score+=seq.size()*100;seq.add(random.nextInt(4));beginShow();}}
        @Override public boolean mouseClicked(double mx,double my,int b){if(b!=0)return true;for(int i=0;i<4;i++){int x=cx()-105+(i%2)*110,y=85+(i/2)*70;if(inside(mx,my,x,y,100,60)){input(i);return true;}}return true;}
        @Override public void keyPressed(int k,int s,int m){if(k==GLFW.GLFW_KEY_R){begin();return;}if(k>=GLFW.GLFW_KEY_1&&k<=GLFW.GLFW_KEY_4)input(k-GLFW.GLFW_KEY_1);}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"SIMON SAYS",status);for(int i=0;i<4;i++){int x=cx()-105+(i%2)*110,y=85+(i/2)*70;c.fill(x,y,x+100,y+60,flash==i?0xFFFFFFFF:COLORS[i]);}}
    }
}