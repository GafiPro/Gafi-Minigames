package com.gafipro.minigames.game.endless;

/** Harder dodging variant with denser hazard waves and faster scaling. */
public final class EndlessDodgerGame extends AvoiderGame {
    @Override public String id() { return "endless_dodger"; }
    @Override public String title() { return "Endless Dodger"; }

    @Override protected double hazardSpeedMultiplier() {
        return 1.35 + Math.min(3.2, elapsedNanos() / 22_000_000_000.0);
    }

    @Override protected void spawnHazard() {
        super.spawnHazard();
        if (elapsedNanos() > 8_000_000_000L) super.spawnHazard();
    }

    @Override public void start() {
        super.start();
        status = "A/D or arrows • Denser waves • Survive as long as possible";
    }
}
