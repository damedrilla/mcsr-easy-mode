package me.contaria.speedrunapi.util;

import net.minecraft.class_2960;

public final class IdentifierUtil {

    public static class_2960 of(String namespace, String path) {
        return new class_2960(namespace, path);
    }

    public static class_2960 ofVanilla(String path) {
        return new class_2960("minecraft", path);
    }

    public static class_2960 parse(String id) {
        int index = id.indexOf(':');
        return new class_2960(id.substring(0, index), id.substring(index + 1));
    }
}
