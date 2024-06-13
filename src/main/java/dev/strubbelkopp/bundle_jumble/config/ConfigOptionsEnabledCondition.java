package dev.strubbelkopp.bundle_jumble.config;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.strubbelkopp.bundle_jumble.BundleJumble;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public record ConfigOptionsEnabledCondition(List<String> configOptions) implements ResourceCondition {
    public static final MapCodec<ConfigOptionsEnabledCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(Codec.STRING.listOf().fieldOf("options").forGetter(ConfigOptionsEnabledCondition::configOptions))
                    .apply(instance, ConfigOptionsEnabledCondition::new));

    @Override
    public ResourceConditionType<?> getType() {
        return BundleJumble.CONFIG_OPTION_ENABLED_CONDITION_TYPE;
    }

    @Override
    public boolean test(@Nullable RegistryWrapper.WrapperLookup registryLookup) {
        JsonObject config = readConfigFile(Config.CONFIG_FILE.toFile());
        if (config != null) {
            return configOptions.stream().filter(configOption -> !config.has(configOption) || !config.get(configOption)
                    .getAsBoolean()).toList().isEmpty();
        }
        return false;
    }

    private static JsonObject readConfigFile(File file) {
        JsonObject object = null;
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                object = JsonHelper.deserialize(reader);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return object;
    }
}
