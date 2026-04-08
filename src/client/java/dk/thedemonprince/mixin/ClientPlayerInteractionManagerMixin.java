package dk.thedemonprince.mixin;

import dk.thedemonprince.feature.SignManager;
import net.minecraft.block.*;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.item.HangingSignItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SignItem;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
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
            if (isSign(stack) && !player.isSneaking() && shouldSneak(player, hitResult)) {
                // Tell the server we started sneaking just before the interaction packet
                PlayerInput lastInput = player.getLastPlayerInput();
                PlayerInput spoofedInput = new PlayerInput(
                        lastInput.forward(), lastInput.backward(), lastInput.left(), lastInput.right(),
                        lastInput.jump(), true, lastInput.sprint()
                );
                player.networkHandler.sendPacket(new PlayerInputC2SPacket(spoofedInput));
            }
        }
    }

    @Inject(method = "interactBlock", at = @At("RETURN"))
    private void onInteractBlockReturn(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if (SignManager.getInstance().isSignPlateEnabled() && SignManager.getInstance().getSelectedTemplate() != null) {
            ItemStack stack = player.getStackInHand(hand);
            if (isSign(stack) && !player.isSneaking() && shouldSneak(player, hitResult)) {
                // Tell the server we stopped sneaking just after
                PlayerInput lastInput = player.getLastPlayerInput();
                PlayerInput spoofedInput = new PlayerInput(
                        lastInput.forward(), lastInput.backward(), lastInput.left(), lastInput.right(),
                        lastInput.jump(), false, lastInput.sprint()
                );
                // Send twice to ensure the server processes it correctly after the interaction
                player.networkHandler.sendPacket(new PlayerInputC2SPacket(spoofedInput));
                player.networkHandler.sendPacket(new PlayerInputC2SPacket(spoofedInput));
            }
        }
    }

    @Unique
    private boolean isSign(ItemStack stack) {
        return stack != null && (stack.getItem() instanceof SignItem || stack.getItem() instanceof HangingSignItem);
    }

    @Unique
    private boolean shouldSneak(ClientPlayerEntity player, BlockHitResult hitResult) {
        BlockPos pos = hitResult.getBlockPos();
        BlockState state = player.getWorld().getBlockState(pos);
        Block block = state.getBlock();

        // Check for containers and blocks that definitely have GUIs/Interactions
        if (block instanceof AbstractChestBlock ||
                block instanceof AbstractFurnaceBlock || block instanceof ShulkerBoxBlock || block instanceof BarrelBlock ||
                block instanceof HopperBlock || block instanceof DispenserBlock || block instanceof BrewingStandBlock) {
            return true;
        }

        // Check for common interactable utility blocks
        if (block instanceof CraftingTableBlock || block instanceof AnvilBlock || block instanceof LecternBlock ||
            block instanceof EnchantingTableBlock || block instanceof LoomBlock || block instanceof CartographyTableBlock ||
            block instanceof GrindstoneBlock || block instanceof StonecutterBlock) {
            return true;
        }

        // Check for redstone/mechanical interactables
        if (block instanceof ButtonBlock || block instanceof LeverBlock || block instanceof DoorBlock || 
            block instanceof TrapdoorBlock || block instanceof FenceGateBlock || block instanceof NoteBlock) {
            return true;
        }

        // Check for existing signs (to avoid opening the edit screen on servers)
        return block instanceof SignBlock || block instanceof HangingSignBlock;
    }
}
