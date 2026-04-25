package com.researchtool;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.researchtool.config.ResearchToolConfig;
import com.researchtool.console.ResearchConsole;
import com.researchtool.gui.ResearchGui;
import com.researchtool.movement.MovementExperimentHandler;
import com.researchtool.teleport.TeleportStudyHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class ResearchToolMod implements ClientModInitializer {
    public static final String MOD_ID = "researchtool";

    private static ResearchToolConfig config;
    private static KeyBinding openGuiKey;
    private static KeyBinding toggleMovementKey;
    private static MovementExperimentHandler movementHandler;
    private static TeleportStudyHandler teleportStudyHandler;

    @Override
    public void onInitializeClient() {
        config = ResearchToolConfig.load();
        movementHandler = new MovementExperimentHandler();
        teleportStudyHandler = new TeleportStudyHandler();

        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.researchtool.open_gui", GLFW.GLFW_KEY_R, "category.researchtool"));
        toggleMovementKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.researchtool.toggle_movement", GLFW.GLFW_KEY_G, "category.researchtool"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.wasPressed()) {
                if (config.guiEnabled) {
                    playUiClick(client);
                    client.setScreen(new ResearchGui(client, movementHandler, teleportStudyHandler));
                }
            }
            while (toggleMovementKey.wasPressed()) {
                if (config.movementEnabled) {
                    boolean state = movementHandler.toggle();
                    client.player.sendMessage(Text.literal("[ResearchTool] Movement experiment: " + (state ? "ON" : "OFF")), false);
                    playUiClick(client);
                }
            }
            movementHandler.onClientTick(client);
            teleportStudyHandler.onClientTick(client);
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("fa")
                    .executes(ctx -> {
                        MinecraftClient client = MinecraftClient.getInstance();
                        if (client != null) {
                            playUiClick(client);
                            client.setScreen(new ResearchGui(client, movementHandler, teleportStudyHandler));
                        }
                        return 1;
                    }));

            dispatcher.register(ClientCommandManager.literal("fagive")
                    .then(ClientCommandManager.argument("item", StringArgumentType.greedyString())
                            .executes(ctx -> {
                                String item = StringArgumentType.getString(ctx, "item");
                                MinecraftClient client = MinecraftClient.getInstance();
                                if (client != null && client.player != null) {
                                    client.player.sendMessage(Text.literal("[Server] Gave " + item + " to player, but you lack permissions."), false);
                                }
                                return 1;
                            })));

            dispatcher.register(ClientCommandManager.literal("faconsole")
                    .executes(ctx -> {
                        ResearchConsole.open();
                        return 1;
                    }));
        });
    }

    public static void playUiClick(MinecraftClient client) {
        if (client.player != null) {
            client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        }
    }

    public static ResearchToolConfig config() {
        return config;
    }
}
