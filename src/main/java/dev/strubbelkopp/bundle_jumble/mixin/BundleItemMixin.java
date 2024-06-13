package dev.strubbelkopp.bundle_jumble.mixin;

import dev.strubbelkopp.bundle_jumble.BundleJumble;
import dev.strubbelkopp.bundle_jumble.config.Config;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.*;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(BundleItem.class)
public abstract class BundleItemMixin extends Item {

    public BundleItemMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (!Config.INSTANCE.shift_drops_items || !user.isSneaking()) {
            cir.setReturnValue(TypedActionResult.fail(user.getStackInHand(hand)));
        }
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        if (player == null || (player.isSneaking() && Config.INSTANCE.shift_drops_items)) {
            return super.useOnBlock(context);
        }

        ItemStack bundleItemStack = player.getStackInHand(context.getHand());
        BundleContentsComponent bundleContentsComponent = bundleItemStack.get(DataComponentTypes.BUNDLE_CONTENTS);
        if (bundleContentsComponent == null || bundleContentsComponent.isEmpty()) {
            return super.useOnBlock(context);
        }

        World world = context.getWorld();
        ActionResult result = ActionResult.FAIL;
        List<Integer> availableIndexes = containedBlockIndexes(bundleContentsComponent);
        while(!availableIndexes.isEmpty() && result != ActionResult.success(world.isClient)) {
            Random random = world.getRandom();
            random.setSeed(bundleItemStack.getOrDefault(BundleJumble.RANDOM_SEED, random.nextLong()));
            bundleItemStack.set(BundleJumble.RANDOM_SEED, random.nextLong());
            int randomIndex = random.nextInt(availableIndexes.size());
            BlockItem blockItem = (BlockItem) bundleContentsComponent.get(randomIndex).getItem();

            result = blockItem.useOnBlock(context);
            if (result == ActionResult.success(world.isClient)) {
                if (!world.isClient && !player.isCreative()) {
                    removeItem(player, bundleItemStack, randomIndex);
                }
                return result;
            } else {
                availableIndexes.remove(randomIndex);
            }
        }
        return super.useOnBlock(context);
    }

    @Unique
    private List<Integer> containedBlockIndexes(BundleContentsComponent bundleContentsComponent) {
        List<Integer> availableIndexes = new ArrayList<>();
        for (int i = 0; i < bundleContentsComponent.size(); i++) {
            if (bundleContentsComponent.get(i).getItem() instanceof BlockItem) {
                availableIndexes.add(i);
            }
        }
        return availableIndexes;
    }

    @Unique
    private void removeItem(PlayerEntity player, ItemStack bundleItemStack, int index) {
        BundleContentsComponent bundleContentsComponent = bundleItemStack.get(DataComponentTypes.BUNDLE_CONTENTS);
        if (bundleContentsComponent != null && !bundleContentsComponent.isEmpty()) {
            List<ItemStack> stacks = new ArrayList<>(bundleContentsComponent.stream().toList());
            ItemStack itemStack = bundleContentsComponent.get(index).copy();
            ItemStack itemStackCopy = itemStack.copy();

            itemStack.decrement(1);
            if (!itemStack.isEmpty()) {
                stacks.set(index, itemStack);
                bundleContentsComponent = new BundleContentsComponent(stacks);
            } else {
                stacks.remove(index);
                bundleContentsComponent = new BundleContentsComponent(stacks);
                Pair<BundleContentsComponent, Boolean> result = tryRefillItemStack(player, itemStackCopy, bundleContentsComponent);
                bundleContentsComponent = result.getLeft();
                if (!Config.INSTANCE.automatic_refill || !result.getRight()) {
                    player.sendMessage(Text.translatable("text.bundle_jumble.bundle.out_of_item", Text.translatable(itemStackCopy.getTranslationKey())), true);
                }
            }

            bundleItemStack.set(DataComponentTypes.BUNDLE_CONTENTS, bundleContentsComponent);
        }
    }

    @Unique
    private Pair<BundleContentsComponent, Boolean> tryRefillItemStack(PlayerEntity player, ItemStack itemStack, BundleContentsComponent bundleContentsComponent) {
        PlayerInventory inventory = player.getInventory();
        int start_index = (Config.INSTANCE.refill_searches_hotbar) ? 0 : 9;
        for (int i = start_index; i < 36; i++) {
            ItemStack inventoryItemStack = inventory.getStack(i);
            if (ItemStack.areItemsAndComponentsEqual(inventoryItemStack, itemStack)) {
                BundleContentsComponent.Builder builder = new BundleContentsComponent.Builder(bundleContentsComponent);
                int itemsAdded = builder.add(inventoryItemStack);
                if (itemsAdded > 0) {
                    inventory.removeStack(i, itemsAdded);
                    return new Pair<>(builder.build(), true);
                }
            }
        }
        return new Pair<>(bundleContentsComponent, false);
    }
}