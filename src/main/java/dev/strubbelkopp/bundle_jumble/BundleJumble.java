package dev.strubbelkopp.bundle_jumble;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;

public class BundleJumble implements ModInitializer {
    @Override
    public void onInitialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(itemGroup -> itemGroup.addAfter(Items.LEAD, Items.BUNDLE));
    }
}
