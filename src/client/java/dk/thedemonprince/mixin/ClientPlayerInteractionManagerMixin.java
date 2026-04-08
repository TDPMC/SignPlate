package dk.thedemonprince.mixin;

import dk.thedemonprince.feature.SignManager;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.item.HangingSignItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SignItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
    @Inject(method = "interactBlock", at = @At("HEAD"))
    private void onInteractBlockHead(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if (SignManager.getInstance().isSignPlateEnabled() && SignManager.getInstance().getSelectedTemplate() != null) {
            ItemStack stack = player.getStackInHand(hand);
            if (isSign(stack) && !player.isSneaking()) {
                // Tell the server we started sneaking just before the interaction packet
                player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
            }
        }
    }

    @Inject(method = "interactBlock", at = @At("RETURN"))
    private void onInteractBlockReturn(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if (SignManager.getInstance().isSignPlateEnabled() && SignManager.getInstance().getSelectedTemplate() != null) {
            ItemStack stack = player.getStackInHand(hand);
            if (isSign(stack) && !player.isSneaking()) {
                // Tell the server we stopped sneaking just after
                player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
            }
        }
    }

    @Unique
    private boolean isSign(ItemStack stack) {
        return stack != null && (stack.getItem() instanceof SignItem || stack.getItem() instanceof HangingSignItem);
    }
}
