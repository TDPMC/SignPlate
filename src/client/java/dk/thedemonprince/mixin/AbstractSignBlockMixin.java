package dk.thedemonprince.mixin;

import dk.thedemonprince.feature.SignManager;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSignBlock.class)
public class AbstractSignBlockMixin {
    @Inject(method = "openEditScreen", at = @At("HEAD"), cancellable = true)
    private void onOpenEditScreen(PlayerEntity player, SignBlockEntity signBlockEntity, boolean front, CallbackInfo ci) {
        if (signBlockEntity != null && SignManager.getInstance().isSignPlateEnabled() && SignManager.getInstance().getSelectedTemplate() != null) {
            SignManager.getInstance().applyTemplate(signBlockEntity.getPos(), front);
            ci.cancel();
        }
    }
}
