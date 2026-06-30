package me.contaria.speedrunapi.util;

import net.minecraft.class_2561;
import net.minecraft.class_2585;
import net.minecraft.class_2588;
import net.minecraft.class_5250;

public final class TextUtil {

    public static class_5250 translatable(String key) {
        return new class_2588(key);
    }

    public static class_5250 translatable(String key, Object... args) {
        return new class_2588(key, args);
    }

    public static class_5250 literal(String string) {
        return new class_2585(string);
    }

    public static class_2561 empty() {
        return class_2585.field_24366;
    }
}
