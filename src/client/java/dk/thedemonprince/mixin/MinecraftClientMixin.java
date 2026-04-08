package dk.thedemonprince.mixin;

import dk.thedemonprince.feature.SignManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void onSetScreen(Screen screen, CallbackInfo ci) {
        if (screen instanceof AbstractSignEditScreen) {
            SignManager manager = SignManager.getInstance();
            if (manager.isSignPlateEnabled() && manager.getSelectedTemplate() != null) {
                AbstractSignEditScreenAccessor accessor = (AbstractSignEditScreenAccessor) screen;
                // SignBlockEntity is now accessed differently or might be null in some contexts
                if (accessor.getBlockEntity() != null) {
                    manager.applyTemplate(accessor.getBlockEntity().getPos(), accessor.isFront());
                    ci.cancel();
                }
            }
        }
    }
}
