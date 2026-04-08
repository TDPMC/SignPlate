package dk.thedemonprince.client;

import dk.thedemonprince.client.gui.SignPlateScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class SignplateClient implements ClientModInitializer {
    private static KeyBinding openSignPlateKey;

    @Override
    public void onInitializeClient() {
        openSignPlateKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.signplate.open",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                "category.signplate"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client == null) return;
            while (openSignPlateKey.wasPressed()) {
                client.setScreen(new SignPlateScreen(null));
            }
        });
    }
}
