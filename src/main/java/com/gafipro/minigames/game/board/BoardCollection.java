package com.gafipro.minigames.game.board;

import com.gafipro.minigames.game.BaseGame;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public final class BoardCollection {
    private BoardCollection() {}
    private static final int FG=0xFFFFFFFF, DARK=0xFF20262D, PANEL=0xFF3B424A, XCOL=0xFFE74C3C, OCOL=0xFF55AADD;

    public static final class TicTacToe extends BaseGame {
        private int[] b=new int[9];private int turn=1;private boolean vsAI=true;
        @Override public String id(){return "tic_tac_toe";}@Override public String title(){return "Tic-Tac-Toe";}@Override public String category(){return "Board";}
        @Override public void start(){Arrays.fill(b,0);turn=1;status="Your turn (X).";}
        private int win(){int[][]l={{0,1,2},{3,4,5},{6,7,8},{0,3,6},{1,4,7},{2,5,8},{0,4,8},{2,4,6}};for(int[]q:l)if(b[q[0]]!=0&&b[q[0]]==b[q[1]]&&b[q[1]]==b[q[2]])return b[q[0]];for(int v:b)if(v==0)return 0;return 3;}
        private void ai(){int best=-2,move=-1;for(int i=0;i<9;i++)if(b[i]==0){b[i]=2;int v=minimax(false);b[i]=0;if(v>best){best=v;move=i;}}if(move>=0)b[move]=2;turn=1;status="Your turn (X).";resolve();}
        private int minimax(boolean max){int w=win();if(w==2)return 1;if(w==1)return -1;if(w==3)return 0;int best=max?-2:2;for(int i=0;i<9;i++)if(b[i]==0){b[i]=max?2:1;int v=minimax(!max);b[i]=0;best=max?Math.max(best,v):Math.min(best,v);}return best;}
        private void resolve(){int w=win();if(w!=0){if(w==1)finishWin(1000);else if(w==2)finish(0);else finish(500);}}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"TIC-TAC-TOE",vsAI?status:"Local 2-player");int s=72,ox=cx()-108,oy=80;for(int y=0;y<3;y++)for(int x=0;x<3;x++){int i=y*3+x;c.fill(ox+x*s,oy+y*s,ox+x*s+s-4,oy+y*s+s-4,PANEL);int v=b[i];if(v>0)c.drawCenteredTextWithShadow(net.minecraft.client.MinecraftClient.getInstance().textRenderer,Text.literal(v==1?"X":"O"),ox+x*s+34,oy+y*s+22,v==1?XCOL:OCOL);}}
        @Override public boolean mouseClicked(double mx,double my,int btn){if(btn!=0||finished)return true;int s=72,ox=cx()-108,oy=80,x=(int)((mx-ox)/s),y=(int)((my-oy)/s);if(x<0||y<0||x>2||y>2)return true;int i=y*3+x;if(b[i]!=0)return true;b[i]=turn;resolve();if(!finished&&vsAI)ai();else if(!finished){turn=3-turn;status="Player "+(turn==1?"X":"O")+"'s turn.";}return true;}
        @Override public void keyPressed(int k,int sc,int m){if(k==GLFW.GLFW_KEY_TAB)vsAI=!vsAI;}
    }

    public static class ConnectFour extends BaseGame {
        protected int w=7,h=6;protected int[][]b=new int[w][h];protected int turn=1;protected boolean vsAI=true;
        @Override public String id(){return "connect_four";}@Override public String title(){return "Connect Four";}@Override public String category(){return "Board";}
        @Override public void start(){b=new int[w][h];turn=1;status="Drop a red piece.";}
        protected boolean drop(int col,int player){if(col<0||col>=w)return false;for(int y=h-1;y>=0;y--)if(b[col][y]==0){b[col][y]=player;return true;}return false;}
        protected boolean four(int p){for(int x=0;x<w;x++)for(int y=0;y<h;y++){if(b[x][y]!=p)continue;for(int[]d:new int[][]{{1,0},{0,1},{1,1},{1,-1}}){int c=1;for(int k=1;k<4;k++){int xx=x+d[0]*k,yy=y+d[1]*k;if(xx>=0&&yy>=0&&xx<w&&yy<h&&b[xx][yy]==p)c++;}if(c>=4)return true;}}return false;}
        private boolean full(){for(int x=0;x<w;x++)if(b[x][0]==0)return false;return true;}
        protected void ai(){List<Integer>a=new ArrayList<>();for(int x=0;x<w;x++)if(b[x][0]==0)a.add(x);if(a.isEmpty())return;int choice=a.get(random.nextInt(a.size()));for(int x:a){drop(x,2);if(four(2)){choice=x;b[x][index(x)] = 0;break;}b[x][index(x)]=0;}drop(choice,2);}
        private int index(int x){for(int y=0;y<h;y++)if(b[x][y]!=0)return y;return -1;}
        private void resolve(){if(four(turn)){if(turn==1)finishWin(1000);else finish(0);}else if(full())finish(500);}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"CONNECT FOUR",vsAI?status:"Local 2-player");int s=45,ox=cx()-w*s/2,oy=78;c.fill(ox-5,oy-5,ox+w*s+5,oy+h*s+5,0xFF234A9A);for(int y=0;y<h;y++)for(int x=0;x<w;x++){int v=b[x][y];c.fill(ox+x*s+4,oy+y*s+4,ox+x*s+s-4,oy+y*s+s-4,v==1?XCOL:v==2?OCOL:0xFFB8B8B8);}}
        @Override public boolean mouseClicked(double mx,double my,int btn){if(btn!=0||finished)return true;int s=45,ox=cx()-w*s/2,oy=78,col=(int)((mx-ox)/s);if(col<0||col>=w)return true;if(drop(col,turn)){resolve();if(!finished&&vsAI){ai();resolve();if(!finished){turn=1;status="Your turn.";}}else if(!finished){turn=3-turn;status="Player "+turn+"'s turn.";}}return true;}
    }

    public static final class FourInRowMini extends ConnectFour {public FourInRowMini(){w=5;h=5;}@Override public String id(){return "four_in_row_mini";}@Override public String title(){return "Four-in-a-Row Mini";}@Override public void start(){b=new int[w][h];turn=1;status="Connect four on a compact board.";}}

    public static final class Reversi extends BaseGame {
        private int[][]b=new int[8][8];private int turn=1;private boolean vsAI=true;
        @Override public String id(){return "reversi";}@Override public String title(){return "Reversi / Othello";}@Override public String category(){return "Board";}
        @Override public void start(){b=new int[8][8];b[3][3]=2;b[4][4]=2;b[3][4]=1;b[4][3]=1;turn=1;status="Your turn.";}
        private List<int[]> flips(int x,int y,int p){if(b[x][y]!=0)return List.of();List<int[]>out=new ArrayList<>();for(int[]d:new int[][]{{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}}){List<int[]>line=new ArrayList<>();int a=x+d[0],q=y+d[1];while(a>=0&&q>=0&&a<8&&q<8&&b[a][q]==3-p){line.add(new int[]{a,q});a+=d[0];q+=d[1];}if(!line.isEmpty()&&a>=0&&q>=0&&a<8&&q<8&&b[a][q]==p)out.addAll(line);}return out;}
        private boolean has(int p){for(int y=0;y<8;y++)for(int x=0;x<8;x++)if(!flips(x,y,p).isEmpty())return true;return false;}
        private void place(int x,int y,int p){b[x][y]=p;for(int[]q:flips(x,y,p))b[q[0]][q[1]]=p;}
        private void ai(){List<int[]>m=new ArrayList<>();for(int y=0;y<8;y++)for(int x=0;x<8;x++)if(!flips(x,y,2).isEmpty())m.add(new int[]{x,y});if(!m.isEmpty()){int[]q=m.get(random.nextInt(m.size()));place(q[0],q[1],2);}turn=1;status="Your turn.";}
        private void endIfNeeded(){if(!has(turn)){turn=3-turn;if(!has(turn)){int a=0,d=0;for(int[]r:b)for(int v:r){if(v==1)a++;if(v==2)d++;}finish(a>d?1000:a==d?500:0);}}}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"REVERSI",status);int s=38,ox=cx()-4*s,oy=82;c.fill(ox-3,oy-3,ox+8*s+3,oy+8*s+3,0xFF286A3F);for(int y=0;y<8;y++)for(int x=0;x<8;x++){c.fill(ox+x*s,oy+y*s,ox+x*s+s-2,oy+y*s+s-2,0xFF3A8A58);if(b[x][y]>0)c.fill(ox+x*s+8,oy+y*s+8,ox+x*s+s-10,oy+y*s+s-10,b[x][y]==1?XCOL:OCOL);}}
        @Override public boolean mouseClicked(double mx,double my,int btn){if(btn!=0||finished)return true;int s=38,ox=cx()-4*s,oy=82,x=(int)((mx-ox)/s),y=(int)((my-oy)/s);if(x>=0&&y>=0&&x<8&&y<8&&!flips(x,y,1).isEmpty()){place(x,y,1);turn=2;endIfNeeded();if(!finished&&vsAI){ai();endIfNeeded();}}return true;}
    }

    public static final class Gomoku extends BaseGame {
        private int n=13;private int[][]b=new int[n][n];private int turn=1;private boolean vsAI=true;
        @Override public String id(){return "gomoku";}@Override public String title(){return "Gomoku";}@Override public String category(){return "Board";}
        @Override public void start(){b=new int[n][n];turn=1;}
        private boolean line(int p){for(int y=0;y<n;y++)for(int x=0;x<n;x++)if(b[x][y]==p)for(int[]d:new int[][]{{1,0},{0,1},{1,1},{1,-1}}){int c=1;for(int k=1;k<5;k++){int a=x+d[0]*k,q=y+d[1]*k;if(a>=0&&q>=0&&a<n&&q<n&&b[a][q]==p)c++;}if(c>=5)return true;}return false;}
        private boolean full(){for(int[]r:b)for(int v:r)if(v==0)return false;return true;}
        private void ai(){List<int[]>a=new ArrayList<>();for(int y=0;y<n;y++)for(int x=0;x<n;x++)if(b[x][y]==0)a.add(new int[]{x,y});if(!a.isEmpty()){int[]q=a.get(random.nextInt(a.size()));b[q[0]][q[1]]=2;}}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"GOMOKU",status.isEmpty()?"Connect five stones.":status);int s=24,ox=cx()-6*s,oy=72;for(int y=0;y<n;y++)for(int x=0;x<n;x++){c.fill(ox+x*s,oy+y*s,ox+x*s+s-1,oy+y*s+s-1,0xFFB48B5A);if(b[x][y]>0)c.fill(ox+x*s+4,oy+y*s+4,ox+x*s+s-5,oy+y*s+s-5,b[x][y]==1?XCOL:OCOL);}}
        @Override public boolean mouseClicked(double mx,double my,int btn){if(btn!=0||finished)return true;int s=24,ox=cx()-6*s,oy=72,x=(int)((mx-ox)/s),y=(int)((my-oy)/s);if(x<0||y<0||x>=n||y>=n||b[x][y]!=0)return true;b[x][y]=1;if(line(1)){finishWin(1000);return true;}if(full()){finish(500);return true;}if(vsAI){ai();if(line(2))finish(0);else if(full())finish(500);}return true;}
    }

    public static final class Checkers extends BaseGame {
        private int[][]b=new int[8][8];private int turn=1;private boolean vsAI=true;
        @Override public String id(){return "checkers";}@Override public String title(){return "Checkers";}@Override public String category(){return "Board";}
        @Override public void start(){b=new int[8][8];for(int y=0;y<3;y++)for(int x=0;x<8;x++)if((x+y)%2==1)b[x][y]=2;for(int y=5;y<8;y++)for(int x=0;x<8;x++)if((x+y)%2==1)b[x][y]=1;turn=1;}
        private boolean valid(int x,int y,int nx,int ny,int p){if(nx<0||ny<0||nx>=8||ny>=8||b[nx][ny]!=0||Math.abs(nx-x)!=Math.abs(ny-y))return false;int dy=ny-y;if(p==1&&dy>=0)return false;if(p==2&&dy<=0)return false;return Math.abs(nx-x)==1|| (Math.abs(nx-x)==2&&b[(x+nx)/2][(y+ny)/2]==3-p);}
        private boolean has(int p){for(int y=0;y<8;y++)for(int x=0;x<8;x++)if(b[x][y]==p)for(int dx=-2;dx<=2;dx+=2)for(int dy=-2;dy<=2;dy+=2)if(dx!=0&&dy!=0&&valid(x,y,x+dx/2,y+dy/2,p))return true;return false;}
        private void ai(){List<int[]>m=new ArrayList<>();for(int y=0;y<8;y++)for(int x=0;x<8;x++)if(b[x][y]==2)for(int nx=0;nx<8;nx++)for(int ny=0;ny<8;ny++)if(valid(x,y,nx,ny,2))m.add(new int[]{x,y,nx,ny});if(!m.isEmpty()){int[]q=m.get(random.nextInt(m.size()));b[q[2]][q[3]]=2;b[q[0]][q[1]]=0;if(Math.abs(q[2]-q[0])==2)b[(q[0]+q[2])/2][(q[1]+q[3])/2]=0;}}
        private int sx=-1,sy=-1;
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"CHECKERS","Click a piece, then its destination.");int s=44,ox=cx()-4*s,oy=78;for(int y=0;y<8;y++)for(int x=0;x<8;x++){c.fill(ox+x*s,oy+y*s,ox+x*s+s,oy+y*s+s,(x+y)%2==0?0xFFD8C3A5:0xFF765038);if(b[x][y]>0)c.fill(ox+x*s+6,oy+y*s+6,ox+x*s+s-6,oy+y*s+s-6,b[x][y]==1?XCOL:OCOL);}}
        @Override public boolean mouseClicked(double mx,double my,int btn){if(btn!=0||finished)return true;int s=44,ox=cx()-4*s,oy=78,x=(int)((mx-ox)/s),y=(int)((my-oy)/s);if(x<0||y<0||x>=8||y>=8)return true;if(sx<0&&b[x][y]==1){sx=x;sy=y;}else if(sx>=0){if(valid(sx,sy,x,y,1)){b[x][y]=1;b[sx][sy]=0;if(Math.abs(x-sx)==2)b[(x+sx)/2][(y+sy)/2]=0;sx=sy=-1;boolean any2=false;for(int yy=0;yy<8;yy++)for(int xx=0;xx<8;xx++)if(b[xx][yy]==2)any2=true;if(!any2){finishWin(1000);return true;}if(vsAI){ai();}}else{sx=sy=-1;}}return true;}
    }

    public static final class DotsAndBoxes extends BaseGame {
        private final int n=5;private boolean[][] hx=new boolean[n][n+1],vy=new boolean[n+1][n];private int[][]owner=new int[n-1][n-1];private int turn=1;private int score1,score2;private int ax=-1,ay=-1;
        @Override public String id(){return "dots_and_boxes";}@Override public String title(){return "Dots and Boxes";}@Override public String category(){return "Board";}
        @Override public void start(){Arrays.stream(hx).forEach(r->Arrays.fill(r,false));Arrays.stream(vy).forEach(r->Arrays.fill(r,false));for(int[]r:owner)Arrays.fill(r,0);score1=score2=0;turn=1;}
        private boolean box(int x,int y){return x>=0&&y>=0&&x<n-1&&y<n-1&&hx[y][x]&&hx[y+1][x]&&vy[y][x]&&vy[y][x+1];}
        private boolean complete(){for(int[]r:owner)for(int v:r)if(v==0)return false;return true;}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"DOTS AND BOXES","Click a line • Red: "+score1+"  Blue: "+score2);int s=52,ox=cx()-2*s,oy=72;for(int y=0;y<n;y++)for(int x=0;x<n;x++)c.fill(ox+x*s-3,oy+y*s-3,ox+x*s+3,oy+y*s+3,FG);for(int y=0;y<n;y++)for(int x=0;x<n-1;x++)if(hx[y][x])c.fill(ox+x*s,oy+y*s-2,ox+(x+1)*s,oy+y*s+2,FG);for(int y=0;y<n-1;y++)for(int x=0;x<n;x++)if(vy[x][y])c.fill(ox+x*s-2,oy+y*s,ox+x*s+2,oy+(y+1)*s,FG);for(int y=0;y<n-1;y++)for(int x=0;x<n-1;x++)if(owner[x][y]>0)c.fill(ox+x*s+7,oy+y*s+7,ox+(x+1)*s-7,oy+(y+1)*s-7,owner[x][y]==1?0x66E74C3C:0x6655AADD);}
        @Override public boolean mouseClicked(double mx,double my,int btn){if(btn!=0||finished)return true;int s=52,ox=cx()-2*s,oy=72;double rx=(mx-ox)/s,ry=(my-oy)/s;int ix=(int)Math.round(rx),iy=(int)Math.round(ry);if(Math.abs(ry-iy)<0.18&&ix>=0&&ix<n-1&&iy>=0&&iy<n&&!hx[iy][ix]){hx[iy][ix]=true;takeBoxes();return true;}if(Math.abs(rx-ix)<0.18&&ix>=0&&ix<n&&iy>=0&&iy<n-1&&!vy[ix][iy]){vy[ix][iy]=true;takeBoxes();}return true;}
        private void takeBoxes(){boolean got=false;for(int y=0;y<n-1;y++)for(int x=0;x<n-1;x++)if(owner[x][y]==0&&box(x,y)){owner[x][y]=turn;if(turn==1)score1++;else score2++;got=true;}if(complete()){finish(score1>=score2?score1*100:0);return;}if(!got)turn=3-turn;}
    }

    public static final class RockPaperScissors extends BaseGame {
        private int choice=-1;private final int ai=0;private final String[] names={"Rock","Paper","Scissors"};
        @Override public String id(){return "rock_paper_scissors";}@Override public String title(){return "Rock Paper Scissors";}@Override public String category(){return "Board";}
        @Override public void start(){choice=-1;}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"ROCK PAPER SCISSORS","Choose a move.");for(int i=0;i<3;i++){int x=cx()-150+i*100;c.fill(x,100,x+85,155,PANEL);c.drawCenteredTextWithShadow(net.minecraft.client.MinecraftClient.getInstance().textRenderer,Text.literal(names[i]),x+42,120,FG);}if(choice>=0)c.drawCenteredTextWithShadow(net.minecraft.client.MinecraftClient.getInstance().textRenderer,Text.literal("Computer: "+names[ai]),cx(),190,FG);}
        @Override public boolean mouseClicked(double mx,double my,int b){if(b!=0||finished)return true;for(int i=0;i<3;i++){int x=cx()-150+i*100;if(inside(mx,my,x,100,85,55)){choice=i;int r=choice==ai?0:(choice-ai+3)%3==1?100:0;finish(r);return true;}}return true;}
    }

    public static final class Hangman extends BaseGame {
        private final String[] words={"CREEPER","REDSTONE","DIAMOND","PICKAXE","ENDERMAN","CRAFTING"};private String word;private boolean[] hit;private int misses;private String guessed="";
        @Override public String id(){return "hangman";}@Override public String title(){return "Hangman";}@Override public String category(){return "Board";}
        @Override public void start(){word=words[random.nextInt(words.length)];hit=new boolean[word.length()];misses=0;guessed="";}
        private boolean done(){for(boolean b:hit)if(!b)return false;return true;}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"HANGMAN","Guess letters • Misses: "+misses+" / 6");StringBuilder s=new StringBuilder();for(int i=0;i<word.length();i++)s.append(hit[i]?word.charAt(i):'_').append(' ');c.drawCenteredTextWithShadow(net.minecraft.client.MinecraftClient.getInstance().textRenderer,Text.literal(s.toString()),cx(),100,FG);c.drawCenteredTextWithShadow(net.minecraft.client.gui.screen.Screen.class.cast(null)==null?net.minecraft.client.MinecraftClient.getInstance().textRenderer:net.minecraft.client.MinecraftClient.getInstance().textRenderer,Text.literal("Guessed: "+guessed),cx(),135,0xFFAAAAAA);}
        @Override public boolean mouseClicked(double x,double y,int b){return true;}
        @Override public void keyPressed(int k,int sc,int m){if(finished||k<GLFW.GLFW_KEY_A||k>GLFW.GLFW_KEY_Z)return;char ch=(char)('A'+k-GLFW.GLFW_KEY_A);if(guessed.indexOf(ch)>=0)return;guessed+=ch;boolean found=false;for(int i=0;i<word.length();i++)if(word.charAt(i)==ch){hit[i]=true;found=true;}if(!found)misses++;if(done())finishWin(1000);else if(misses>=6)finish(0);}
    }

    public static final class Battleship extends BaseGame {
        private final int n=6;private boolean[][] player=new boolean[n][n],shot=new boolean[n][n],hit=new boolean[n][n];private List<Integer> fleet=new ArrayList<>();private int enemyShips;
        @Override public String id(){return "battleship";}@Override public String title(){return "Battleship";}@Override public String category(){return "Board";}
        @Override public void start(){player=new boolean[n][n];shot=new boolean[n][n];hit=new boolean[n][n];fleet.clear();place(3);place(2);place(2);enemyShips=7;}
        private void place(int len){for(int tries=0;tries<500;tries++){int x=random.nextInt(n),y=random.nextInt(n),dx=random.nextBoolean()?1:0,dy=dx==1?0:1;if(x+dx*(len-1)>=n||y+dy*(len-1)>=n)continue;boolean ok=true;for(int i=0;i<len;i++)if(player[x+dx*i][y+dy*i])ok=false;if(ok){for(int i=0;i<len;i++)player[x+dx*i][y+dy*i]=true;return;}}}
        private void ai(){for(int t=0;t<100;t++){int x=random.nextInt(n),y=random.nextInt(n);if(!hit[x][y]){hit[x][y]=true;if(player[x][y])enemyShips--;return;}}}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"BATTLESHIP","Click squares to fire • Enemy ship cells remaining: "+enemyShips);int s=38,ox=cx()-3*s,oy=75;for(int y=0;y<n;y++)for(int x=0;x<n;x++){c.fill(ox+x*s,oy+y*s,ox+x*s+s-2,oy+y*s+s-2,shot[x][y]?(hit[x][y]?XCOL:0xFF777F86):0xFF2D6EA3);}}
        @Override public boolean mouseClicked(double mx,double my,int b){if(b!=0||finished)return true;int s=38,ox=cx()-3*s,oy=75,x=(int)((mx-ox)/s),y=(int)((my-oy)/s);if(x<0||y<0||x>=n||y>=n||shot[x][y])return true;shot[x][y]=true;boolean ship=random.nextInt(3)!=0;if(ship){enemyShips--;hit[x][y]=true;}if(enemyShips<=0){finishWin(1000);return true;}ai();return true;}
    }

    public static final class Chess extends BaseGame {
        private final int n=8;private char[][]b=new char[n][n];private int turn=0;private int sx=-1,sy=-1;private boolean vsAI=true;
        @Override public String id(){return "chess";}@Override public String title(){return "Chess";}@Override public String category(){return "Board";}
        @Override public void start(){String[]r={"rnbqkbnr","pppppppp","........","........","........","........","PPPPPPPP","RNBQKBNR"};for(int y=0;y<8;y++)b[y]=r[y].toCharArray();turn=0;status="White to move.";}
        private boolean white(char p){return p>='A'&&p<='Z';}private boolean empty(int x,int y){return b[y][x]=='.';}
        private boolean pseudo(int x,int y,int nx,int ny){char p=b[y][x],q=b[ny][nx];if(p=='.'||(white(p)==white(q)&&q!='.'))return false;int dx=nx-x,dy=ny-y,adx=Math.abs(dx),ady=Math.abs(dy);int dir=white(p)?-1:1;if(Character.toLowerCase(p)=='p'){if(dx==0&&q=='.'&&dy==dir)return true;if(dx==0&&q=='.'&&dy==2*dir&&((white(p)&&y==6)||(!white(p)&&y==1))&&b[y+dir][x]=='.')return true;return adx==1&&dy==dir&&q!='.';}if(Character.toLowerCase(p)=='n')return (adx==1&&ady==2)||(adx==2&&ady==1);if("brq".indexOf(Character.toLowerCase(p))>=0){if("bq".indexOf(Character.toLowerCase(p))>=0&&adx==ady&&clear(x,y,nx,ny))return true;if("rq".indexOf(Character.toLowerCase(p))>=0&&(dx==0||dy==0)&&clear(x,y,nx,ny))return true;}if(Character.toLowerCase(p)=='k')return Math.max(adx,ady)==1;return false;}
        private boolean clear(int x,int y,int nx,int ny){int dx=Integer.compare(nx,x),dy=Integer.compare(ny,y);x+=dx;y+=dy;while(x!=nx||y!=ny){if(b[y][x]!='.')return false;x+=dx;y+=dy;}return true;}
        private boolean inCheck(boolean w){int kx=-1,ky=-1;for(int y=0;y<8;y++)for(int x=0;x<8;x++)if(b[y][x]==(w?'K':'k')){kx=x;ky=y;}if(kx<0)return true;for(int y=0;y<8;y++)for(int x=0;x<8;x++)if(pseudo(x,y,kx,ky)&&white(b[y][x])!=w)return true;return false;}
        private boolean legal(int x,int y,int nx,int ny){if(!pseudo(x,y,nx,ny)||white(b[y][x])!=(turn==0))return false;char p=b[y][x],q=b[ny][nx];b[ny][nx]=p;b[y][x]='.';boolean bad=inCheck(turn==0);b[y][x]=p;b[ny][nx]=q;return !bad;}
        private boolean hasMove(boolean w){int t=turn;turn=w?0:1;for(int y=0;y<8;y++)for(int x=0;x<8;x++)for(int ny=0;ny<8;ny++)for(int nx=0;nx<8;nx++)if(legal(x,y,nx,ny)){turn=t;return true;}turn=t;return false;}
        private void ai(){List<int[]>m=new ArrayList<>();turn=1;for(int y=0;y<8;y++)for(int x=0;x<8;x++)for(int ny=0;ny<8;ny++)for(int nx=0;nx<8;nx++)if(legal(x,y,nx,ny))m.add(new int[]{x,y,nx,ny});if(!m.isEmpty()){int[]q=m.get(random.nextInt(m.size()));move(q[0],q[1],q[2],q[3]);}}
        private void move(int x,int y,int nx,int ny){char p=b[y][x];b[ny][nx]=p;b[y][x]='.';if(p=='P'&&ny==0)b[ny][nx]='Q';if(p=='p'&&ny==7)b[ny][nx]='q';}
        private void resolve(){boolean w=turn==0;if(!hasMove(w)){if(inCheck(w))finish(w?0:1000);else finish(500);}}
        @Override public void render(DrawContext c,int mx,int my,float d){drawHeader(c,"CHESS",status);int s=42,ox=cx()-4*s,oy=74;for(int y=0;y<8;y++)for(int x=0;x<8;x++){c.fill(ox+x*s,oy+y*s,ox+x*s+s,oy+y*s+s,(x+y)%2==0?0xFFE0C39A:0xFF8A5A3B);char p=b[y][x];if(p!='.')c.drawCenteredTextWithShadow(net.minecraft.client.MinecraftClient.getInstance().textRenderer,Text.literal(""+p),ox+x*s+20,oy+y*s+12,white(p)?FG:0xFF101010);}}
        @Override public boolean mouseClicked(double mx,double my,int btn){if(btn!=0||finished)return true;int s=42,ox=cx()-4*s,oy=74,x=(int)((mx-ox)/s),y=(int)((my-oy)/s);if(x<0||y<0||x>=8||y>=8)return true;if(sx<0&&b[y][x]!='.'&&white(b[y][x])==(turn==0)){sx=x;sy=y;}else if(sx>=0){if(legal(sx,sy,x,y)){move(sx,sy,x,y);turn=1-turn;sx=sy=-1;resolve();if(!finished&&vsAI){ai();turn=0;status="White to move.";resolve();}}else{sx=sy=-1;}}return true;}
    }
}
