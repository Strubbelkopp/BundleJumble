package dev.strubbelkopp.bundle_jumble.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class Config {

    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public static final Path CONFIG_FILE = FabricLoader.getInstance()
            .getConfigDir().resolve("bundle_jumble.json");

    public static Config INSTANCE = loadConfig(CONFIG_FILE.toFile());

    public boolean shift_drops_items = false;
    public boolean more_bunnies = true;
    public boolean automatic_refill = true;
    public boolean refill_searches_hotbar = false;
    public boolean enable_leather_recipe = false;

    public static Screen createConfigScreen(Screen parentScreen) {
        return YetAnotherConfigLib.createBuilder()
                .title(Text.translatable("text.bundle_jumble.config.title"))
                .category(ConfigCategory.createBuilder()
                        .name(Text.translatable("text.bundle_jumble.config.general_category"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.bundle_jumble.config.shift_drops_items"))
                                .description(OptionDescription.of(Text.translatable("text.bundle_jumble.config.shift_drops_items_description")))
                                .controller(TickBoxControllerBuilder::create)
                                .binding(
                                        false,
                                        () -> Config.INSTANCE.shift_drops_items,
                                        newValue -> Config.INSTANCE.shift_drops_items = newValue
                                )
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.bundle_jumble.config.more_bunnies"))
                                .description(OptionDescription.of(Text.translatable("text.bundle_jumble.config.more_bunnies_description")))
                                .controller(TickBoxControllerBuilder::create)
                                .binding(
                                        true,
                                        () -> Config.INSTANCE.more_bunnies,
                                        newValue -> Config.INSTANCE.more_bunnies = newValue
                                )
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.bundle_jumble.config.automatic_refill"))
                                .description(OptionDescription.of(Text.translatable("text.bundle_jumble.config.automatic_refill_description")))
                                .controller(TickBoxControllerBuilder::create)
                                .binding(
                                        true,
                                        () -> Config.INSTANCE.automatic_refill,
                                        newValue -> Config.INSTANCE.automatic_refill = newValue
                                )
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.bundle_jumble.config.refill_searches_hotbar"))
                                .description(OptionDescription.of(Text.translatable("text.bundle_jumble.config.refill_searches_description")))
                                .controller(TickBoxControllerBuilder::create)
                                .binding(
                                        false,
                                        () -> Config.INSTANCE.refill_searches_hotbar,
                                        newValue -> Config.INSTANCE.refill_searches_hotbar = newValue
                                )
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.bundle_jumble.config.enable_leather_recipe"))
                                .description(OptionDescription.of(Text.translatable("text.bundle_jumble.config.enable_leather_recipe_description")))
                                .controller(TickBoxControllerBuilder::create)
                                .binding(
                                        false,
                                        () -> Config.INSTANCE.enable_leather_recipe,
                                        newValue -> Config.INSTANCE.enable_leather_recipe = newValue
                                )
                                .build())
                        .build())
                .save(() -> INSTANCE.saveConfig(CONFIG_FILE.toFile()))
                .build()
                .generateScreen(parentScreen);
    }

    public void saveConfig(File file) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Config loadConfig(File file) {
        Config config = null;

        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)
            )) {
                config = GSON.fromJson(reader, Config.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (config == null) {
            config = new Config();
        }

        config.saveConfig(file);
        return config;
    }
}
