package dev.strubbelkopp.bundle_jumble.mixin;

import net.minecraft.item.BlockItem;
import net.minecraft.item.BundleItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockItem.class)
public class BlockItemMixin {
    @Redirect(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;decrement(I)V"))
    private void cancelBundleDecrement(ItemStack itemStack, int amount) {
        Item item = itemStack.getItem();
        if (item instanceof BundleItem) {
            itemStack.decrement(0);
        } else {
            itemStack.decrement(amount);
        }
    }
}
