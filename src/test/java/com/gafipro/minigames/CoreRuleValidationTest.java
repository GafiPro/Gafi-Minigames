package com.gafipro.minigames;

import com.gafipro.minigames.core.GameCatalog;
import com.gafipro.minigames.core.GameFactory;
import com.gafipro.minigames.core.WordBank;
import com.gafipro.minigames.game.Game;
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

    @Test void catalogAndFactoryIdsAreExactlyOneToOne(){assertEquals(new HashSet<>(GameCatalog.all().stream().map(GameCatalog.Entry::id).collect(Collectors.toSet())),new HashSet<>(GameFactory.supportedIds()));}
    @Test void factoryRejectsUnknownIds(){assertThrows(IllegalArgumentException.class,()->GameFactory.create("definitely_not_a_game"));}

    @Test void twentyFortyEightDoesNotMergeAResultTwice()throws Exception{Game2048 game=new Game2048();Method m=Game2048.class.getDeclaredMethod("mergeLine",int[].class);m.setAccessible(true);assertArrayEquals(new int[]{4,4,0,0},(int[])m.invoke(game,(Object)new int[]{2,2,2,2}));}
    @Test void twentyFortyEightOnlyMergesAdjacentCompactedPairs()throws Exception{Game2048 game=new Game2048();Method m=Game2048.class.getDeclaredMethod("mergeLine",int[].class);m.setAccessible(true);assertArrayEquals(new int[]{4,2,0,0},(int[])m.invoke(game,(Object)new int[]{2,0,2,2}));}
    @Test void connectFourDetectsDiagonalWins()throws Exception{Method m=ConnectFourGame.class.getDeclaredMethod("hasWon",int[][].class,int.class);m.setAccessible(true);int[][]b={{0,0,0,1,0,0,0},{0,0,1,2,0,0,0},{0,1,2,2,0,0,0},{1,2,2,1,0,0,0},{0,0,0,0,0,0,0},{0,0,0,0,0,0,0}};assertTrue((boolean)m.invoke(null,(Object)b,1));}
    @Test void connectFourDoesNotAcceptAThreeInARow()throws Exception{Method m=ConnectFourGame.class.getDeclaredMethod("hasWon",int[][].class,int.class);m.setAccessible(true);int[][]b={{0,0,0,0,0,0,0},{0,0,0,0,0,0,0},{0,0,0,0,0,0,0},{0,0,0,1,0,0,0},{0,0,1,1,0,0,0},{0,0,0,0,0,0,0}};assertFalse((boolean)m.invoke(null,(Object)b,1));}
    @Test void wordBankIsSubstantialAndSanitized(){assertTrue(WordBank.all().size()>=64);assertTrue(WordBank.all().stream().allMatch(word->word.matches("[A-Z]{3,18}")));}
}
