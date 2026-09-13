package com.gafipro.minigames;

import com.gafipro.minigames.core.GameCatalog;
import com.gafipro.minigames.core.GameFactory;
import com.gafipro.minigames.core.WordBank;
import com.gafipro.minigames.game.Game;
import com.gafipro.minigames.game.GameState;
import com.gafipro.minigames.game.arcade.ArcadeCollection;
import com.gafipro.minigames.game.arcade.WhackAMoleGame;
import com.gafipro.minigames.game.board.ConnectFourGame;
import com.gafipro.minigames.game.puzzle.Game2048;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CoreRuleValidationTest {
    @Test void catalogContainsExactly44UniqueIds(){assertEquals(44,GameCatalog.all().size());Set<String>ids=new HashSet<>();for(GameCatalog.Entry e:GameCatalog.all())assertTrue(ids.add(e.id()),"duplicate catalog id: "+e.id());}

    @TestFactory
    Stream<DynamicTest> everyCatalogEntryMatchesItsFactoryImplementation(){
        return GameCatalog.all().stream().map(entry->DynamicTest.dynamicTest("catalog " + entry.id(),()->{
            Game game=assertDoesNotThrow(()->GameFactory.create(entry.id()));
            assertEquals(entry.id(),game.id());
            assertEquals(entry.title(),game.title());
            assertEquals(GameCatalog.displayCategory(entry.category()),game.category());
        }));
    }

    @TestFactory
    Stream<DynamicTest> everyGameSurvivesARealLifecycleReset(){
        return GameCatalog.all().stream().map(entry->DynamicTest.dynamicTest("lifecycle " + entry.id(),()->{
            Game game=GameFactory.create(entry.id());
            assertTrue(game instanceof com.gafipro.minigames.game.BaseGame,"game must use the shared lifecycle");
            var base=(com.gafipro.minigames.game.BaseGame)game;
            assertDoesNotThrow(base::begin);
            assertEquals(GameState.PLAYING,base.state(),"game should enter PLAYING after begin");
            assertEquals(0,base.score());
            assertEquals(0,base.metrics().moves());
            base.begin();
            assertEquals(GameState.PLAYING,base.state(),"second begin must create a fresh playable session");
            assertEquals(0,base.score());
            assertEquals(0,base.metrics().moves());
            base.finishForTesting();
        }));
    }

    @Test void catalogAndFactoryIdsAreExactlyOneToOne(){assertEquals(new HashSet<>(GameCatalog.all().stream().map(GameCatalog.Entry::id).collect(Collectors.toSet())),new HashSet<>(GameFactory.supportedIds()));}
    @Test void factoryRejectsUnknownIds(){assertThrows(IllegalArgumentException.class,()->GameFactory.create("definitely_not_a_game"));}

    private static int[] merge(Game2048 game,int[] line)throws Exception{Method m=Game2048.class.getDeclaredMethod("mergeLine",int[].class);m.setAccessible(true);return (int[])m.invoke(game,(Object)line);}
    @Test void twentyFortyEightDoesNotMergeAResultTwice()throws Exception{assertArrayEquals(new int[]{4,4,0,0},merge(new Game2048(),new int[]{2,2,2,2}));}
    @Test void twentyFortyEightOnlyMergesAdjacentCompactedPairs()throws Exception{assertArrayEquals(new int[]{4,2,0,0},merge(new Game2048(),new int[]{2,0,2,2}));}
    @Test void twentyFortyEightMergesLongRunsCorrectly()throws Exception{assertArrayEquals(new int[]{8,4,0,0},merge(new Game2048(),new int[]{4,4,4,0}));assertArrayEquals(new int[]{16,16,0,0},merge(new Game2048(),new int[]{8,8,16,0}));}

    @Test void connectFourDetectsDiagonalWins()throws Exception{Method m=ConnectFourGame.class.getDeclaredMethod("hasWon",int[][].class,int.class);m.setAccessible(true);int[][]b={{0,0,0,1,0,0,0},{0,0,1,2,0,0,0},{0,1,2,2,0,0,0},{1,2,2,1,0,0,0},{0,0,0,0,0,0,0},{0,0,0,0,0,0,0}};assertTrue((boolean)m.invoke(null,(Object)b,1));}
    @Test void connectFourDoesNotAcceptAThreeInARow()throws Exception{Method m=ConnectFourGame.class.getDeclaredMethod("hasWon",int[][].class,int.class);m.setAccessible(true);int[][]b={{0,0,0,0,0,0,0},{0,0,0,0,0,0,0},{0,0,0,0,0,0,0},{0,0,0,1,0,0,0},{0,0,1,1,0,0,0},{0,0,0,0,0,0,0}};assertFalse((boolean)m.invoke(null,(Object)b,1));}
    @Test void wordBankIsSubstantialAndSanitized(){assertTrue(WordBank.all().size()>=64);assertTrue(WordBank.all().stream().allMatch(word->word.matches("[A-Z]{3,18}")));}
    @Test void fastClickRequiresAllTwentyTargets()throws Exception{Method m=ArcadeCollection.FastClick.class.getDeclaredMethod("completed",int.class);m.setAccessible(true);assertFalse((boolean)m.invoke(null,19));assertTrue((boolean)m.invoke(null,20));assertTrue((boolean)m.invoke(null,25));}
    @Test void whackAMoleNeedsAtLeastOneRealHitToWin()throws Exception{Method m=WhackAMoleGame.class.getDeclaredMethod("completed",int.class);m.setAccessible(true);assertFalse((boolean)m.invoke(null,0));assertTrue((boolean)m.invoke(null,1));}
}
