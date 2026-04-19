package com.goodboy.scoreboardswap;

import com.goodboy.scoreboardswap.gui.ScoreboardSwapScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ScoreboardSwapClient implements ClientModInitializer {

    public static KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.scoreboardswap.open",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F8,
                "category.scoreboardswap"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new ScoreboardSwapScreen());
                }
            }
        });

        SwapConfig.load();
    }
}
