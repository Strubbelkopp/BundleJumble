package dev.strubbelkopp.bundle_jumble;

import dev.strubbelkopp.bundle_jumble.config.ConfigOptionsEnabledCondition;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public class BundleJumble implements ModInitializer {

    public static final String MOD_ID = "bundle_jumble";

    @Override
    public void onInitialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(itemGroup -> itemGroup.addAfter(Items.LEAD, Items.BUNDLE));
        ResourceConditions.register(new Identifier(MOD_ID, "config_options_enabled"), ConfigOptionsEnabledCondition::configEnabled);
    }
}
