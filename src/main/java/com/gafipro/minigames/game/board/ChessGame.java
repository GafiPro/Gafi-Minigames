package com.gafipro.minigames.game.board;

import com.gafipro.minigames.game.BaseGame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Complete local chess rules engine with legal-move validation and a lightweight selectable AI.
 * Board coordinates are [row][file], row 0 = Black's back rank, row 7 = White's back rank.
 */
public final class ChessGame extends BaseGame {
    private static final int SIZE = 8;
    private static final int WHITE = 1;
    private static final int BLACK = -1;

    private final char[][] board = new char[SIZE][SIZE];
    private final Map<String, Integer> repetition = new HashMap<>();
    private boolean whiteTurn;
    private int selectedRow = -1;
    private int selectedCol = -1;
    private int enPassantRow = -1;
    private int enPassantCol = -1;
    private boolean whiteKingMoved;
    private boolean blackKingMoved;
    private boolean whiteKingRookMoved;
    private boolean whiteQueenRookMoved;
    private boolean blackKingRookMoved;
    private boolean blackQueenRookMoved;
    private int halfmoveClock;
    private Difficulty difficulty = Difficulty.NORMAL;
    private int aiDelay;

    private enum Difficulty { EASY, NORMAL, HARD }

    private record Move(int fr, int fc, int tr, int tc, char promotion, boolean castleKingSide,
                        boolean castleQueenSide, boolean enPassant) {
        static Move normal(int fr, int fc, int tr, int tc) {
            return new Move(fr, fc, tr, tc, '\0', false, false, false);
        }
    }

    private record Snapshot(char[][] board, boolean whiteTurn, int epRow, int epCol,
                            boolean wkm, boolean bkm, boolean wkr, boolean wqr, boolean bkr,
                            boolean bqr, int halfmoveClock) {
    }

    @Override public String id() { return "chess"; }
    @Override public String title() { return "Chess"; }
    @Override public String category() { return "Board"; }

    @Override
    public void start() {
        setupBoard();
        whiteTurn = true;
        selectedRow = -1;
        selectedCol = -1;
        enPassantRow = enPassantCol = -1;
        whiteKingMoved = blackKingMoved = false;
        whiteKingRookMoved = whiteQueenRookMoved = false;
        blackKingRookMoved = blackQueenRookMoved = false;
        halfmoveClock = 0;
        aiDelay = 0;
        repetition.clear();
        repetition.merge(positionKey(), 1, Integer::sum);
        status = "Normal • White to move • 1 Easy  2 Normal  3 Hard";
    }

    private void setupBoard() {
        String[] rows = {
                "rnbqkbnr",
                "pppppppp",
                "........",
                "........",
                "........",
                "........",
                "PPPPPPPP",
                "RNBQKBNR"
        };
        for (int r = 0; r < SIZE; r++) for (int c = 0; c < SIZE; c++) board[r][c] = rows[r].charAt(c);
    }

    @Override
    public void tick() {
        super.tick();
        if (!finished && !whiteTurn && --aiDelay <= 0) aiMove();
    }

    private void aiMove() {
        List<Move> legal = legalMoves(false);
        if (legal.isEmpty()) { resolvePosition(); return; }
        Move chosen = switch (difficulty) {
            case EASY -> legal.get(random.nextInt(legal.size()));
            case NORMAL -> chooseBest(legal, 2);
            case HARD -> chooseBest(legal, 3);
        };
        applyAndResolve(chosen);
    }

    private Move chooseBest(List<Move> legal, int depth) {
        int best = Integer.MIN_VALUE;
        List<Move> bestMoves = new ArrayList<>();
        List<Move> ordered = orderMoves(legal);
        for (Move move : ordered) {
            Snapshot snapshot = snapshot();
            apply(move);
            int score = minimax(depth - 1, Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 1);
            restore(snapshot);
            if (score > best) {
                best = score;
                bestMoves.clear();
                bestMoves.add(move);
            } else if (score == best) {
                bestMoves.add(move);
            }
        }
        return bestMoves.get(random.nextInt(bestMoves.size()));
    }

    private int minimax(int depth, int alpha, int beta) {
        List<Move> legal = legalMoves(whiteTurn);
        if (legal.isEmpty()) {
            if (isInCheck(whiteTurn)) return whiteTurn ? -100000 - depth : 100000 + depth;
            return 0;
        }
        if (isThreefold() || halfmoveClock >= 100 || insufficientMaterial()) return 0;
        if (depth <= 0) return evaluation();

        boolean maximizing = !whiteTurn;
        int best = maximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for (Move move : orderMoves(legal)) {
            Snapshot snapshot = snapshot();
            apply(move);
            int score = minimax(depth - 1, alpha, beta);
            restore(snapshot);
            if (maximizing) {
                best = Math.max(best, score);
                alpha = Math.max(alpha, best);
            } else {
                best = Math.min(best, score);
                beta = Math.min(beta, best);
            }
            if (beta <= alpha) break;
        }
        return best;
    }

    private List<Move> orderMoves(List<Move> moves) {
        List<Move> ordered = new ArrayList<>(moves);
        ordered.sort((a, b) -> Integer.compare(movePriority(b), movePriority(a)));
        return ordered;
    }

    private int movePriority(Move move) {
        char target = board[move.tr][move.tc];
        int value = pieceValue(Character.toLowerCase(target));
        if (move.enPassant) value = 100;
        if (move.promotion != '\0') value += 800;
        if (move.castleKingSide || move.castleQueenSide) value += 50;
        return value;
    }

    private int evaluation() {
        int total = 0;
        for (int r = 0; r < SIZE; r++) for (int c = 0; c < SIZE; c++) {
            char p = board[r][c];
            if (p == '.') continue;
            int value = pieceValue(Character.toLowerCase(p));
            int color = Character.isUpperCase(p) ? WHITE : BLACK;
            int centerBonus = (int) Math.round(3.0 - (Math.abs(3.5 - r) + Math.abs(3.5 - c)) * 0.35);
            total += color * (value + centerBonus);
        }
        total += (whiteTurn ? -2 : 2);
        return total;
    }

    private int pieceValue(char piece) {
        return switch (piece) {
            case 'p' -> 100;
            case 'n', 'b' -> 320;
            case 'r' -> 500;
            case 'q' -> 900;
            case 'k' -> 20000;
            default -> 0;
        };
    }

    private void applyAndResolve(Move move) {
        apply(move);
        markMove();
        selectedRow = selectedCol = -1;
        resolvePosition();
        if (!finished) {
            status = whiteTurn ? "Your turn • White" : "Thinking...";
            if (!whiteTurn) aiDelay = difficulty == Difficulty.HARD ? 2 : 5;
        }
    }

    private void resolvePosition() {
        List<Move> legal = legalMoves(whiteTurn);
        if (legal.isEmpty()) {
            if (isInCheck(whiteTurn)) {
                if (whiteTurn) { status = "Checkmate — Black wins."; finish(0); }
                else { status = "Checkmate — You win!"; finishWin(1000 + Math.max(0, 1000 - halfmoveClock)); }
            } else {
                status = "Stalemate — draw.";
                finishDraw(500);
            }
            return;
        }
        if (isThreefold()) { status = "Draw — threefold repetition."; finishDraw(500); return; }
        if (halfmoveClock >= 100) { status = "Draw — fifty-move rule."; finishDraw(500); return; }
        if (insufficientMaterial()) { status = "Draw — insufficient material."; finishDraw(500); return; }
        if (isInCheck(whiteTurn)) status = (whiteTurn ? "White" : "Black") + " is in check.";
    }

    private boolean isThreefold() {
        return repetition.getOrDefault(positionKey(), 0) >= 3;
    }

    private boolean insufficientMaterial() {
        int bishops = 0, knights = 0, others = 0;
        int bishopColor = -1;
        for (int r = 0; r < SIZE; r++) for (int c = 0; c < SIZE; c++) {
            char p = board[r][c];
            if (p == '.' || Character.toLowerCase(p) == 'k') continue;
            char type = Character.toLowerCase(p);
            if (type == 'b') { bishops++; bishopColor = (r + c) & 1; }
            else if (type == 'n') knights++;
            else others++;
        }
        if (others > 0) return false;
        if (bishops + knights <= 1) return true;
        if (bishops == 2 && knights == 0) {
            int first = -1;
            for (int r = 0; r < SIZE; r++) for (int c = 0; c < SIZE; c++) if (Character.toLowerCase(board[r][c]) == 'b') {
                if (first < 0) first = (r + c) & 1;
                else if (first != ((r + c) & 1)) return false;
            }
            return true;
        }
        return false;
    }

    private List<Move> legalMoves(boolean forWhite) {
        List<Move> pseudo = pseudoMoves(forWhite);
        List<Move> legal = new ArrayList<>(pseudo.size());
        for (Move move : pseudo) {
            Snapshot snapshot = snapshot();
            apply(move);
            boolean illegal = isInCheck(forWhite);
            restore(snapshot);
            if (!illegal) legal.add(move);
        }
        return legal;
    }

    private List<Move> pseudoMoves(boolean forWhite) {
        List<Move> moves = new ArrayList<>();
        int color = forWhite ? WHITE : BLACK;
        for (int r = 0; r < SIZE; r++) for (int c = 0; c < SIZE; c++) {
            char p = board[r][c];
            if (p == '.' || pieceColor(p) != color) continue;
            char type = Character.toLowerCase(p);
            switch (type) {
                case 'p' -> pawnMoves(r, c, forWhite, moves);
                case 'n' -> knightMoves(r, c, forWhite, moves);
                case 'b' -> slideMoves(r, c, forWhite, moves, new int[][]{{1,1},{1,-1},{-1,1},{-1,-1}});
                case 'r' -> slideMoves(r, c, forWhite, moves, new int[][]{{1,0},{-1,0},{0,1},{0,-1}});
                case 'q' -> slideMoves(r, c, forWhite, moves, new int[][]{{1,1},{1,-1},{-1,1},{-1,-1},{1,0},{-1,0},{0,1},{0,-1}});
                case 'k' -> kingMoves(r, c, forWhite, moves);
            }
        }
        return moves;
    }

    private void pawnMoves(int r, int c, boolean white, List<Move> out) {
        int dir = white ? -1 : 1;
        int start = white ? 6 : 1;
        int promotionRow = white ? 0 : 7;
        int nr = r + dir;
        if (inside(nr, c) && board[nr][c] == '.') {
            addPawnMove(out, r, c, nr, c, promotionRow);
            if (r == start && board[r + 2 * dir][c] == '.') out.add(Move.normal(r, c, r + 2 * dir, c));
        }
        for (int dc : new int[]{-1, 1}) {
            int nc = c + dc;
            if (!inside(nr, nc)) continue;
            if (board[nr][nc] != '.' && pieceColor(board[nr][nc]) == (white ? BLACK : WHITE)) {
                addPawnMove(out, r, c, nr, nc, promotionRow);
            } else if (nr == enPassantRow && nc == enPassantCol) {
                out.add(new Move(r, c, nr, nc, '\0', false, false, true));
            }
        }
    }

    private void addPawnMove(List<Move> out, int fr, int fc, int tr, int tc, int promotionRow) {
        if (tr == promotionRow) for (char p : new char[]{'q','r','b','n'}) out.add(new Move(fr, fc, tr, tc, p, false, false, false));
        else out.add(Move.normal(fr, fc, tr, tc));
    }

    private void knightMoves(int r, int c, boolean white, List<Move> out) {
        int[][] ds = {{2,1},{2,-1},{-2,1},{-2,-1},{1,2},{1,-2},{-1,2},{-1,-2}};
        for (int[] d : ds) addQuietOrCapture(r, c, r + d[0], c + d[1], white, out);
    }

    private void slideMoves(int r, int c, boolean white, List<Move> out, int[][] dirs) {
        int enemy = white ? BLACK : WHITE;
        for (int[] d : dirs) {
            int nr = r + d[0], nc = c + d[1];
            while (inside(nr, nc)) {
                if (board[nr][nc] == '.') out.add(Move.normal(r, c, nr, nc));
                else {
                    if (pieceColor(board[nr][nc]) == enemy && Character.toLowerCase(board[nr][nc]) != 'k') out.add(Move.normal(r, c, nr, nc));
                    break;
                }
                nr += d[0]; nc += d[1];
            }
        }
    }

    private void kingMoves(int r, int c, boolean white, List<Move> out) {
        for (int dr = -1; dr <= 1; dr++) for (int dc = -1; dc <= 1; dc++) {
            if (dr == 0 && dc == 0) continue;
            addQuietOrCapture(r, c, r + dr, c + dc, white, out);
        }
        if (canCastle(white, true)) out.add(new Move(r, c, r, c + 2, '\0', true, false, false));
        if (canCastle(white, false)) out.add(new Move(r, c, r, c - 2, '\0', false, true, false));
    }

    private boolean canCastle(boolean white, boolean kingSide) {
        int r = white ? 7 : 0;
        char king = white ? 'K' : 'k';
        char rook = white ? 'R' : 'r';
        if (board[r][4] != king) return false;
        if (white ? whiteKingMoved : blackKingMoved) return false;
        if (kingSide) {
            if (board[r][7] != rook || (white ? whiteKingRookMoved : blackKingRookMoved)) return false;
            if (board[r][5] != '.' || board[r][6] != '.') return false;
            return !isSquareAttacked(r, 4, !white) && !isSquareAttacked(r, 5, !white) && !isSquareAttacked(r, 6, !white);
        }
        if (board[r][0] != rook || (white ? whiteQueenRookMoved : blackQueenRookMoved)) return false;
        if (board[r][1] != '.' || board[r][2] != '.' || board[r][3] != '.') return false;
        return !isSquareAttacked(r, 4, !white) && !isSquareAttacked(r, 3, !white) && !isSquareAttacked(r, 2, !white);
    }

    private void addQuietOrCapture(int fr, int fc, int tr, int tc, boolean white, List<Move> out) {
        if (!inside(tr, tc)) return;
        char target = board[tr][tc];
        if (target == '.' || (pieceColor(target) == (white ? BLACK : WHITE) && Character.toLowerCase(target) != 'k')) out.add(Move.normal(fr, fc, tr, tc));
    }

    private boolean isInCheck(boolean white) {
        char king = white ? 'K' : 'k';
        for (int r = 0; r < SIZE; r++) for (int c = 0; c < SIZE; c++) if (board[r][c] == king) return isSquareAttacked(r, c, !white);
        return true;
    }

    private boolean isSquareAttacked(int r, int c, boolean byWhite) {
        int color = byWhite ? WHITE : BLACK;
        int pawnDir = byWhite ? -1 : 1;
        int pawnRow = r - pawnDir;
        for (int dc : new int[]{-1,1}) if (inside(pawnRow, c + dc)) {
            char p = board[pawnRow][c + dc];
            if (p == (byWhite ? 'P' : 'p')) return true;
        }
        int[][] knight = {{2,1},{2,-1},{-2,1},{-2,-1},{1,2},{1,-2},{-1,2},{-1,-2}};
        for (int[] d : knight) if (inside(r + d[0], c + d[1]) && board[r + d[0]][c + d[1]] == (byWhite ? 'N' : 'n')) return true;
        int[][] diagonals = {{1,1},{1,-1},{-1,1},{-1,-1}};
        for (int[] d : diagonals) {
            int nr = r + d[0], nc = c + d[1];
            while (inside(nr, nc)) {
                char p = board[nr][nc];
                if (p != '.') {
                    if (pieceColor(p) == color && (Character.toLowerCase(p) == 'b' || Character.toLowerCase(p) == 'q')) return true;
                    break;
                }
                nr += d[0]; nc += d[1];
            }
        }
        int[][] straights = {{1,0},{-1,0},{0,1},{0,-1}};
        for (int[] d : straights) {
            int nr = r + d[0], nc = c + d[1];
            while (inside(nr, nc)) {
                char p = board[nr][nc];
                if (p != '.') {
                    if (pieceColor(p) == color && (Character.toLowerCase(p) == 'r' || Character.toLowerCase(p) == 'q')) return true;
                    break;
                }
                nr += d[0]; nc += d[1];
            }
        }
        for (int dr = -1; dr <= 1; dr++) for (int dc = -1; dc <= 1; dc++) if (dr != 0 || dc != 0) {
            if (inside(r + dr, c + dc) && board[r + dr][c + dc] == (byWhite ? 'K' : 'k')) return true;
        }
        return false;
    }

    private void apply(Move move) {
        char piece = board[move.fr][move.fc];
        char target = board[move.tr][move.tc];
        boolean white = Character.isUpperCase(piece);
        enPassantRow = enPassantCol = -1;

        if (move.enPassant) board[move.tr + (white ? 1 : -1)][move.tc] = '.';
        board[move.tr][move.tc] = piece;
        board[move.fr][move.fc] = '.';

        if (move.promotion != '\0') board[move.tr][move.tc] = white ? Character.toUpperCase(move.promotion) : move.promotion;

        if (move.castleKingSide) {
            board[move.tr][5] = white ? 'R' : 'r';
            board[move.tr][7] = '.';
        } else if (move.castleQueenSide) {
            board[move.tr][3] = white ? 'R' : 'r';
            board[move.tr][0] = '.';
        }

        if (Character.toLowerCase(piece) == 'k') {
            if (white) whiteKingMoved = true; else blackKingMoved = true;
        } else if (piece == 'R') {
            if (move.fr == 7 && move.fc == 7) whiteKingRookMoved = true;
            if (move.fr == 7 && move.fc == 0) whiteQueenRookMoved = true;
        } else if (piece == 'r') {
            if (move.fr == 0 && move.fc == 7) blackKingRookMoved = true;
            if (move.fr == 0 && move.fc == 0) blackQueenRookMoved = true;
        }
        if (target == 'R') {
            if (move.tr == 7 && move.tc == 7) whiteKingRookMoved = true;
            if (move.tr == 7 && move.tc == 0) whiteQueenRookMoved = true;
        } else if (target == 'r') {
            if (move.tr == 0 && move.tc == 7) blackKingRookMoved = true;
            if (move.tr == 0 && move.tc == 0) blackQueenRookMoved = true;
        }

        if (Character.toLowerCase(piece) == 'p' && Math.abs(move.tr - move.fr) == 2) {
            enPassantRow = (move.tr + move.fr) / 2;
            enPassantCol = move.fc;
        }
        halfmoveClock = (Character.toLowerCase(piece) == 'p' || target != '.' || move.enPassant) ? 0 : halfmoveClock + 1;
        whiteTurn = !whiteTurn;
        repetition.merge(positionKey(), 1, Integer::sum);
    }

    private Snapshot snapshot() {
        char[][] copy = new char[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) copy[r] = board[r].clone();
        return new Snapshot(copy, whiteTurn, enPassantRow, enPassantCol, whiteKingMoved, blackKingMoved,
                whiteKingRookMoved, whiteQueenRookMoved, blackKingRookMoved, blackQueenRookMoved, halfmoveClock);
    }

    private void restore(Snapshot s) {
        for (int r = 0; r < SIZE; r++) System.arraycopy(s.board[r], 0, board[r], 0, SIZE);
        whiteTurn = s.whiteTurn;
        enPassantRow = s.epRow;
        enPassantCol = s.epCol;
        whiteKingMoved = s.wkm;
        blackKingMoved = s.bkm;
        whiteKingRookMoved = s.wkr;
        whiteQueenRookMoved = s.wqr;
        blackKingRookMoved = s.bkr;
        blackQueenRookMoved = s.bqr;
        halfmoveClock = s.halfmoveClock;
    }

    private String positionKey() {
        StringBuilder b = new StringBuilder(80);
        for (int r = 0; r < SIZE; r++) for (int c = 0; c < SIZE; c++) b.append(board[r][c]);
        b.append(whiteTurn ? 'w' : 'b');
        b.append(enPassantRow).append(',').append(enPassantCol);
        b.append(whiteKingMoved ? '1' : '0').append(blackKingMoved ? '1' : '0')
                .append(whiteKingRookMoved ? '1' : '0').append(whiteQueenRookMoved ? '1' : '0')
                .append(blackKingRookMoved ? '1' : '0').append(blackQueenRookMoved ? '1' : '0');
        return b.toString();
    }

    private int pieceColor(char p) {
        if (p == '.') return 0;
        return Character.isUpperCase(p) ? WHITE : BLACK;
    }

    private boolean inside(int r, int c) { return r >= 0 && r < SIZE && c >= 0 && c < SIZE; }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        var mc = MinecraftClient.getInstance();
        var tr = mc.textRenderer;
        drawHeader(c, "CHESS", status);
        int cell = Math.min(58, Math.max(34, (mc.getWindow().getScaledHeight() - 155) / 8));
        int size = cell * 8;
        int sx = cx() - size / 2, sy = 76;
        for (int r = 0; r < 8; r++) for (int col = 0; col < 8; col++) {
            int x = sx + col * cell, y = sy + r * cell;
            boolean light = ((r + col) & 1) == 0;
            int tile = light ? 0xFFD9C7A3 : 0xFF7A5538;
            if (r == selectedRow && col == selectedCol) tile = 0xFFCCAA33;
            c.fill(x, y, x + cell, y + cell, tile);
            char p = board[r][col];
            if (p != '.') {
                String symbol = pieceSymbol(p);
                int color = Character.isUpperCase(p) ? 0xFFFFFFFF : 0xFF222222;
                c.drawCenteredTextWithShadow(tr, Text.literal(symbol), x + cell / 2, y + cell / 2 - 5, color);
            }
        }
        c.drawCenteredTextWithShadow(tr, Text.literal("Click a piece, then a legal square • R Restart • 1/2/3 Difficulty"), cx(), sy + size + 12, 0xFFAAAAAA);
    }

    private String pieceSymbol(char p) {
        return switch (p) {
            case 'K' -> "♔"; case 'Q' -> "♕"; case 'R' -> "♖"; case 'B' -> "♗"; case 'N' -> "♘"; case 'P' -> "♙";
            case 'k' -> "♚"; case 'q' -> "♛"; case 'r' -> "♜"; case 'b' -> "♝"; case 'n' -> "♞"; case 'p' -> "♟";
            default -> "";
        };
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0 || finished || !whiteTurn) return true;
        MinecraftClient mc = MinecraftClient.getInstance();
        int cell = Math.min(58, Math.max(34, (mc.getWindow().getScaledHeight() - 155) / 8));
        int size = cell * 8;
        int sx = cx() - size / 2, sy = 76;
        int col = (int) ((mx - sx) / cell), row = (int) ((my - sy) / cell);
        if (!inside(row, col)) return true;

        if (selectedRow < 0) {
            if (pieceColor(board[row][col]) == WHITE) {
                selectedRow = row;
                selectedCol = col;
            }
            return true;
        }

        List<Move> legal = legalMoves(true);
        for (Move move : legal) {
            if (move.fr == selectedRow && move.fc == selectedCol && move.tr == row && move.tc == col) {
                applyAndResolve(move);
                return true;
            }
        }
        if (pieceColor(board[row][col]) == WHITE) {
            selectedRow = row;
            selectedCol = col;
        } else {
            selectedRow = selectedCol = -1;
        }
        return true;
    }

    @Override
    public void keyPressed(int key, int scan, int modifiers) {
        if (key == GLFW.GLFW_KEY_R) { begin(); return; }
        if (finished) return;
        if (key == GLFW.GLFW_KEY_1) { difficulty = Difficulty.EASY; begin(); }
        else if (key == GLFW.GLFW_KEY_2) { difficulty = Difficulty.NORMAL; begin(); }
        else if (key == GLFW.GLFW_KEY_3) { difficulty = Difficulty.HARD; begin(); }
    }
}
