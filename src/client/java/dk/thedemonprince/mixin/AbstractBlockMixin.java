package dk.thedemonprince.mixin;

import dk.thedemonprince.feature.SignManager;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HangingSignItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SignItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.class)
public abstract class AbstractBlockMixin {
    
    @Inject(method = "onUseWithItem", at = @At("HEAD"), cancellable = true)
    private void onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ItemActionResult> cir) {
        if (world.isClient && isSign(stack)) {
            SignManager manager = SignManager.getInstance();
            if (manager.isSignPlateEnabled() && manager.getSelectedTemplate() != null) {
                cir.setReturnValue(ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
            }
        }
    }

    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    private void onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (world.isClient) {
            SignManager manager = SignManager.getInstance();
            if (manager.isSignPlateEnabled() && manager.getSelectedTemplate() != null) {
                if (isSign(player.getMainHandStack()) || isSign(player.getOffHandStack())) {
                    cir.setReturnValue(ActionResult.PASS);
                }
            }
        }
    }

    @Unique
    private boolean isSign(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Item item = stack.getItem();
        return item instanceof SignItem || item instanceof HangingSignItem;
    }
}
