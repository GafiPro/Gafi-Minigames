package com.gafipro.minigames.game.puzzle;

import com.gafipro.minigames.game.BaseGame;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public final class PuzzleCollection {
    private PuzzleCollection() {}
    private static final int FG=0xFFFFFFFF, PANEL=0xFF303840, ON=0xFF55CC88, OFF=0xFF3B424A;

    public static class Minesweeper extends BaseGame {
        private int w=9,h=9,mines=10; private boolean[][] mine,open,flag; private int safeLeft; private long started;
        @Override public String id(){return "minesweeper";} @Override public String title(){return "Minesweeper";} @Override public String category(){return "Puzzle";}
        @Override public void start(){w=9;h=9;mines=10;mine=new boolean[w][h];open=new boolean[w][h];flag=new boolean[w][h];safeLeft=w*h-mines;started=System.currentTimeMillis();place();}
        private void place(){Set<Integer> s=new HashSet<>();while(s.size()<mines)s.add(random.nextInt(w*h));for(int v:s)mine[v%w][v/w]=true;}
        private int n(int x,int y){int c=0;for(int dx=-1;dx<=1;dx++)for(int dy=-1;dy<=1;dy++)if(!(dx==0&&dy==0)&&x+dx>=0&&x+dx<w&&y+dy>=0&&y+dy<h&&mine[x+dx][y+dy])c++;return c;}
        private void reveal(int x,int y){if(x<0||y<0||x>=w||y>=h||open[x][y]||flag[x][y])return;open[x][y]=true;safeLeft--;if(n(x,y)==0)for(int dx=-1;dx<=1;dx++)for(int dy=-1;dy<=1;dy++)if(dx!=0||dy!=0)reveal(x+dx,y+dy);}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"MINESWEEPER","Left click reveal • Right click flag • "+Math.max(0,safeLeft)+" safe tiles left");int s=28,ox=cx()-w*s/2,oy=72;for(int y=0;y<h;y++)for(int x=0;x<w;x++){int px=ox+x*s,py=oy+y*s;boolean shown=open[x][y];c.fill(px,py,px+s-2,py+s-2,shown?0xFF56616B:PANEL);if(flag[x][y])c.drawCenteredTextWithShadow(net.minecraft.client.MinecraftClient.getInstance().textRenderer,Text.literal("⚑"),px+13,py+7,0xFFFFFF55);else if(shown){if(mine)c.drawCenteredTextWithShadow(net.minecraft.client.MinecraftClient.getInstance().textRenderer,Text.literal("✹"),px+13,py+7,0xFFFF5555);else if(n(x,y)>0)c.drawCenteredTextWithShadow(net.minecraft.client.MinecraftClient.getInstance().textRenderer,Text.literal(""+n(x,y)),px+13,py+7,FG);}}
        }
        @Override public boolean mouseClicked(double mx,double my,int b){if(finished)return true;int s=28,ox=cx()-w*s/2,oy=72;int x=(int)((mx-ox)/s),y=(int)((my-oy)/s);if(x<0||y<0||x>=w||y>=h)return true;if(b==1){flag[x][y]=!flag[x][y];return true;}if(b==0&&!flag[x][y]){if(mine[x][y]){for(int yy=0;yy<h;yy++)for(int xx=0;xx<w;xx++)if(mine[xx][yy])open[xx][yy]=true;finish(score);status="Mine hit.";}else{reveal(x,y);score=Math.max(0,1000-(int)((System.currentTimeMillis()-started)/1000)*5);if(safeLeft<=0)finishWin(score);}}return true;}
    }

    public static class Game2048 extends BaseGame {
        private int size=4;private int[][] b;private int localScore;private boolean moved;
        public Game2048(){} public Game2048(boolean extreme){size=extreme?5:4;}
        @Override public String id(){return size==5?"2048_extreme":"2048";} @Override public String title(){return size==5?"2048 Extreme":"2048";} @Override public String category(){return "Puzzle";}
        @Override public void start(){b=new int[size][size];localScore=0;add();add();}
        private void add(){List<int[]> e=new ArrayList<>();for(int y=0;y<size;y++)for(int x=0;x<size;x++)if(b[x][y]==0)e.add(new int[]{x,y});if(!e.isEmpty()){int[] p=e.get(random.nextInt(e.size()));b[p[0]][p[1]]=random.nextInt(10)==0?4:2;}}
        private int[] line(int[] a){int[] z=new int[size];int p=0;for(int v:a)if(v!=0)z[p++]=v;for(int i=0;i<size-1;i++)if(z[i]!=0&&z[i]==z[i+1]){z[i]*=2;localScore+=z[i];for(int j=i+1;j<size-1;j++)z[j]=z[j+1];z[size-1]=0;}return z;}
        private void move(int dir){int[][] old=new int[size][size];for(int x=0;x<size;x++)old[x]=b[x].clone();if(dir==0||dir==1){for(int y=0;y<size;y++){int[] a=new int[size];for(int x=0;x<size;x++)a[x]=dir==0?b[x][y]:b[size-1-x][y];a=line(a);for(int x=0;x<size;x++)b[dir==0?x:size-1-x][y]=a[x];}}else{for(int x=0;x<size;x++){int[] a=new int[size];for(int y=0;y<size;y++)a[y]=dir==2?b[x][y]:b[x][size-1-y];a=line(a);for(int y=0;y<size;y++)b[x][dir==2?y:size-1-y]=a[y];}}moved=!Arrays.deepEquals(old,b);if(moved)add();if(!canMove())finish(localScore);if(has2048())finishWin(localScore);}
        private boolean canMove(){for(int y=0;y<size;y++)for(int x=0;x<size;x++){if(b[x][y]==0)return true;if(x+1<size&&b[x][y]==b[x+1][y])return true;if(y+1<size&&b[x][y]==b[x][y+1])return true;}return false;}
        private boolean has2048(){for(int[] r:b)for(int v:r)if(v>=2048)return true;return false;}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,title(),"Arrow keys / WASD to slide • Score: "+localScore);int s=52,ox=cx()-size*s/2,oy=78;for(int y=0;y<size;y++)for(int x=0;x<size;x++){int v=b[x][y];c.fill(ox+x*s,oy+y*s,ox+x*s+s-3,oy+y*s+s-3,v==0?PANEL:(v>=1024?0xFFB87333:0xFF665044));if(v>0)c.drawCenteredTextWithShadow(net.minecraft.client.MinecraftClient.getInstance().textRenderer,Text.literal(""+v),ox+x*s+24,oy+y*s+18,FG);}}
        @Override public void keyPressed(int key,int scan,int mods){if(finished)return;if(key==GLFW.GLFW_KEY_LEFT||key==GLFW.GLFW_KEY_A)move(0);else if(key==GLFW.GLFW_KEY_RIGHT||key==GLFW.GLFW_KEY_D)move(1);else if(key==GLFW.GLFW_KEY_UP||key==GLFW.GLFW_KEY_W)move(2);else if(key==GLFW.GLFW_KEY_DOWN||key==GLFW.GLFW_KEY_S)move(3);}
        @Override public boolean mouseClicked(double x,double y,int b){return true;}
    }

    public static final class SlidingPuzzle extends BaseGame {
        private int n=4;private int[] p;private int empty;
        @Override public String id(){return "sliding_puzzle";} @Override public String title(){return "Sliding Puzzle";} @Override public String category(){return "Puzzle";}
        @Override public void start(){p=new int[n*n];for(int i=0;i<p.length;i++)p[i]=i;empty=p.length-1;for(int i=0;i<300;i++){List<Integer> a=adj(empty);int q=a.get(random.nextInt(a.size()));swap(empty,q);empty=q;}}
        private List<Integer> adj(int i){List<Integer>a=new ArrayList<>();int x=i%n,y=i/n;if(x>0)a.add(i-1);if(x<n-1)a.add(i+1);if(y>0)a.add(i-n);if(y<n-1)a.add(i+n);return a;}
        private void swap(int a,int b){int t=p[a];p[a]=p[b];p[b]=t;}
        private boolean solved(){for(int i=0;i<p.length;i++)if(p[i]!=i)return false;return true;}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"SLIDING PUZZLE","Click a tile next to the empty space.");int s=58,ox=cx()-2*s,oy=78;for(int i=0;i<p.length;i++){int x=i%n,y=i/n;int v=p[i];c.fill(ox+x*s,oy+y*s,ox+x*s+s-3,oy+y*s+s-3,v==n*n-1?OFF:0xFF4B6A88);if(v<n*n-1)c.drawCenteredTextWithShadow(net.minecraft.client.MinecraftClient.getInstance().textRenderer,Text.literal(""+(v+1)),ox+x*s+27,oy+y*s+20,FG);}}
        @Override public boolean mouseClicked(double mx,double my,int b){if(b!=0||finished)return true;int s=58,ox=cx()-2*s,oy=78;int x=(int)((mx-ox)/s),y=(int)((my-oy)/s),i=y*n+x;if(x>=0&&x<n&&y>=0&&y<n&&adj(empty).contains(i)){swap(empty,i);empty=i;if(solved())finishWin(10000-Math.min(9000,ticks*2));}return true;}
    }

    public static final class LightsOut extends BaseGame {
        private boolean[][] on=new boolean[5][5];
        @Override public String id(){return "lights_out";} @Override public String title(){return "Lights Out";} @Override public String category(){return "Puzzle";}
        @Override public void start(){for(int y=0;y<5;y++)for(int x=0;x<5;x++)on[x][y]=random.nextBoolean();}
        private void toggle(int x,int y){int[][]d={{0,0},{1,0},{-1,0},{0,1},{0,-1}};for(int[]q:d){int a=x+q[0],b=y+q[1];if(a>=0&&b>=0&&a<5&&b<5)on[a][b]=!on[a][b];}}
        private boolean done(){for(boolean[]r:on)for(boolean v:r)if(v)return false;return true;}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"LIGHTS OUT","Turn every tile dark.");int s=52,ox=cx()-2*s,oy=82;for(int y=0;y<5;y++)for(int x=0;x<5;x++){c.fill(ox+x*s,oy+y*s,ox+x*s+s-3,oy+y*s+s-3,on[x][y]?0xFFF1C40F:OFF);}}
        @Override public boolean mouseClicked(double mx,double my,int b){if(b!=0||finished)return true;int s=52,ox=cx()-2*s,oy=82;int x=(int)((mx-ox)/s),y=(int)((my-oy)/s);if(x>=0&&x<5&&y>=0&&y<5){toggle(x,y);score++;if(done())finishWin(Math.max(100,1000-score*10));}return true;}
    }

    public static final class Maze extends BaseGame {
        private final int n=15;private boolean[][] wall=new boolean[n][n];private int px=1,py=1;private final int ex=n-2,ey=n-2;
        @Override public String id(){return "maze";} @Override public String title(){return "Maze";} @Override public String category(){return "Puzzle";}
        @Override public void start(){generate();}
        private void generate(){for(int y=0;y<n;y++)Arrays.fill(wall[y],true);carve(1,1);}
        private void carve(int x,int y){wall[y][x]=false;int[] dx={1,-1,0,0},dy={0,0,1,-1};List<Integer> a=new ArrayList<>(List.of(0,1,2,3));Collections.shuffle(a);for(int i:a){int nx=x+dx[i]*2,ny=y+dy[i]*2;if(nx>0&&ny>0&&nx<n-1&&ny<n-1&&wall[ny][nx]){wall[y+dy[i]][x+dx[i]]=false;carve(nx,ny);}}wall[ey][ex]=false;}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"MAZE","Arrow keys / WASD • Reach the green exit.");int s=14,ox=cx()-n*s/2,oy=72;for(int y=0;y<n;y++)for(int x=0;x<n;x++)c.fill(ox+x*s,oy+y*s,ox+x*s+s,oy+y*s+s,wall[y][x]?0xFF1E242A:0xFFB8B8B8);c.fill(ox+px*s+2,oy+py*s+2,ox+px*s+s-2,oy+py*s+s-2,ON);c.fill(ox+ex*s+2,oy+ey*s+2,ox+ex*s+s-2,oy+ey*s+s-2,0xFFE67E22);}
        @Override public void keyPressed(int k,int s,int m){if(finished)return;int nx=px,ny=py;if(k==GLFW.GLFW_KEY_LEFT||k==GLFW.GLFW_KEY_A)nx--;if(k==GLFW.GLFW_KEY_RIGHT||k==GLFW.GLFW_KEY_D)nx++;if(k==GLFW.GLFW_KEY_UP||k==GLFW.GLFW_KEY_W)ny--;if(k==GLFW.GLFW_KEY_DOWN||k==GLFW.GLFW_KEY_S)ny++;if(nx>=0&&ny>=0&&nx<n&&ny<n&&!wall[ny][nx]){px=nx;py=ny;if(px==ex&&py==ey)finishWin(Math.max(100,10000-ticks*15));}}
        @Override public boolean mouseClicked(double x,double y,int b){return true;}
    }

    public static final class Match3 extends BaseGame {
        private int[][] g=new int[8][8];private int sx=-1,sy=-1, moves=30;
        @Override public String id(){return "match_3";} @Override public String title(){return "Match-3";} @Override public String category(){return "Puzzle";}
        @Override public void start(){for(int y=0;y<8;y++)for(int x=0;x<8;x++)g[x][y]=random.nextInt(5);resolve();}
        private void swap(int x1,int y1,int x2,int y2){int t=g[x1][y1];g[x1][y1]=g[x2][y2];g[x2][y2]=t;}
        private boolean resolve(){boolean[][]r=new boolean[8][8];boolean any=false;for(int y=0;y<8;y++){for(int x=0;x<6;x++)if(g[x][y]==g[x+1][y]&&g[x][y]==g[x+2][y]){r[x][y]=r[x+1][y]=r[x+2][y]=true;any=true;}}for(int x=0;x<8;x++)for(int y=0;y<6;y++)if(g[x][y]==g[x][y+1]&&g[x][y]==g[x][y+2]){r[x][y]=r[x][y+1]=r[x][y+2]=true;any=true;}if(any){for(int x=0;x<8;x++){int p=7;for(int y=7;y>=0;y--)if(!r[x][y])g[x][p--]=g[x][y];while(p>=0)g[x][p--]=random.nextInt(5);score+=100;} }return any;}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"MATCH-3","Swap adjacent gems • Moves: "+moves+" • Score: "+score);int s=34,ox=cx()-4*s,oy=76;for(int y=0;y<8;y++)for(int x=0;x<8;x++){int col=0xFF000000|new int[]{0xE74C3C,0x2ECC71,0x3498DB,0x9B59B6,0xF1C40F}[g[x][y]];c.fill(ox+x*s,oy+y*s,ox+x*s+s-3,oy+y*s+s-3,col);if(x==sx&&y==sy)c.fill(ox+x*s,oy+y*s,ox+x*s+s-3,oy+y*s+s-3,0xFFFFFFFF);}}
        @Override public boolean mouseClicked(double mx,double my,int b){if(b!=0||finished)return true;int s=34,ox=cx()-4*s,oy=76;int x=(int)((mx-ox)/s),y=(int)((my-oy)/s);if(x<0||y<0||x>=8||y>=8)return true;if(sx<0){sx=x;sy=y;}else{if(Math.abs(x-sx)+Math.abs(y-sy)==1){swap(sx,sy,x,y);if(!resolve())swap(sx,sy,x,y);else{moves--;while(resolve()){}if(moves<=0)finishWin(score);}}sx=sy=-1;}return true;}
    }

    public static final class Sudoku extends BaseGame {
        private int[][] solution=new int[9][9],b=new int[9][9];private int sel=-1;
        @Override public String id(){return "sudoku";} @Override public String title(){return "Sudoku";} @Override public String category(){return "Puzzle";}
        @Override public void start(){fill(0,0);for(int y=0;y<9;y++)for(int x=0;x<9;x++)b[x][y]=solution[x][y];for(int i=0;i<45;i++){int x=random.nextInt(9),y=random.nextInt(9);b[x][y]=0;}}
        private boolean fill(int x,int y){if(y==9)return true;int nx=(x+1)%9,ny=y+(x+1)/9;List<Integer>a=new ArrayList<>();for(int i=1;i<=9;i++)a.add(i);Collections.shuffle(a);for(int v:a){if(valid(x,y,v)){solution[x][y]=v;if(fill(nx,ny))return true;}}solution[x][y]=0;return false;}
        private boolean valid(int x,int y,int v){for(int i=0;i<9;i++)if(solution[i][y]==v||solution[x][i]==v)return false;int bx=x/3*3,by=y/3*3;for(int yy=by;yy<by+3;yy++)for(int xx=bx;xx<bx+3;xx++)if(solution[xx][yy]==v)return false;return true;}
        private boolean complete(){for(int y=0;y<9;y++)for(int x=0;x<9;x++)if(b[x][y]!=solution[x][y])return false;return true;}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"SUDOKU","Click a cell, then press 1-9.");int s=30,ox=cx()-135,oy=70;for(int y=0;y<9;y++)for(int x=0;x<9;x++){c.fill(ox+x*s,oy+y*s,ox+x*s+s-1,oy+y*s+s-1,(x/3+y/3)%2==0?0xFF2E353C:0xFF252B31);int v=b[x][y];if(v>0)c.drawCenteredTextWithShadow(net.minecraft.client.MinecraftClient.getInstance().textRenderer,Text.literal(""+v),ox+x*s+14,oy+y*s+10,FG);} }
        @Override public boolean mouseClicked(double mx,double my,int k){if(k!=0)return true;int s=30,ox=cx()-135,oy=70;int x=(int)((mx-ox)/s),y=(int)((my-oy)/s);if(x>=0&&x<9&&y>=0&&y<9&&b[x][y]!=solution[x][y])sel=y*9+x;return true;}
        @Override public void keyPressed(int k,int sc,int m){if(finished||sel<0)return;if(k>=GLFW.GLFW_KEY_1&&k<=GLFW.GLFW_KEY_9){int x=sel%9,y=sel/9;int v=k-GLFW.GLFW_KEY_0;if(validForBoard(x,y,v)){b[x][y]=v;score+=5;if(complete())finishWin(score+500);}}}
        private boolean validForBoard(int x,int y,int v){for(int i=0;i<9;i++)if(i!=x&&b[i][y]==v)return false;for(int i=0;i<9;i++)if(i!=y&&b[x][i]==v)return false;int bx=x/3*3,by=y/3*3;for(int yy=by;yy<by+3;yy++)for(int xx=bx;xx<bx+3;xx++)if((xx!=x||yy!=y)&&b[xx][yy]==v)return false;return true;}
    }

    public static final class WordSearch extends BaseGame {
        private final String[] pool={"CREEPER","DIAMOND","NETHER","REDSTONE","POTION"};private char[][] g=new char[10][10];private Set<String> left=new LinkedHashSet<>();private int sx=-1,sy=-1;
        @Override public String id(){return "word_search";} @Override public String title(){return "Word Search";} @Override public String category(){return "Puzzle";}
        @Override public void start(){left.clear();for(String w:pool)left.add(w);for(int y=0;y<10;y++)for(int x=0;x<10;x++)g[x][y]=(char)('A'+random.nextInt(26));for(String w:pool){int y=random.nextInt(10),x=random.nextInt(10),dx=random.nextBoolean()?1:0,dy=dx==1?0:1;if(x+dx*(w.length()-1)>=10||y+dy*(w.length()-1)>=10){y=0;x=0;dx=1;}for(int i=0;i<w.length();i++)g[x+dx*i][y+dy*i]=w.charAt(i);}}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"WORD SEARCH","Find: "+String.join(", ",left));int s=24,ox=cx()-120,oy=78;for(int y=0;y<10;y++)for(int x=0;x<10;x++){c.fill(ox+x*s,oy+y*s,ox+x*s+s-2,oy+y*s+s-2,OFF);c.drawCenteredTextWithShadow(net.minecraft.client.MinecraftClient.getInstance().textRenderer,Text.literal(""+g[x][y]),ox+x*s+11,oy+y*s+6,FG);}}
        @Override public boolean mouseClicked(double mx,double my,int b){if(b!=0||finished)return true;int s=24,ox=cx()-120,oy=78;int x=(int)((mx-ox)/s),y=(int)((my-oy)/s);if(x<0||y<0||x>=10||y>=10)return true;if(sx<0){sx=x;sy=y;}else{String w="";int dx=Integer.compare(x,sx),dy=Integer.compare(y,sy),len=Math.max(Math.abs(x-sx),Math.abs(y-sy))+1;for(int i=0;i<len;i++)w+=g[sx+dx*i][sy+dy*i];String r=new StringBuilder(w).reverse().toString();if(left.remove(w)||left.remove(r)){score+=100;if(left.isEmpty())finishWin(score);}sx=sy=-1;}return true;}
    }

    public static final class Nonogram extends BaseGame {
        private boolean[][] target=new boolean[10][10],mark=new boolean[10][10];
        @Override public String id(){return "nonogram";} @Override public String title(){return "Nonogram";} @Override public String category(){return "Puzzle";}
        @Override public void start(){for(int y=0;y<10;y++)for(int x=0;x<10;x++)target[x][y]=random.nextBoolean();}
        private boolean done(){for(int y=0;y<10;y++)for(int x=0;x<10;x++)if(target[x][y]!=mark[x][y])return false;return true;}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"NONOGRAM","Left click fill • Right click clear • Recreate the hidden picture");int s=23,ox=cx()-115,oy=80;for(int y=0;y<10;y++)for(int x=0;x<10;x++){c.fill(ox+x*s,oy+y*s,ox+x*s+s-2,oy+y*s+s-2,mark[x][y]?0xFF55AADD:OFF);}}
        @Override public boolean mouseClicked(double mx,double my,int b){if(b!=0&&b!=1)return true;int s=23,ox=cx()-115,oy=80,x=(int)((mx-ox)/s),y=(int)((my-oy)/s);if(x>=0&&y>=0&&x<10&&y<10){mark[x][y]=b==0;if(done())finishWin(1000); }return true;}
    }

    public static final class SpotDifference extends BaseGame {
        private int[] diff=new int[5];private boolean[] found=new boolean[5];private int round;
        @Override public String id(){return "spot_difference";} @Override public String title(){return "Spot the Difference";} @Override public String category(){return "Puzzle";}
        @Override public void start(){for(int i=0;i<5;i++)diff[i]=random.nextInt(16);}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"SPOT THE DIFFERENCE","Find the changed tiles • "+round+" / 5");for(int board=0;board<2;board++)for(int i=0;i<16;i++){int x=cx()-180+board*185+(i%4)*44,y=82+(i/4)*44;c.fill(x,y,x+40,y+40,(contains(i)&&board==1)?0xFFE67E22:0xFF56616B);if(board==1&&contains(i))c.fill(x+7,y+7,x+33,y+33,0xFF8E44AD);}}
        private boolean contains(int i){for(int v:diff)if(v==i)return true;return false;}
        @Override public boolean mouseClicked(double mx,double my,int b){if(b!=0||finished)return true;int x0=cx()-180;int i=(int)((mx-(x0+185))/44)+(int)((my-82)/44)*4;if(i>=0&&i<16){for(int d=0;d<diff.length;d++)if(diff[d]==i&&!found[d]){found[d]=true;round++;score+=200;if(round>=5)finishWin(score);return true;}}}return true;}
    }

    public static final class SequenceMemory extends BaseGame {
        private final List<Integer> seq=new ArrayList<>();private int idx;private boolean show;private long until;private int flash=-1;
        @Override public String id(){return "sequence_memory";} @Override public String title(){return "Sequence Memory";} @Override public String category(){return "Puzzle";}
        @Override public void start(){seq.add(random.nextInt(16));show=true;idx=0;flash=seq.get(0);until=System.currentTimeMillis()+500;}
        @Override public void tick(){super.tick();if(!finished&&show&&System.currentTimeMillis()>until){show=false;flash=-1;status="Repeat the sequence.";}}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"SEQUENCE MEMORY","Length: "+seq.size());int s=48,ox=cx()-96,oy=84;for(int i=0;i<16;i++){int x=ox+(i%4)*s,y=oy+(i/4)*s;c.fill(x,y,x+s-3,y+s-3,i==flash?0xFFFFFFFF:OFF);}}
        @Override public boolean mouseClicked(double mx,double my,int b){if(b!=0||finished||show)return true;int s=48,ox=cx()-96,oy=84,x=(int)((mx-ox)/s),y=(int)((my-oy)/s);if(x<0||y<0||x>=4||y>=4)return true;int i=y*4+x;if(i!=seq.get(idx)){finish(score);status="Sequence failed.";}else{idx++;if(idx>=seq.size()){score+=seq.size()*100;seq.add(random.nextInt(16));idx=0;show=true;flash=seq.get(seq.size()-1);until=System.currentTimeMillis()+500;if(seq.size()>10)finishWin(score);}}return true;}
    }
}
