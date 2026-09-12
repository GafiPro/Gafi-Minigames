package com.gafipro.minigames;

import com.gafipro.minigames.gui.GamesScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;

public final class GafiMinigamesClient implements ClientModInitializer {
    public static final String MOD_ID = "gafi-minigames";

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(ClientCommandManager.literal("games")
                .executes(context -> {
                    MinecraftClient.getInstance().setScreen(new GamesScreen(null));
                    return 1;
                }))
        );
    }
}
