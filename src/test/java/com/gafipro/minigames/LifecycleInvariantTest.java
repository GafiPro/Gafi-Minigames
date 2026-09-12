package com.gafipro.minigames;

import com.gafipro.minigames.game.BaseGame;
import com.gafipro.minigames.game.GameState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LifecycleInvariantTest {
    @Test
    void beginResetsLifecycleAndFinishIsIdempotent() {
        DummyGame game = new DummyGame();
        game.begin();
        assertEquals(GameState.PLAYING, game.state());
        assertEquals(1, game.starts);
        assertEquals(0, game.score());

        game.addScore(321);
        game.finishForTest();
        assertEquals(GameState.LOST, game.state());
        assertEquals(321, game.score());

        game.winForTest();
        assertEquals(GameState.LOST, game.state());
        assertEquals(321, game.score());

        game.begin();
        assertEquals(GameState.PLAYING, game.state());
        assertEquals(2, game.starts);
        assertEquals(0, game.score());
        assertEquals(0, game.metrics().moves());
        assertEquals(100.0, game.metrics().accuracyPercent());
    }

    private static final class DummyGame extends BaseGame {
        private int starts;
        @Override public String id() { return "test_lifecycle"; }
        @Override public String title() { return "Lifecycle Test"; }
        @Override public String category() { return "Test"; }
        @Override public void start() { starts++; }
        void addScore(int amount) { score += amount; }
        void finishForTest() { finish(score); }
        void winForTest() { finishWin(score + 999); }
    }
}
