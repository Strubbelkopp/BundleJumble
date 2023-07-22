package dev.strubbelkopp.bundle_jumble.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.JsonHelper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class ConfigOptionsEnabledCondition {

    public static boolean configEnabled(JsonObject object) {
        Path path = Config.CONFIG_FILE;
        JsonArray array = JsonHelper.getArray(object, "options");
        JsonObject config = readConfigFile(path.toFile());

        if (config != null) {
            for (JsonElement element : array) {
                if (element.isJsonPrimitive()) {
                    String option = element.getAsString();
                    if (!config.has(option)) {
                        return false;
                    }
                    if (!config.get(option).getAsBoolean()) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    private static JsonObject readConfigFile(File file) {
        JsonObject object = null;
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)
            )) {
                object = JsonHelper.deserialize(reader);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return object;
    }


}
