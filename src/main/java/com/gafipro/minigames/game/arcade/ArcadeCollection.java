package com.gafipro.minigames.game.arcade;

import com.gafipro.minigames.game.BaseGame;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ArcadeCollection {
    private ArcadeCollection() {}

    public static final class ColorRush extends BaseGame {
        private final String[] names={"RED","GREEN","BLUE","YELLOW"};
        private final int[] cols={0xFFE74C3C,0xFF2ECC71,0xFF3498DB,0xFFF1C40F};
        private int answer,target; private int round; private long nextChange;
        @Override public String id(){return "color_rush";} @Override public String title(){return "Color Rush";} @Override public String category(){return "Arcade";}
        @Override public void start(){target=random.nextInt(4);answer=random.nextInt(4);round=0;nextChange=System.currentTimeMillis()+2200;status="Click the color named above, not its text color.";}
        @Override public void tick(){super.tick();if(!finished && System.currentTimeMillis()>nextChange){target=random.nextInt(4);answer=random.nextInt(4);nextChange=System.currentTimeMillis()+1800;}}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"COLOR RUSH",status);c.drawCenteredTextWithShadow(net.minecraft.client.MinecraftClient.getInstance().textRenderer,net.minecraft.text.Text.literal(names[target]),cx(),84,cols[answer]);for(int i=0;i<4;i++){int x=cx()-120+(i%2)*125,y=105+(i/2)*65;c.fill(x,y,x+115,y+55,cols[i]);c.drawCenteredTextWithShadow(net.minecraft.client.MinecraftClient.getInstance().textRenderer,net.minecraft.text.Text.literal(names[i]),x+57,y+22,0xFFFFFFFF);}c.drawCenteredTextWithShadow(net.minecraft.client.MinecraftClient.getInstance().textRenderer,net.minecraft.text.Text.literal("Round "+round+" / 15"),cx(),248,0xFFFFFFFF);}
        @Override public boolean mouseClicked(double mx,double my,int b){if(b!=0||finished)return true;for(int i=0;i<4;i++){int x=cx()-120+(i%2)*125,y=105+(i/2)*65;if(inside(mx,my,x,y,115,55)){if(i!=target){finish(round*100);status="Wrong color!";}else{round++;score+=100;if(round>=15)finishWin(score);else{target=random.nextInt(4);answer=random.nextInt(4);nextChange=System.currentTimeMillis()+1400;}}return true;}}return true;}
    }

    public static final class FastClick extends BaseGame {
        private long end; private int clicks;
        @Override public String id(){return "fast_click";} @Override public String title(){return "Fast Click";} @Override public String category(){return "Arcade";}
        @Override public void start(){end=System.currentTimeMillis()+10000;clicks=0;status="Click the large target as many times as possible in 10 seconds.";}
        @Override public void tick(){super.tick();if(!finished&&System.currentTimeMillis()>=end){score=clicks;finishWin(score);}}
        @Override public void render(DrawContext c,int mx,int my,float d){long left=Math.max(0,end-System.currentTimeMillis());drawHeader(c,"FAST CLICK","Time: "+(left/1000.0));int x=cx()-85,y=90;c.fill(x,y,x+170,y+110,0xFF2ECC71);c.drawCenteredTextWithShadow(net.minecraft.client.MinecraftClient.getInstance().textRenderer,net.minecraft.text.Text.literal("CLICK!"),cx(),132,0xFFFFFFFF);c.drawCenteredTextWithShadow(net.minecraft.client.MinecraftClient.getInstance().textRenderer,net.minecraft.text.Text.literal("Clicks: "+clicks),cx(),225,0xFFFFFFFF);}
        @Override public boolean mouseClicked(double mx,double my,int b){if(b==0&&!finished&&inside(mx,my,cx()-85,90,170,110))clicks++;return true;}
    }

    public static final class SafeTile extends BaseGame {
        private int safe,reveal=-1,round=1; private long deadline;
        @Override public String id(){return "safe_tile";} @Override public String title(){return "Safe Tile";} @Override public String category(){return "Arcade";}
        @Override public void start(){newRound();}
        private void newRound(){safe=random.nextInt(16);reveal=-1;deadline=System.currentTimeMillis()+3500;status="Find the safe tile.";}
        @Override public void tick(){super.tick();if(!finished&&System.currentTimeMillis()>deadline){finish(score);status="Time's up!";}}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"SAFE TILE","Round "+round+" / 8");for(int i=0;i<16;i++){int x=cx()-120+(i%4)*62,y=80+(i/4)*48;boolean r=i==reveal;c.fill(x,y,x+54,y+40,r?(i==safe?0xFF2ECC71:0xFFE74C3C):0xFF3A424A);c.drawCenteredTextWithShadow(net.minecraft.client.MinecraftClient.getInstance().textRenderer,net.minecraft.text.Text.literal(r?(i==safe?"SAFE":"TRAP"):"?"),x+27,y+15,0xFFFFFFFF);} }
        @Override public boolean mouseClicked(double mx,double my,int b){if(b!=0||finished)return true;for(int i=0;i<16;i++){int x=cx()-120+(i%4)*62,y=80+(i/4)*48;if(inside(mx,my,x,y,54,40)){reveal=i;if(i==safe){score+=100*round;round++;if(round>8)finishWin(score);else newRound();}else{finish(score);status="You hit a trap.";}return true;}}return true;}
    }

    public static final class ReactionTest extends BaseGame {
        private long signalAt;private boolean ready;private long started;private long reaction=-1;
        @Override public String id(){return "reaction_test";} @Override public String title(){return "Reaction Test";} @Override public String category(){return "Arcade";}
        @Override public void start(){ready=false;signalAt=System.currentTimeMillis()+1200+random.nextInt(2600);status="Wait... do not click.";}
        @Override public void tick(){super.tick();if(!finished&&!ready&&System.currentTimeMillis()>=signalAt){ready=true;started=System.nanoTime();status="CLICK NOW!";}}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"REACTION TEST",status);c.fill(cx()-100,95,cx()+100,195,ready?0xFF2ECC71:0xFF8E44AD);c.drawCenteredTextWithShadow(net.minecraft.client.MinecraftClient.getInstance().textRenderer,net.minecraft.text.Text.literal(ready?"CLICK":"WAIT"),cx(),139,0xFFFFFFFF);if(reaction>=0)c.drawCenteredTextWithShadow(net.minecraft.client.MinecraftClient.getInstance().textRenderer,net.minecraft.text.Text.literal(reaction+" ms"),cx(),215,0xFFFFFFFF);}
        @Override public boolean mouseClicked(double mx,double my,int b){if(b!=0||finished)return true;if(!ready){finish(1);status="False start.";}else if(inside(mx,my,cx()-100,95,200,100)){reaction=(System.nanoTime()-started)/1_000_000;finishWin(Math.max(1,1000-(int)reaction));status="Reaction: "+reaction+" ms";}return true;}
    }

    public static final class WhackAMole extends BaseGame {
        private int mole;private long next;private int hits,misses;
        @Override public String id(){return "whack_a_mole";} @Override public String title(){return "Whack-A-Mole";} @Override public String category(){return "Arcade";}
        @Override public void start(){mole=random.nextInt(9);next=System.currentTimeMillis()+900;hits=misses=0;}
        @Override public void tick(){super.tick();if(!finished&&System.currentTimeMillis()>=next){mole=random.nextInt(9);next=System.currentTimeMillis()+Math.max(320,900-hits*15);misses++;if(misses>=20)finishWin(hits*100);}}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"WHACK-A-MOLE","Hit the mole. 20 misses ends the game.");for(int i=0;i<9;i++){int x=cx()-135+(i%3)*90,y=82+(i/3)*60;c.fill(x,y,x+78,y+50,0xFF3A424A);if(i==mole)c.fill(x+10,y+8,x+68,y+42,0xFFE67E22);}c.drawCenteredTextWithShadow(net.minecraft.client.MinecraftClient.getInstance().textRenderer,net.minecraft.text.Text.literal("Hits: "+hits+"  Misses: "+misses),cx(),275,0xFFFFFFFF);}
        @Override public boolean mouseClicked(double mx,double my,int b){if(b!=0||finished)return true;for(int i=0;i<9;i++){int x=cx()-135+(i%3)*90,y=82+(i/3)*60;if(inside(mx,my,x,y,78,50)&&i==mole){hits++;score=hits*100;mole=random.nextInt(9);next=System.currentTimeMillis()+Math.max(300,800-hits*12);return true;}}return true;}
    }

    public static final class PatternCopy extends BaseGame {
        private final List<Integer> seq=new ArrayList<>(); private int shown,userIndex; private long showUntil; private boolean accepting;
        @Override public String id(){return "pattern_copy";} @Override public String title(){return "Pattern Copy";} @Override public String category(){return "Arcade";}
        @Override public void start(){seq.clear();round();}
        private void round(){seq.add(random.nextInt(9));shown=0;userIndex=0;accepting=false;showUntil=System.currentTimeMillis()+650;}
        @Override public void tick(){super.tick();if(!finished&&!accepting&&System.currentTimeMillis()>=showUntil){shown=seq.size();accepting=true;status="Repeat the pattern.";}}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"PATTERN COPY","Round "+seq.size()+"  "+(accepting?"Your turn":"Watch"));for(int i=0;i<9;i++){int x=cx()-135+(i%3)*90,y=80+(i/3)*55;boolean hi=!accepting&&shown==seq.size()&&i==seq.get(seq.size()-1);c.fill(x,y,x+78,y+45,hi?0xFF2ECC71:0xFF3A424A);}}
        @Override public boolean mouseClicked(double mx,double my,int b){if(b!=0||finished||!accepting)return true;for(int i=0;i<9;i++){int x=cx()-135+(i%3)*90,y=80+(i/3)*55;if(inside(mx,my,x,y,78,45)){if(i!=seq.get(userIndex)){finish(score);status="Pattern failed.";}else{userIndex++;if(userIndex>=seq.size()){score+=seq.size()*100;if(seq.size()>=8)finishWin(score);else{accepting=false;round();}}}return true;}}return true;}
    }

    public static final class TargetPractice extends BaseGame {
        private double tx,ty,dx,dy;private long end;private int hits;
        @Override public String id(){return "target_practice";} @Override public String title(){return "Target Practice";} @Override public String category(){return "Arcade";}
        @Override public void start(){tx=cx();ty=120;dx=2.6;dy=2.1;end=System.currentTimeMillis()+15000;hits=0;}
        @Override public void tick(){super.tick();if(!finished){tx+=dx;ty+=dy;if(tx<cx()-150||tx>cx()+150)dx=-dx;if(ty<85||ty>235)dy=-dy;if(System.currentTimeMillis()>=end)finishWin(hits*100);}}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"TARGET PRACTICE","Hit the moving target. Time: "+Math.max(0,(end-System.currentTimeMillis())/1000.0));c.fill((int)tx-15,(int)ty-15,(int)tx+15,(int)ty+15,0xFFE74C3C);c.fill((int)tx-5,(int)ty-5,(int)tx+5,(int)ty+5,0xFFFFFFFF);}
        @Override public boolean mouseClicked(double mx,double my,int b){if(b==0&&!finished&&Math.hypot(mx-tx,my-ty)<=24){hits++;score=hits*100;dx*=1.025;dy*=1.025;}return true;}
    }

    public static final class MathRush extends BaseGame {
        private int a,b,op,answer;private int round;private long end;
        @Override public String id(){return "math_rush";} @Override public String title(){return "Math Rush";} @Override public String category(){return "Arcade";}
        @Override public void start(){round=0;next();}
        private void next(){a=2+random.nextInt(18);b=2+random.nextInt(18);op=random.nextInt(3);answer=op==0?a+b:op==1?a-b:a*b;end=System.currentTimeMillis()+4500;status="Choose the correct answer.";}
        @Override public void tick(){super.tick();if(!finished&&System.currentTimeMillis()>end)finish(score);}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"MATH RUSH","Question "+(round+1)+" / 10");String q=a+(op==0?" + ":op==1?" - ":" × ")+b+" = ?";c.drawCenteredTextWithShadow(net.minecraft.client.MinecraftClient.getInstance().textRenderer,net.minecraft.text.Text.literal(q),cx(),88,0xFFFFFFFF);int[] opts={answer,answer+random.nextInt(9)+1,answer-random.nextInt(8)-1,answer+random.nextInt(4)+10};List<Integer> list=new ArrayList<>();Collections.addAll(list,opts);Collections.shuffle(list);for(int i=0;i<4;i++){int x=cx()-140+(i%2)*145,y=115+(i/2)*65;c.fill(x,y,x+130,y+55,0xFF3A424A);c.drawCenteredTextWithShadow(net.minecraft.client.MinecraftClient.getInstance().textRenderer,net.minecraft.text.Text.literal(""+list.get(i)),x+65,y+20,0xFFFFFFFF);}}
        @Override public boolean mouseClicked(double mx,double my,int btn){if(btn!=0||finished)return true;int[] opts={answer,answer+random.nextInt(9)+1,answer-random.nextInt(8)-1,answer+random.nextInt(4)+10};for(int i=0;i<4;i++){int x=cx()-140+(i%2)*145,y=115+(i/2)*65;if(inside(mx,my,x,y,130,55)){if(i==0){round++;score+=100;if(round>=10)finishWin(score);else next();}else finish(score);return true;}}return true;}
        @Override public void keyPressed(int key,int scan,int mods){if(key>=GLFW.GLFW_KEY_0&&key<=GLFW.GLFW_KEY_9&&!finished){int v=key-GLFW.GLFW_KEY_0;if(v==answer){round++;score+=100;if(round>=10)finishWin(score);else next();}else finish(score);}}
    }

    public static final class WordScramble extends BaseGame {
        private final String[] words={"MINECRAFT","REDSTONE","PICKAXE","CREEPER","DIAMOND","ENDERDRAGON","VILLAGER","NETHERITE","CRAFTING","ADVENTURE"};
        private String word,scrambled,input="";private int round;private long end;
        @Override public String id(){return "word_scramble";} @Override public String title(){return "Word Scramble";} @Override public String category(){return "Arcade";}
        @Override public void start(){round=0;next();}
        private void next(){word=words[random.nextInt(words.length)];List<Character> cs=new ArrayList<>();for(char ch:word.toCharArray())cs.add(ch);Collections.shuffle(cs);StringBuilder s=new StringBuilder();for(char ch:cs)s.append(ch);scrambled=s.toString();input="";end=System.currentTimeMillis()+8000;}
        @Override public void tick(){super.tick();if(!finished&&System.currentTimeMillis()>end)finish(score);}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"WORD SCRAMBLE","Type the answer and press Enter");c.drawCenteredTextWithShadow(net.minecraft.client.MinecraftClient.getInstance().textRenderer,net.minecraft.text.Text.literal(scrambled),cx(),95,0xFF55FFFF);c.drawCenteredTextWithShadow(net.minecraft.client.MinecraftClient.getInstance().textRenderer,net.minecraft.text.Text.literal(input+"_"),cx(),135,0xFFFFFFFF);c.drawCenteredTextWithShadow(net.minecraft.client.MinecraftClient.getInstance().textRenderer,net.minecraft.text.Text.literal("Round "+(round+1)+" / 8"),cx(),180,0xFFAAAAAA);}
        @Override public boolean mouseClicked(double x,double y,int b){return true;}
        @Override public void keyPressed(int key,int scan,int mods){if(finished)return;if(key==GLFW.GLFW_KEY_BACKSPACE&&!input.isEmpty())input=input.substring(0,input.length()-1);else if(key==GLFW.GLFW_KEY_ENTER){if(input.equalsIgnoreCase(word)){round++;score+=100;if(round>=8)finishWin(score);else next();}else status="Incorrect.";}else if(key>=GLFW.GLFW_KEY_A&&key<=GLFW.GLFW_KEY_Z&&input.length()<24)input+=(char)('A'+key-GLFW.GLFW_KEY_A);}
    }

    public static final class TypingSpeed extends BaseGame {
        private final String[] prompts={"the quick brown fox","mining diamonds is satisfying","redstone makes everything better","welcome to gafi minigames"};
        private String prompt,input="";private long end;
        @Override public String id(){return "typing_speed";} @Override public String title(){return "Typing Speed";} @Override public String category(){return "Arcade";}
        @Override public void start(){prompt=prompts[random.nextInt(prompts.length)];end=System.currentTimeMillis()+15000;}
        @Override public void tick(){super.tick();if(!finished&&System.currentTimeMillis()>end)finish(score);}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"TYPING SPEED","Type the sentence exactly, then press Enter");c.drawCenteredTextWithShadow(net.minecraft.client.MinecraftClient.getInstance().textRenderer,net.minecraft.text.Text.literal(prompt),cx(),95,0xFF55FFFF);c.drawCenteredTextWithShadow(net.minecraft.client.MinecraftClient.getInstance().textRenderer,net.minecraft.text.Text.literal(input),cx(),125,0xFFFFFFFF);}
        @Override public boolean mouseClicked(double x,double y,int b){return true;}
        @Override public void keyPressed(int key,int scan,int mods){if(finished)return;if(key==GLFW.GLFW_KEY_BACKSPACE&&!input.isEmpty())input=input.substring(0,input.length()-1);else if(key==GLFW.GLFW_KEY_ENTER){if(input.equals(prompt)){long ms=15000-Math.max(0,end-System.currentTimeMillis());score=1000-(int)ms;finishWin(Math.max(100,score));}else status="Mismatch — try again.";}else{String ch=keyToChar(key);if(ch!=null)input+=ch;}}
        private String keyToChar(int k){if(k==GLFW.GLFW_KEY_SPACE)return " ";if(k>=GLFW.GLFW_KEY_A&&k<=GLFW.GLFW_KEY_Z)return String.valueOf((char)('a'+k-GLFW.GLFW_KEY_A));return null;}
    }

    public static final class SimonSays extends BaseGame {
        private final List<Integer> seq=new ArrayList<>();private int index;private long flashUntil;private boolean showing;private int flash=-1;
        @Override public String id(){return "simon_says";} @Override public String title(){return "Simon Says";} @Override public String category(){return "Arcade";}
        @Override public void start(){seq.clear();nextRound();}
        private void nextRound(){seq.add(random.nextInt(4));index=0;showing=true;showAt(0);}
        private void showAt(int i){if(i<seq.size()){flash=seq.get(i);flashUntil=System.currentTimeMillis()+420;}else{showing=false;flash=-1;status="Repeat the colors.";}}
        @Override public void tick(){super.tick();if(!finished&&showing&&System.currentTimeMillis()>flashUntil){flash=-1;if(System.currentTimeMillis()%500<30)showAt((int)Math.min(seq.size(),(ticks/12)));else if(ticks%12==0)showAt((int)Math.min(seq.size(),(ticks/12)));}}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"SIMON SAYS","Round "+seq.size());for(int i=0;i<4;i++){int x=cx()-115+(i%2)*120,y=90+(i/2)*70;int base=i==0?0xFFE74C3C:i==1?0xFF2ECC71:i==2?0xFF3498DB:0xFFF1C40F;c.fill(x,y,x+105,y+55,(i==flash)?0xFFFFFFFF:base);}}
        @Override public boolean mouseClicked(double mx,double my,int b){if(b!=0||finished||showing)return true;for(int i=0;i<4;i++){int x=cx()-115+(i%2)*120,y=90+(i/2)*70;if(inside(mx,my,x,y,105,55)){if(i!=seq.get(index)){finish(score);status="Wrong sequence.";}else{index++;if(index>=seq.size()){score+=seq.size()*100;if(seq.size()>=9)finishWin(score);else nextRound();}}return true;}}return true;}
    }
}
