package dev.strubbelkopp.bundle_jumble.mixin;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Item.class)
public abstract class ItemMixin {
    @Shadow public abstract Item asItem();
    @Inject(method = "appendStacks", at = @At("HEAD"))
    private void onAppendStacks(ItemGroup group, DefaultedList<ItemStack> stacks, CallbackInfo ci) {
        if (this.asItem() == Items.BUNDLE && group == ItemGroup.TOOLS) {
            stacks.add(new ItemStack(this::asItem));
        }
    }
}