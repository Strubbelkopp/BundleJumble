package dev.strubbelkopp.bundle_jumble;

import com.mojang.serialization.Codec;
import dev.strubbelkopp.bundle_jumble.config.ConfigOptionsEnabledCondition;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class BundleJumble implements ModInitializer {

    public static final String MOD_ID = "bundle_jumble";

    public static final ComponentType<Long> RANDOM_SEED = ComponentType.<Long>builder().codec(Codec.LONG).packetCodec(PacketCodecs.VAR_LONG).build();
    public static final ResourceConditionType<ConfigOptionsEnabledCondition> CONFIG_OPTION_ENABLED_CONDITION_TYPE = ResourceConditionType
            .create(Identifier.of(BundleJumble.MOD_ID, "config_options_enabled"), ConfigOptionsEnabledCondition.CODEC);

    @Override
    public void onInitialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(itemGroup -> itemGroup.addAfter(Items.LEAD, Items.BUNDLE));
        ResourceConditions.register(CONFIG_OPTION_ENABLED_CONDITION_TYPE);
        Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(MOD_ID, "random_seed"), RANDOM_SEED);
    }
}
