package com.gafipro.minigames;

import com.gafipro.minigames.gui.GamesScreen;
import com.gafipro.minigames.multiplayer.MultiplayerManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;

public final class GafiMinigamesClient implements ClientModInitializer {
    public static final String MOD_ID = "gafi-minigames";
    @Override public void onInitializeClient() {
        MultiplayerManager.init();
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(ClientCommandManager.literal("games")
                .executes(context -> { MinecraftClient.getInstance().setScreen(new GamesScreen(null)); return 1; })
                .then(ClientCommandManager.literal("invite")
                    .then(ClientCommandManager.argument("player", StringArgumentType.word())
                        .executes(context -> { MultiplayerManager.invite(StringArgumentType.getString(context,"player"), "connect_four"); return 1; })))
            )
        );
    }
}
