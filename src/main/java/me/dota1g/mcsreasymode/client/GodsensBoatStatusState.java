package me.dota1g.mcsreasymode.client;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;

public final class GodsensBoatStatusState {
    private static final long CLEAN_FADE_DURATION_MILLIS = 1200L;
    private static ClientPlayerEntity trackedPlayer;
    private static BoatEntity trackedBoat;
    private static Status status = Status.CLEAN;
    private static int mountedTicks;
    private static boolean steeredDuringRide;
    private static long cleanFadeStartedAt = -1L;

    private GodsensBoatStatusState() {
    }

    public static void tick(ClientPlayerEntity player) {
        if (player != trackedPlayer) {
            trackedPlayer = player;
            trackedBoat = null;
            mountedTicks = 0;
            steeredDuringRide = false;
        }

        Entity vehicle = player.getVehicle();
        BoatEntity boat = vehicle instanceof BoatEntity ? (BoatEntity) vehicle : null;
        if (boat != trackedBoat) {
            finishRide();
            if (boat != null) {
                trackedBoat = boat;
                mountedTicks = 0;
                steeredDuringRide = false;
                status = Status.RESETTING;
                cleanFadeStartedAt = -1L;
            }
        }

        if (trackedBoat != null) {
            mountedTicks++;
        }
    }

    public static void reset() {
        trackedPlayer = null;
        trackedBoat = null;
        status = Status.CLEAN;
        mountedTicks = 0;
        steeredDuringRide = false;
        cleanFadeStartedAt = -1L;
    }

    public static void onBoatInput(BoatEntity boat, boolean left, boolean right) {
        if ((!left && !right) || trackedPlayer == null || trackedPlayer.getVehicle() != boat) {
            return;
        }

        trackedBoat = boat;
        steeredDuringRide = true;
        status = Status.RESET_FAILED;
        cleanFadeStartedAt = -1L;
    }

    public static Status getStatus() {
        return status;
    }

    public static float getIconAlpha() {
        if (status != Status.CLEAN) {
            return 1.0F;
        }
        if (cleanFadeStartedAt < 0L) {
            return 0.0F;
        }

        long elapsed = System.currentTimeMillis() - cleanFadeStartedAt;
        if (elapsed >= CLEAN_FADE_DURATION_MILLIS) {
            cleanFadeStartedAt = -1L;
            return 0.0F;
        }
        return 1.0F - (float) elapsed / CLEAN_FADE_DURATION_MILLIS;
    }

    private static void finishRide() {
        if (trackedBoat == null) {
            return;
        }

        if (mountedTicks > 0 && !steeredDuringRide) {
            status = Status.CLEAN;
            cleanFadeStartedAt = System.currentTimeMillis();
        } else if (steeredDuringRide) {
            status = Status.DIRTY;
            cleanFadeStartedAt = -1L;
        }
        trackedBoat = null;
        mountedTicks = 0;
        steeredDuringRide = false;
    }

    public enum Status {
        CLEAN("Godsens: Clean", 0x55FF55),
        DIRTY("Godsens: Dirty - boat A/D used", 0xFF5555),
        RESETTING("Godsens: Resetting - exit without A/D", 0xFFFF55),
        RESET_FAILED("Godsens: Reset failed - exit and retry", 0xFF5555);

        private final String text;
        private final int color;

        Status(String text, int color) {
            this.text = text;
            this.color = color;
        }

        public String text() {
            return this.text;
        }

        public int color() {
            return this.color;
        }
    }
}
