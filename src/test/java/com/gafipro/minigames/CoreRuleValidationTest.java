package com.gafipro.minigames;

import com.gafipro.minigames.core.GameCatalog;
import com.gafipro.minigames.core.GameFactory;
import com.gafipro.minigames.core.WordBank;
import com.gafipro.minigames.game.Game;
import com.gafipro.minigames.game.puzzle.Game2048;
import com.gafipro.minigames.game.board.ConnectFourGame;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CoreRuleValidationTest {
    @Test
    void catalogIsExactlyTheImplemented44Games() {
        assertEquals(44, GameCatalog.all().size(), "catalog count must remain synchronized with the launcher");
        Set<String> ids = new HashSet<>();
        for (GameCatalog.Entry entry : GameCatalog.all()) {
            assertTrue(ids.add(entry.id()), "duplicate catalog id: " + entry.id());
            Game game = assertDoesNotThrow(() -> GameFactory.create(entry.id()));
            assertEquals(entry.id(), game.id(), "factory routed the wrong game for " + entry.id());
            assertEquals(entry.title(), game.title(), "factory title mismatch for " + entry.id());
            assertEquals(entry.category().name(), game.category().trim().toUpperCase(Locale.ROOT), "factory category mismatch for " + entry.id());
        }
    }

    @Test
    void twentyFortyEightDoesNotMergeAResultTwice() throws Exception {
        Game2048 game = new Game2048();
        Method mergeLine = Game2048.class.getDeclaredMethod("mergeLine", int[].class);
        mergeLine.setAccessible(true);
        int[] result = (int[]) mergeLine.invoke(game, (Object) new int[]{2, 2, 2, 2});
        assertArrayEquals(new int[]{4, 4, 0, 0}, result);
    }

    @Test
    void twentyFortyEightOnlyMergesAdjacentCompactedPairs() throws Exception {
        Game2048 game = new Game2048();
        Method mergeLine = Game2048.class.getDeclaredMethod("mergeLine", int[].class);
        mergeLine.setAccessible(true);
        int[] result = (int[]) mergeLine.invoke(game, (Object) new int[]{2, 0, 2, 2});
        assertArrayEquals(new int[]{4, 2, 0, 0}, result);
    }

    @Test
    void connectFourDetectsDiagonalWins() throws Exception {
        Method hasWon = ConnectFourGame.class.getDeclaredMethod("hasWon", int[][].class, int.class);
        hasWon.setAccessible(true);
        int[][] board = {
                {0,0,0,1,0,0,0},
                {0,0,1,2,0,0,0},
                {0,1,2,2,0,0,0},
                {1,2,2,1,0,0,0},
                {0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0}
        };
        assertTrue((boolean) hasWon.invoke(null, (Object) board, 1));
    }

    @Test
    void connectFourDoesNotAcceptAThreeInARow() throws Exception {
        Method hasWon = ConnectFourGame.class.getDeclaredMethod("hasWon", int[][].class, int.class);
        hasWon.setAccessible(true);
        int[][] board = {
                {0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0},
                {0,0,0,1,0,0,0},
                {0,0,1,1,0,0,0},
                {0,0,0,0,0,0,0}
        };
        assertFalse((boolean) hasWon.invoke(null, (Object) board, 1));
    }

    @Test
    void wordBankIsSubstantialAndSanitized() {
        assertTrue(WordBank.all().size() >= 64);
        assertTrue(WordBank.all().stream().allMatch(word -> word.matches("[A-Z]{3,18}")));
    }
}
