package com.gafipro.minigames.game.puzzle;

import com.gafipro.minigames.core.WordBank;
import com.gafipro.minigames.game.BaseGame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Word Search using the shared embedded dictionary and eight legal directions. */
public final class WordSearchGame extends BaseGame {
    private static final int SIZE=12, WORD_COUNT=8;
    private final char[][] grid=new char[SIZE][SIZE];
    private final List<String> words=new ArrayList<>(),found=new ArrayList<>();
    private int sx=-1,sy=-1;

    @Override public String id(){return "word_search";}
    @Override public String title(){return "Word Search";}
    @Override public String category(){return "Puzzle";}

    @Override public void start(){
        for(int x=0;x<SIZE;x++)for(int y=0;y<SIZE;y++)grid[x][y]=' ';
        words.clear();found.clear();sx=sy=-1;
        List<String> pool=new ArrayList<>(WordBank.all());Collections.shuffle(pool,random);
        for(String word:pool){if(word.length()>SIZE)continue;if(place(word)){words.add(word);if(words.size()==WORD_COUNT)break;}}
        for(int x=0;x<SIZE;x++)for(int y=0;y<SIZE;y++)if(grid[x][y]==' ')grid[x][y]=(char)('A'+random.nextInt(26));
        status="Select the first and last letter of a hidden word • Found 0/"+words.size();
    }
    private boolean place(String word){
        int[][] dirs={{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}};
        for(int tries=0;tries<100;tries++){int dx=dirs[random.nextInt(dirs.length)][0],dy=dirs[random.nextInt(dirs.length)][1],x=random.nextInt(SIZE),y=random.nextInt(SIZE),ex=x+dx*(word.length()-1),ey=y+dy*(word.length()-1);if(ex<0||ey<0||ex>=SIZE||ey>=SIZE)continue;boolean ok=true;for(int i=0;i<word.length();i++){char v=grid[x+dx*i][y+dy*i];if(v!=' '&&v!=word.charAt(i)){ok=false;break;}}if(!ok)continue;for(int i=0;i<word.length();i++)grid[x+dx*i][y+dy*i]=word.charAt(i);return true;}return false;
    }
    private String line(int x1,int y1,int x2,int y2){int dx=Integer.compare(x2,x1),dy=Integer.compare(y2,y1),len=Math.max(Math.abs(x2-x1),Math.abs(y2-y1))+1;if(x1+dx*(len-1)!=x2||y1+dy*(len-1)!=y2)return "";StringBuilder s=new StringBuilder();for(int i=0;i<len;i++)s.append(grid[x1+dx*i][y1+dy*i]);return s.toString();}

    @Override public boolean mouseClicked(double mx,double my,int button){
        if(button!=0||finished)return true;int cell=Math.min(30,Math.max(20,(MinecraftClient.getInstance().getWindow().getScaledHeight()-145)/SIZE));int ox=cx()-SIZE*cell/2,oy=82,x=(int)((mx-ox)/cell),y=(int)((my-oy)/cell);if(x<0||y<0||x>=SIZE||y>=SIZE)return true;if(sx<0){sx=x;sy=y;status="Now select the last letter.";return true;}String candidate=line(sx,sy,x,y);String reverse=new StringBuilder(candidate).reverse().toString();String matched=words.stream().filter(w->!found.contains(w)&& (w.equals(candidate)||w.equals(reverse))).findFirst().orElse(null);if(matched!=null){found.add(matched);score+=100;markMove();}sx=sy=-1;status="Found "+found.size()+"/"+words.size();if(found.size()==words.size())finishWin(score);return true;
    }
    @Override public void render(DrawContext c,int mx,int my,float delta){MinecraftClient mc=MinecraftClient.getInstance();drawHeader(c,"WORD SEARCH",status);int cell=Math.min(30,Math.max(20,(mc.getWindow().getScaledHeight()-145)/SIZE));int ox=cx()-SIZE*cell/2,oy=82;for(int y=0;y<SIZE;y++)for(int x=0;x<SIZE;x++)c.drawCenteredTextWithShadow(mc.textRenderer,net.minecraft.text.Text.literal(Character.toString(grid[x][y])),ox+x*cell+cell/2,oy+y*cell+6,0xFFFFFFFF);c.drawTextWithShadow(mc.textRenderer,net.minecraft.text.Text.literal("Words: "+String.join("  ",words)),20,Math.min(oy+SIZE*cell+15,mc.getWindow().getScaledHeight()-18),0xFFCCCCCC);}
    @Override public void keyPressed(int key,int scan,int modifiers){if(key==GLFW.GLFW_KEY_R)begin();}
}
