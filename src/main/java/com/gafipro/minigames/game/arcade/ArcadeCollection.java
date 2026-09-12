package com.gafipro.minigames.game.arcade;

import com.gafipro.minigames.game.BaseGame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Focused arcade implementations. Timing uses monotonic time and gameplay state is independent of rendering. */
public final class ArcadeCollection {
    private ArcadeCollection() {}
    private static final String[] WORDS={"MINECRAFT","REDSTONE","CREEPER","DIAMOND","PICKAXE","VILLAGER","NETHER","ENDER","PORTAL","ZOMBIE","SKELETON","DRAGON","FOREST","DESERT","OCEAN","VILLAGE","FURNACE","CRAFTING","AXOLOTL","GOLEM","BEACON","ELYTRA","SHIELD","TRIDENT","MUSHROOM","WATER","LAVA","CAVE","SPAWNER","ENCHANTMENT"};

    public static final class ReactionTest extends BaseGame {
        private long readyAt; private long startAt; private boolean armed; private int reactionMs;
        @Override public String id(){return "reaction_test";} @Override public String title(){return "Reaction Test";} @Override public String category(){return "Arcade";}
        @Override public void start(){armed=false;reactionMs=-1;readyAt=System.nanoTime()+1_000_000_000L+random.nextInt(2_500_000_000);status="Wait for the target to turn green...";}
        @Override public void tick(){super.tick();if(!finished&&!armed&&System.nanoTime()>=readyAt){armed=true;startAt=System.nanoTime();status="CLICK NOW!";}}
        @Override public boolean mouseClicked(double mx,double my,int b){if(b!=0||finished)return true;if(!armed){status="Too early — false start.";finish(0);return true;}reactionMs=(int)((System.nanoTime()-startAt)/1_000_000L);score=Math.max(1,2_000-reactionMs*4);status="Reaction: "+reactionMs+" ms";finishWin(score);return true;}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"REACTION TEST",status);int x=cx(),y=125;c.fill(x-70,y-45,x+70,y+45,armed?0xFF44CC77:0xFFAA4444);c.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer,Text.literal(armed?"CLICK":"WAIT"),x,y-5,0xFFFFFFFF);}
        @Override public void keyPressed(int k,int s,int m){if(k==GLFW.GLFW_KEY_R)begin();}
    }

    public static final class WhackAMole extends BaseGame {
        private int mole=-1,hits; private long nextMole,end;
        @Override public String id(){return "whack_a_mole";} @Override public String title(){return "Whack-A-Mole";} @Override public String category(){return "Arcade";}
        @Override public void start(){hits=0;end=System.nanoTime()+20_000_000_000L;spawn();}
        private void spawn(){mole=random.nextInt(12);nextMole=System.nanoTime()+500_000_000L-random.nextInt(180_000_000);}
        @Override public void tick(){super.tick();long now=System.nanoTime();if(!finished&&now>=end)finishWin(hits*100);else if(!finished&&now>=nextMole)spawn();}
        @Override public boolean mouseClicked(double mx,double my,int b){if(b!=0||finished)return true;int s=55,ox=cx()-110,oy=85,x=(int)((mx-ox)/s),y=(int)((my-oy)/s);if(x>=0&&y>=0&&x<4&&y<3){if(y*4+x==mole){hits++;score=hits*100;spawn();}else score=Math.max(0,score-20);}return true;}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"WHACK-A-MOLE","Hits: "+hits+" • Time: "+Math.max(0,(20000-(System.nanoTime()-(end-20_000_000_000L))/1_000_000)/1000));int s=55,ox=cx()-110,oy=85;for(int i=0;i<12;i++){int x=ox+(i%4)*s,y=oy+(i/4)*s;c.fill(x+2,y+2,x+s-2,y+s-2,0xFF333B43);if(i==mole)c.fill(x+12,y+12,x+s-12,y+s-12,0xFFE4A23B);}}
        @Override public void keyPressed(int k,int s,int m){if(k==GLFW.GLFW_KEY_R)begin();}
    }

    public static final class ColorRush extends BaseGame {
        private static final String[] N={"RED","BLUE","GREEN","YELLOW"}; private static final int[] C={0xFFE74C3C,0xFF3498DB,0xFF2ECC71,0xFFF1C40F};
        private int word,ink,round,streak,mistakes; private long next;
        @Override public String id(){return "color_rush";} @Override public String title(){return "Color Rush";} @Override public String category(){return "Arcade";}
        @Override public void start(){round=0;streak=0;mistakes=0;nextRound();}
        private void nextRound(){word=random.nextInt(4);do ink=random.nextInt(4);while(ink==word&&random.nextBoolean());next=System.nanoTime()+4_000_000_000L;status="Click the INK color • Streak: "+streak;}
        @Override public void tick(){super.tick();if(!finished&&System.nanoTime()>next){mistakes++;streak=0;if(round++>=11)finish(score);else nextRound();}}
        @Override public boolean mouseClicked(double mx,double my,int b){if(b!=0||finished)return true;for(int i=0;i<4;i++){int x=cx()-150+i*100;if(mx>=x&&mx<x+85&&my>=135&&my<190){if(i==ink){streak++;score+=100+streak*20;round++;if(round>=12)finishWin(score);else nextRound();}else{mistakes++;streak=0;score=Math.max(0,score-50);round++;if(round>=12)finish(score);else nextRound();}return true;}}return true;}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"COLOR RUSH","Choose the ink color • Round "+(round+1)+" / 12 • Mistakes: "+mistakes);c.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer,Text.literal(N[word]),cx(),90,C[ink]);for(int i=0;i<4;i++){int x=cx()-150+i*100;c.fill(x,135,x+85,190,C[i]);c.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer,Text.literal(N[i]),x+42,154,0xFFFFFFFF);}}
        @Override public void keyPressed(int k,int s,int m){if(k==GLFW.GLFW_KEY_R)begin();}
    }

    public static final class PatternCopy extends BaseGame {
        private final boolean[][] target=new boolean[4][4],chosen=new boolean[4][4]; private boolean showing; private long hideAt; private int round,needed;
        @Override public String id(){return "pattern_copy";} @Override public String title(){return "Pattern Copy";} @Override public String category(){return "Arcade";}
        @Override public void start(){round=0;nextPattern();}
        private void nextPattern(){for(int x=0;x<4;x++)for(int y=0;y<4;y++){target[x][y]=false;chosen[x][y]=false;}needed=4+random.nextInt(Math.min(7,round+4));List<Integer> cells=new ArrayList<>();for(int i=0;i<16;i++)cells.add(i);Collections.shuffle(cells,random);for(int i=0;i<needed;i++){int q=cells.get(i);target[q%4][q/4]=true;}showing=true;hideAt=System.nanoTime()+1_500_000_000L;status="Memorize the pattern.";}
        @Override public void tick(){super.tick();if(showing&&System.nanoTime()>=hideAt){showing=false;status="Recreate it, then press Enter.";}}
        private int selected(){int n=0;for(boolean[]r:chosen)for(boolean v:r)if(v)n++;return n;}
        @Override public boolean mouseClicked(double mx,double my,int b){if(b!=0||finished||showing)return true;int s=50,ox=cx()-100,oy=90,x=(int)((mx-ox)/s),y=(int)((my-oy)/s);if(x>=0&&y>=0&&x<4&&y<4){chosen[x][y]=!chosen[x][y];}return true;}
        @Override public void keyPressed(int k,int s,int m){if(k==GLFW.GLFW_KEY_R)begin();else if(k==GLFW.GLFW_KEY_ENTER&&!finished&&!showing){boolean ok=true;for(int x=0;x<4;x++)for(int y=0;y<4;y++)if(target[x][y]!=chosen[x][y])ok=false;if(ok){round++;score+=needed*100;if(round>=6)finishWin(score);else nextPattern();}else{status="Pattern incorrect.";finish(score);}}}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"PATTERN COPY",showing?"Memorize the pattern.":"Recreate it, then press Enter.");int s=50,ox=cx()-100,oy=90;for(int x=0;x<4;x++)for(int y=0;y<4;y++)c.fill(ox+x*s+2,oy+y*s+2,ox+x*s+s-2,oy+y*s+s-2,(showing&&target[x][y])||(!showing&&chosen[x][y])?0xFF55AAFF:0xFF3B424A);}
    }

    public static final class FastClick extends BaseGame {
        private int clicks; private long end; private int tx,ty;
        @Override public String id(){return "fast_click";} @Override public String title(){return "Fast Click";} @Override public String category(){return "Arcade";}
        @Override public void start(){clicks=0;end=System.nanoTime()+15_000_000_000L;move();}
        private void move(){tx=cx()-120+random.nextInt(241);ty=95+random.nextInt(130);}
        @Override public void tick(){super.tick();if(!finished&&System.nanoTime()>=end){status="20 clicks completed in time.";finishWin(clicks*100);}}
        @Override public boolean mouseClicked(double mx,double my,int b){if(b!=0||finished)return true;if(Math.hypot(mx-tx,my-ty)<=22){clicks++;score=clicks*100;if(clicks>=20)finishWin(score);else move();}return true;}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"FAST CLICK","Click the target 20 times • Clicks: "+clicks);c.fill(tx-22,ty-22,tx+22,ty+22,0xFFE74C3C);c.fill(tx-4,ty-4,tx+4,ty+4,0xFFFFFFFF);}
        @Override public void keyPressed(int k,int s,int m){if(k==GLFW.GLFW_KEY_R)begin();}
    }

    public static final class SafeTile extends BaseGame {
        private int safe; private boolean reveal; private long hideAt; private int round;
        @Override public String id(){return "safe_tile";} @Override public String title(){return "Safe Tile";} @Override public String category(){return "Arcade";}
        @Override public void start(){round=0;newRound();}
        private void newRound(){safe=random.nextInt(25);reveal=true;hideAt=System.nanoTime()+1_200_000_000L;status="Memorize the safe tile.";}
        @Override public void tick(){super.tick();if(reveal&&System.nanoTime()>=hideAt){reveal=false;status="Find the safe tile.";}}
        @Override public boolean mouseClicked(double mx,double my,int b){if(b!=0||finished||reveal)return true;int s=45,ox=cx()-112,oy=85,x=(int)((mx-ox)/s),y=(int)((my-oy)/s);if(x>=0&&y>=0&&x<5&&y<5){int i=y*5+x;if(i==safe){round++;score+=100+round*25;if(round>=10)finishWin(score);else newRound();}else finish(score);}return true;}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"SAFE TILE",status);int s=45,ox=cx()-112,oy=85;for(int i=0;i<25;i++){int x=ox+(i%5)*s,y=oy+(i/5)*s;c.fill(x+2,y+2,x+s-2,y+s-2,reveal&&i==safe?0xFF55CC88:0xFF3B424A);}}
        @Override public void keyPressed(int k,int s,int m){if(k==GLFW.GLFW_KEY_R)begin();}
    }

    public static final class TargetPractice extends BaseGame {
        private double tx,ty,dx,dy; private int hits; private long end;
        @Override public String id(){return "target_practice";} @Override public String title(){return "Target Practice";} @Override public String category(){return "Arcade";}
        @Override public void start(){tx=cx();ty=130;dx=2.4;dy=1.8;end=System.nanoTime()+15_000_000_000L;hits=0;}
        @Override public void tick(){super.tick();if(finished)return;tx+=dx;ty+=dy;if(tx<cx()-150||tx>cx()+150)dx=-dx;if(ty<90||ty>235)dy=-dy;if(System.nanoTime()>=end)finishWin(hits*100);}
        @Override public boolean mouseClicked(double mx,double my,int b){if(b==0&&!finished&&Math.hypot(mx-tx,my-ty)<=25){hits++;score=hits*100;dx*=1.02;dy*=1.02;}return true;}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"TARGET PRACTICE","Hits: "+hits+" • Keep hitting the moving target");c.fill((int)tx-18,(int)ty-18,(int)tx+18,(int)ty+18,0xFFE74C3C);c.fill((int)tx-5,(int)ty-5,(int)tx+5,(int)ty+5,0xFFFFFFFF);}
        @Override public void keyPressed(int k,int s,int m){if(k==GLFW.GLFW_KEY_R)begin();}
    }

    public static final class MathRush extends BaseGame {
        private int a,b,op,answer,round; private final int[] options=new int[4]; private long deadline;
        @Override public String id(){return "math_rush";} @Override public String title(){return "Math Rush";} @Override public String category(){return "Arcade";}
        @Override public void start(){round=0;next();}
        private void next(){a=2+random.nextInt(18);b=2+random.nextInt(18);op=random.nextInt(3);answer=op==0?a+b:op==1?a-b:a*b;options[0]=answer;do options[1]=answer+random.nextInt(15)-7;while(options[1]==answer);do options[2]=answer+random.nextInt(21)-10;while(options[2]==answer||options[2]==options[1]);do options[3]=answer+random.nextInt(25)-12;while(options[3]==answer||options[3]==options[1]||options[3]==options[2]);for(int i=3;i>0;i--){int j=random.nextInt(i+1);int t=options[i];options[i]=options[j];options[j]=t;}deadline=System.nanoTime()+4_500_000_000L;status="Choose the correct answer.";}
        private void pick(int i){if(i<0||i>3||finished)return;if(options[i]!=answer){status="Incorrect answer.";finish(score);return;}round++;score+=100;if(round>=10)finishWin(score);else next();}
        @Override public void tick(){super.tick();if(!finished&&System.nanoTime()>=deadline)finish(score);}
        @Override public boolean mouseClicked(double mx,double my,int btn){if(btn!=0)return true;for(int i=0;i<4;i++){int x=cx()-140+(i%2)*145,y=125+(i/2)*65;if(inside(mx,my,x,y,130,55)){pick(i);return true;}}return true;}
        @Override public void keyPressed(int k,int s,int m){if(k==GLFW.GLFW_KEY_R){begin();return;}for(int i=0;i<4;i++)if(k==GLFW.GLFW_KEY_1+i)pick(i);}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"MATH RUSH","Question "+(round+1)+" / 10 • Choose the correct answer");String q=a+(op==0?" + ":op==1?" - ":" × ")+b+" = ?";c.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer,Text.literal(q),cx(),88,0xFFFFFFFF);for(int i=0;i<4;i++){int x=cx()-140+(i%2)*145,y=125+(i/2)*65;c.fill(x,y,x+130,y+55,0xFF3A424A);c.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer,Text.literal(String.valueOf(options[i])),x+65,y+20,0xFFFFFFFF);}}
    }

    public static final class WordScramble extends BaseGame {
        private String word,scramble,input=""; private int round; private long deadline;
        @Override public String id(){return "word_scramble";} @Override public String title(){return "Word Scramble";} @Override public String category(){return "Arcade";}
        @Override public void start(){round=0;next();}
        private void next(){word=WORDS[random.nextInt(WORDS.length)];List<Character> chars=new ArrayList<>();for(char ch:word.toCharArray())chars.add(ch);do{Collections.shuffle(chars,random);StringBuilder b=new StringBuilder();for(char ch:chars)b.append(ch);scramble=b.toString();}while(scramble.equals(word)&&word.length()>1);input="";deadline=System.nanoTime()+8_000_000_000L;status="Type the answer and press Enter.";}
        @Override public void tick(){super.tick();if(!finished&&System.nanoTime()>=deadline)finish(score);}
        @Override public void keyPressed(int k,int s,int m){if(k==GLFW.GLFW_KEY_R){begin();return;}if(finished)return;if(k==GLFW.GLFW_KEY_BACKSPACE&&!input.isEmpty())input=input.substring(0,input.length()-1);else if(k==GLFW.GLFW_KEY_ENTER){if(input.equalsIgnoreCase(word)){round++;score+=100;if(round>=8)finishWin(score);else next();}else finish(score);}else if(k>=GLFW.GLFW_KEY_A&&k<=GLFW.GLFW_KEY_Z&&input.length()<24)input+=(char)('A'+k-GLFW.GLFW_KEY_A);}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"WORD SCRAMBLE","Unscramble: "+scramble);c.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer,Text.literal(input+"_"),cx(),135,0xFFFFFFFF);c.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer,Text.literal("Round "+(round+1)+" / 8"),cx(),175,0xFFAAAAAA);}
    }

    public static final class TypingSpeed extends BaseGame {
        private static final String[] PHRASES={"THE QUICK BROWN FOX JUMPS OVER THE LAZY DOG","MINECRAFT IS BETTER WITH REDSTONE","BUILD THINK TEST REPEAT","DIAMONDS ARE A MINERS BEST FRIEND","PRACTICE MAKES FAST AND ACCURATE"};
        private String target,input="";private long start;
        @Override public String id(){return "typing_speed";} @Override public String title(){return "Typing Speed";} @Override public String category(){return "Arcade";}
        @Override public void start(){target=PHRASES[random.nextInt(PHRASES.length)];input="";start=System.nanoTime();status="Type the sentence exactly.";}
        @Override public void keyPressed(int k,int s,int m){if(k==GLFW.GLFW_KEY_R){begin();return;}if(finished)return;if(k==GLFW.GLFW_KEY_BACKSPACE&&!input.isEmpty())input=input.substring(0,input.length()-1);else if(k==GLFW.GLFW_KEY_SPACE)input+=" ";else if(k>=GLFW.GLFW_KEY_A&&k<=GLFW.GLFW_KEY_Z)input+=(char)('A'+k-GLFW.GLFW_KEY_A);if(!target.startsWith(input)){metrics.accuracyPercent(Math.max(0,100-(input.length()>target.length()?0:countDifferences()))); }if(input.equals(target)){long ms=Math.max(1,(System.nanoTime()-start)/1_000_000L);int correct=target.length();double minutes=ms/60000.0;double wpm=(correct/5.0)/minutes;score=(int)Math.round(wpm*10);metrics.accuracyPercent(100);finishWin(score);}}
        private int countDifferences(){int n=Math.min(input.length(),target.length());int d=0;for(int i=0;i<n;i++)if(input.charAt(i)!=target.charAt(i))d++;return d+(input.length()>target.length()?input.length()-target.length():0);}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"TYPING SPEED","Type exactly • WPM measured on completion");c.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer,Text.literal(target),cx(),90,0xFF55FFFF);c.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer,Text.literal(input+"_"),cx(),130,0xFFFFFFFF);}
    }

    public static final class SimonSays extends BaseGame {
        private final List<Integer> seq=new ArrayList<>();private int phaseIndex,inputIndex,round;private boolean showing;private long nextStep;private int flash=-1;
        @Override public String id(){return "simon_says";} @Override public String title(){return "Simon Says";} @Override public String category(){return "Arcade";}
        @Override public void start(){seq.clear();round=0;addStep();}
        private void addStep(){seq.add(random.nextInt(4));phaseIndex=0;inputIndex=0;showing=true;flash=-1;nextStep=System.nanoTime()+350_000_000L;status="Watch the sequence.";}
        @Override public void tick(){super.tick();if(!showing||finished)return;long now=System.nanoTime();if(now>=nextStep){if(phaseIndex<seq.size()){flash=seq.get(phaseIndex++);nextStep=now+450_000_000L;}else{flash=-1;showing=false;status="Repeat the sequence.";}}}
        private void input(int v){if(showing||finished)return;if(v!=seq.get(inputIndex)){status="Wrong sequence.";finish(score);return;}inputIndex++;if(inputIndex==seq.size()){round++;score+=round*100;if(round>=8)finishWin(score);else addStep();}}
        @Override public boolean mouseClicked(double mx,double my,int b){if(b!=0)return true;for(int i=0;i<4;i++){int x=cx()-105+(i%2)*110,y=85+(i/2)*70;if(inside(mx,my,x,y,100,60)){input(i);return true;}}return true;}
        @Override public void keyPressed(int k,int s,int m){if(k==GLFW.GLFW_KEY_R){begin();return;}if(k>=GLFW.GLFW_KEY_1&&k<=GLFW.GLFW_KEY_4)input(k-GLFW.GLFW_KEY_1);}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"SIMON SAYS",showing?"Watch the sequence.":"Repeat the sequence.");int[]colors={0xFFE74C3C,0xFF3498DB,0xFF2ECC71,0xFFF1C40F};for(int i=0;i<4;i++){int x=cx()-105+(i%2)*110,y=85+(i/2)*70;c.fill(x,y,x+100,y+60,(flash==i)?0xFFFFFFFF:colors[i]);}}
    }
}
