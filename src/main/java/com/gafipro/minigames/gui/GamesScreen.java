package com.gafipro.minigames.gui;

import com.gafipro.minigames.core.GameCatalog;
import com.gafipro.minigames.core.GameFactory;
import com.gafipro.minigames.core.GameStats;
import com.gafipro.minigames.core.MinigamesSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.stream.Collectors;

public final class GamesScreen extends Screen {
    private final Screen parent;
    private final List<GameCatalog.Entry> all=GameCatalog.all();
    private String search="";
    private GameCatalog.Category category=null;
    private int page;
    private boolean favoritesOnly;

    public GamesScreen(Screen parent){super(Text.literal("Gafi Minigames"));this.parent=parent;MinigamesSettings.load();GameStats.load();}
    @Override protected void init(){page=0;}

    private List<GameCatalog.Entry> filtered(){String q=search.toLowerCase(Locale.ROOT);return all.stream().filter(e->(category==null||e.category()==category)&&(q.isBlank()||e.title().toLowerCase(Locale.ROOT).contains(q)||e.description().toLowerCase(Locale.ROOT).contains(q)||e.category().name().toLowerCase(Locale.ROOT).contains(q))&&(!favoritesOnly||MinigamesSettings.favorite(e.id()))).collect(Collectors.toList());}

    @Override public void render(DrawContext c,int mx,int my,float delta){renderInGameBackground(c);int cx=width/2;c.drawCenteredTextWithShadow(textRenderer,Text.literal("GAFI MINIGAMES").formatted(Formatting.BOLD,Formatting.AQUA),cx,10,0xFFFFFFFF);c.drawCenteredTextWithShadow(textRenderer,Text.literal(search.isBlank()?"Your Minecraft arcade":"Search: "+search).formatted(Formatting.GRAY),cx,24,0xFFFFFFFF);
        String[] tabs={"ALL","ARCADE","PUZZLE","BOARD","ENDLESS","★ FAV"};for(int i=0;i<tabs.length;i++){int x=cx-255+i*85;boolean on=(i==0&&category==null&&!favoritesOnly)||(i==1&&category==GameCatalog.Category.ARCADE)||(i==2&&category==GameCatalog.Category.PUZZLE)||(i==3&&category==GameCatalog.Category.BOARD)||(i==4&&category==GameCatalog.Category.ENDLESS)||(i==5&&favoritesOnly);c.fill(x,35,x+78,53,on?0xFF355C7D:0xFF252B31);c.drawCenteredTextWithShadow(textRenderer,Text.literal(tabs[i]),x+39,40,0xFFFFFFFF);}
        List<GameCatalog.Entry> list=filtered();int per=6,start=page*per;int pages=Math.max(1,(list.size()+per-1)/per);if(page>=pages)page=pages-1;int cw=210,ch=70,g=8;int left=cx-cw-g/2;for(int j=0;j<per&&start+j<list.size();j++){GameCatalog.Entry e=list.get(start+j);int row=j/2,col=j%2,x=left+col*(cw+g),y=62+row*(ch+g);boolean hov=inside(mx,my,x,y,cw,ch);c.fill(x,y,x+cw,y+ch,hov?0xFF323A43:0xFF20262D);c.fill(x,y,x+4,y+ch,colorFor(e.category()));context.drawTextWithShadow(textRenderer,Text.literal((MinigamesSettings.favorite(e.id())?"★ ":"")+e.title()).formatted(Formatting.BOLD),x+10,y+8,0xFFFFFFFF);c.drawTextWithShadow(textRenderer,Text.literal(e.category().name()).formatted(Formatting.GRAY),x+10,y+23,0xFFFFFFFF);String d=e.description().length()>42?e.description().substring(0,41)+"…":e.description();c.drawTextWithShadow(textRenderer,Text.literal(d).formatted(Formatting.DARK_GRAY),x+10,y+38,0xFFFFFFFF);String modes=e.pvp()?"PvP":e.ai()?"AI":"Solo";c.drawTextWithShadow(textRenderer,Text.literal(modes+"  •  Best "+GameStats.best(e.id())),x+10,y+54,0xFFFFD166);}
        int foot=287;c.drawCenteredTextWithShadow(textRenderer,Text.literal("Page "+(page+1)+" / "+pages+"  •  Enter: search  •  F: favorite"),cx,foot,0xFFAAAAAA);c.drawCenteredTextWithShadow(textRenderer,Text.literal("←/→ page   Esc: back"),cx,foot+14,0xFF888888);}
    private boolean inside(double a,double b,int x,int y,int w,int h){return a>=x&&a<x+w&&b>=y&&b<y+h;}private int colorFor(GameCatalog.Category c){return switch(c){case ARCADE->0xFF55CC88;case PUZZLE->0xFFAA77DD;case BOARD->0xFF5599DD;case ENDLESS->0xFFE67E22;};}
    @Override public boolean mouseClicked(double mx,double my,int button){if(button!=0)return true;int cx=width/2;String[]tabs={"ALL","ARCADE","PUZZLE","BOARD","ENDLESS","★ FAV"};for(int i=0;i<tabs.length;i++){int x=cx-255+i*85;if(inside(mx,my,x,35,78,18)){category=i==0||i==5?null:GameCatalog.Category.values()[i-1];favoritesOnly=i==5;page=0;return true;}}List<GameCatalog.Entry> list=filtered();int per=6,start=page*per,cw=210,ch=70,g=8,left=cx-cw-g/2;for(int j=0;j<per&&start+j<list.size();j++){int col=j%2,row=j/2,x=left+col*(cw+g),y=62+row*(ch+g);if(inside(mx,my,x,y,cw,ch)){GameCatalog.Entry e=list.get(start+j);MinigamesSettings.touchRecent(e.id());MinecraftClient.getInstance().setScreen(new GameScreen(this,GameFactory.create(e.id())));return true;}}return true;}
    @Override public boolean keyPressed(int key,int scan,int mods){if(key==GLFW.GLFW_KEY_ESCAPE){close();return true;}if(key==GLFW.GLFW_KEY_LEFT){page=Math.max(0,page-1);return true;}if(key==GLFW.GLFW_KEY_RIGHT){page++;return true;}if(key==GLFW.GLFW_KEY_F){List<GameCatalog.Entry> l=filtered();if(!l.isEmpty()){MinigamesSettings.toggleFavorite(l.get(Math.min(page*6,l.size()-1)).id());}return true;}if(key==GLFW.GLFW_KEY_ENTER){search=search.isEmpty()?"a":"";page=0;return true;}if((key>=GLFW.GLFW_KEY_A&&key<=GLFW.GLFW_KEY_Z)||key==GLFW.GLFW_KEY_SPACE||key==GLFW.GLFW_KEY_BACKSPACE){if(key==GLFW.GLFW_KEY_BACKSPACE&&!search.isEmpty())search=search.substring(0,search.length()-1);else if(key==GLFW.GLFW_KEY_SPACE)search+=" ";else if(key!=GLFW.GLFW_KEY_ENTER)search+=(char)('a'+key-GLFW.GLFW_KEY_A);page=0;return true;}return super.keyPressed(key,scan,mods);}
    @Override public void close(){MinecraftClient.getInstance().setScreen(parent);}
}
