package dev.strubbelkopp.bundle_jumble.mixin;

import dev.strubbelkopp.bundle_jumble.config.Config;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(BundleItem.class)
public abstract class BundleItemMixin extends Item implements DyeableItem{

    @Shadow @Final private static String ITEMS_KEY;
    @Shadow @Final public static int MAX_STORAGE;
    @Unique private static final int DEFAULT_COLOR = 0xcc7b46;
    @Unique private static final String SEED_KEY = "Seed";

    @Override
    public int getColor(ItemStack stack) {
        NbtCompound nbtCompound = stack.getSubNbt(DISPLAY_KEY);
        if (nbtCompound != null && nbtCompound.contains(COLOR_KEY, NbtElement.NUMBER_TYPE)) {
            return nbtCompound.getInt(COLOR_KEY);
        }
        return DEFAULT_COLOR;
    }

    @Shadow
    private static int getBundleOccupancy(ItemStack stack) {
        throw new IllegalCallerException();
    }

    @Shadow
    private static int getItemOccupancy(ItemStack stack) {
        throw new IllegalCallerException();
    }

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
        NbtCompound bundleNbtCompound = bundleItemStack.getOrCreateNbt();
        if (!bundleNbtCompound.contains(ITEMS_KEY)) {
            return super.useOnBlock(context);
        }
        World world = context.getWorld();
        NbtList nbtList = bundleNbtCompound.getList(ITEMS_KEY, NbtElement.COMPOUND_TYPE);
        List<Integer> availableIndexes = containedBlockIndexes(nbtList);
        ActionResult result = ActionResult.FAIL;
        while (!availableIndexes.isEmpty() && !(result == ActionResult.success(world.isClient))) {
            Random random = world.getRandom();
            random.setSeed(bundleNbtCompound.getLong(SEED_KEY));
            bundleNbtCompound.putLong(SEED_KEY, random.nextLong());
            int index = random.nextInt(availableIndexes.size());
            int compoundIndex = availableIndexes.get(index);
            NbtCompound nbtCompound = nbtList.getCompound(compoundIndex);
            ItemStack itemStack = ItemStack.fromNbt(nbtCompound);
            BlockItem blockItem = (BlockItem) itemStack.getItem();
            result = blockItem.useOnBlock(context);
            if (result == ActionResult.success(world.isClient)) {
                if (!player.isCreative()) {
                    ItemStack copyItemStack = itemStack.copy();
                    nbtList.remove(nbtCompound);
                    itemStack.decrement(1);
                    itemStack.writeNbt(nbtCompound);
                    if (itemStack.getCount() == 0) {
                        if (!Config.INSTANCE.automatic_refill || !tryRefillItemStack(player, copyItemStack, bundleItemStack, nbtList)) {
                            Text outOfItemMessage = Text.translatable("text.bundle_jumble.bundle.out_of_item", Text.translatable(blockItem.getTranslationKey()));
                            player.sendMessage(outOfItemMessage, true);
                        }
                    } else {
                        nbtList.add(0, nbtCompound);
                    }
                    if (nbtList.isEmpty()) {
                        bundleItemStack.removeSubNbt(ITEMS_KEY);
                    }
                }
                return result;
            } else {
                availableIndexes.remove(index);
            }
        }
        return super.useOnBlock(context);
    }

    @Unique
    static private Boolean tryRefillItemStack(PlayerEntity player, ItemStack itemStack, ItemStack bundleItemStack, NbtList nbtList) {
        PlayerInventory inventory = player.getInventory();
        int start_index = (Config.INSTANCE.refill_searches_hotbar) ? 0 : 9;
        for (int i = start_index; i < 36; i++) {
            ItemStack inventoryItemStack = inventory.getStack(i);
            if (ItemStack.canCombine(inventoryItemStack, itemStack)) {
                int j = getBundleOccupancy(bundleItemStack);
                int k = getItemOccupancy(inventoryItemStack);
                int l = Math.min(inventoryItemStack.getCount(), (MAX_STORAGE - j) / k);
                ItemStack newItemStack = inventoryItemStack.copy();
                newItemStack.setCount(l);
                NbtCompound newNbtCompound = new NbtCompound();
                newItemStack.writeNbt(newNbtCompound);
                nbtList.add(0, newNbtCompound);
                inventory.removeStack(i, l);
                return true;
            }
        }
        return false;
    }

    @Unique
    private List<Integer> containedBlockIndexes(NbtList nbtList) {
        List<Integer> availableIndexes = new ArrayList<>();
        for (int i = 0; i < nbtList.size(); i++) {
            NbtCompound nbtCompound = nbtList.getCompound(i);
            ItemStack itemStack =  ItemStack.fromNbt(nbtCompound);
            Item item = itemStack.getItem();
            if (item instanceof BlockItem) {
                availableIndexes.add(i);
            }
        }
        return availableIndexes;
    }
}