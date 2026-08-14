package me.dota1g.mcsreasymode.client.skin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class WorldSkinManager {

    // pool of configured skin PNG paths (persisted config, order = display order)
    private static final List<String> skinPool = new ArrayList<>();

    // path -> resolved+registered Identifier, cached for the session so re-rolls don't re-read disk
    private static final Map<String, Identifier> resolvedCache = new HashMap<>();

    private static final Random random = new Random();

    private static Identifier currentSkin = null; // hot-path field, set once per world join

    public static void onWorldJoin() {
        if (!Mcsreasymode.CONFIG.worldSkinEnabled || skinPool.isEmpty()) {
            currentSkin = null;
            return;
        }
        String chosenPath = skinPool.get(random.nextInt(skinPool.size()));
        currentSkin = resolvedCache.computeIfAbsent(chosenPath, WorldSkinManager::loadCustomSkin);
    }

    public static void onWorldLeave() {
        currentSkin = null;
    }

    // hot path: called every frame from the mixin, must stay cheap
    public static Identifier getCurrentWorldSkin() {
        return currentSkin;
    }

    private static Identifier loadCustomSkin(String pngPath) {
        File pngFile = new File(pngPath);
        String cacheKey = Integer.toHexString(pngPath.hashCode()); // stable, always-valid Identifier segment
        Identifier id = new Identifier("mcsreasymode", "world_skins/" + cacheKey);
        TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
        try (InputStream in = new FileInputStream(pngFile)) {
            NativeImage image = NativeImage.read(in);
            NativeImageBackedTexture texture = new NativeImageBackedTexture(image);
            textureManager.registerTexture(id, texture);
            return id;
        } catch (IOException e) {
            return null;
        }
    }

    // config API for the screen
    public static void addSkin(String pngPath) {
        if (!skinPool.contains(pngPath)) {
            skinPool.add(pngPath);
        }
    }

    public static void removeSkin(String pngPath) {
        skinPool.remove(pngPath);
        resolvedCache.remove(pngPath);
    }

    public static List<String> getSkinPool() {
        return skinPool;
    }
}