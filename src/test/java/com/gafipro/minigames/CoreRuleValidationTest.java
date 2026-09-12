package com.gafipro.minigames;

import com.gafipro.minigames.core.GameCatalog;
import com.gafipro.minigames.core.GameFactory;
import com.gafipro.minigames.core.WordBank;
import com.gafipro.minigames.game.Game;
import com.gafipro.minigames.game.board.ConnectFourGame;
import com.gafipro.minigames.game.puzzle.Game2048;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class CoreRuleValidationTest {
    @Test
    void catalogIsExactlyTheImplemented44Games() {
        assertEquals(44, GameCatalog.all().size());
        Set<String> ids = new HashSet<>();
        for (GameCatalog.Entry entry : GameCatalog.all()) {
            assertTrue(ids.add(entry.id()), "duplicate catalog id: " + entry.id());
            Game game = assertDoesNotThrow(() -> GameFactory.create(entry.id()));
            assertEquals(entry.id(), game.id());
            assertEquals(entry.title(), game.title());
            assertEquals(GameCatalog.displayCategory(entry.category()), game.category(),
                    "category contract for " + entry.id());
        }
        assertEquals(ids, GameFactory.supportedIds(), "catalog and factory IDs must be exactly one-to-one");
    }

    @Test
    void factoryRejectsUnknownIds() {
        assertThrows(IllegalArgumentException.class, () -> GameFactory.create("definitely_not_a_game"));
    }

    @Test
    void everyFactoryIdConstructsTheExpectedCatalogEntry() {
        var catalog = GameCatalog.all().stream().collect(Collectors.toMap(GameCatalog.Entry::id, e -> e));
        for (String id : GameFactory.supportedIds()) {
            GameCatalog.Entry entry = catalog.get(id);
            assertNotNull(entry, "factory-only id: " + id);
            Game game = assertDoesNotThrow(() -> GameFactory.create(id));
            assertEquals(id, game.id());
            assertEquals(entry.title(), game.title());
            assertEquals(GameCatalog.displayCategory(entry.category()), game.category());
        }
    }

    @Test
    void twentyFortyEightDoesNotMergeAResultTwice() throws Exception {
        Game2048 game = new Game2048();
        Method mergeLine = Game2048.class.getDeclaredMethod("mergeLine", int[].class);
        mergeLine.setAccessible(true);
        assertArrayEquals(new int[]{4, 4, 0, 0}, (int[]) mergeLine.invoke(game, (Object) new int[]{2, 2, 2, 2}));
    }

    @Test
    void twentyFortyEightOnlyMergesAdjacentCompactedPairs() throws Exception {
        Game2048 game = new Game2048();
        Method mergeLine = Game2048.class.getDeclaredMethod("mergeLine", int[].class);
        mergeLine.setAccessible(true);
        assertArrayEquals(new int[]{4, 2, 0, 0}, (int[]) mergeLine.invoke(game, (Object) new int[]{2, 0, 2, 2}));
    }

    @Test
    void connectFourDetectsDiagonalWins() throws Exception {
        Method hasWon = ConnectFourGame.class.getDeclaredMethod("hasWon", int[][].class, int.class);
        hasWon.setAccessible(true);
        int[][] board = {{0,0,0,1,0,0,0},{0,0,1,2,0,0,0},{0,1,2,2,0,0,0},{1,2,2,1,0,0,0},{0,0,0,0,0,0,0},{0,0,0,0,0,0,0}};
        assertTrue((boolean) hasWon.invoke(null, (Object) board, 1));
    }

    @Test
    void connectFourDoesNotAcceptAThreeInARow() throws Exception {
        Method hasWon = ConnectFourGame.class.getDeclaredMethod("hasWon", int[][].class, int.class);
        hasWon.setAccessible(true);
        int[][] board = {{0,0,0,0,0,0,0},{0,0,0,0,0,0,0},{0,0,0,0,0,0,0},{0,0,0,1,0,0,0},{0,0,1,1,0,0,0},{0,0,0,0,0,0,0}};
        assertFalse((boolean) hasWon.invoke(null, (Object) board, 1));
    }

    @Test
    void wordBankIsSubstantialAndSanitized() {
        assertTrue(WordBank.all().size() >= 64);
        assertTrue(WordBank.all().stream().allMatch(word -> word.matches("[A-Z]{3,18}")));
    }
}
