package me.dota1g.mcsreasymode;

public final class ExplosionDropState {
    private static int depth = 0;

    private ExplosionDropState() {
    }

    public static void enter() {
        depth++;
    }

    public static void exit() {
        if (depth > 0) {
            depth--;
        }
    }

    public static boolean isActive() {
        return depth > 0;
    }
}