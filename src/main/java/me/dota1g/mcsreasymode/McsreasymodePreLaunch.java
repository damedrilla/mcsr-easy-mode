package me.dota1g.mcsreasymode;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class McsreasymodePreLaunch implements PreLaunchEntrypoint {
    private static final String MIXINS_FILE = "mcsreasymode.mixins.json";
    private static final String DEBUG_HUD_MIXIN = "DebugHudMixin";

    @Override
    public void onPreLaunch() {
        verify();
    }

    public static void verify() {
        new McsreasymodePreLaunch().verifyIntegrity();
    }

    private void verifyIntegrity() {
        ModContainer container = FabricLoader.getInstance().getModContainer(Mcsreasymode.MOD_ID)
                .orElseThrow(() -> new IllegalStateException("MCSR Easy Mode integrity check failed: mod container was not found."));

        JsonObject fabricModJson = this.readJson(container, "fabric.mod.json");
        JsonObject mixinsJson = this.readJson(container, MIXINS_FILE);

        if (!this.hasString(fabricModJson.getAsJsonArray("mixins"), MIXINS_FILE)) {
            throw new IllegalStateException("MCSR Easy Mode integrity check failed: " + MIXINS_FILE + " is not registered in fabric.mod.json.");
        }

        if (!this.hasString(mixinsJson.getAsJsonArray("client"), DEBUG_HUD_MIXIN)) {
            throw new IllegalStateException("MCSR Easy Mode integrity check failed: required F3 marker mixin was removed from " + MIXINS_FILE + ".");
        }
    }

    private JsonObject readJson(ModContainer container, String path) {
        Path jsonPath = container.findPath(path)
                .orElseThrow(() -> new IllegalStateException("MCSR Easy Mode integrity check failed: missing " + path + "."));

        try (Reader reader = Files.newBufferedReader(jsonPath, StandardCharsets.UTF_8)) {
            JsonElement element = new JsonParser().parse(reader);
            if (!element.isJsonObject()) {
                throw new IllegalStateException("MCSR Easy Mode integrity check failed: " + path + " is not a JSON object.");
            }
            return element.getAsJsonObject();
        } catch (IOException exception) {
            throw new IllegalStateException("MCSR Easy Mode integrity check failed: could not read " + path + ".", exception);
        }
    }

    private boolean hasString(JsonArray array, String expected) {
        if (array == null) {
            return false;
        }

        for (JsonElement element : array) {
            if (element.isJsonPrimitive() && expected.equals(element.getAsString())) {
                return true;
            }
        }
        return false;
    }
}
