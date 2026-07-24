package me.dota1g.mcsreasymode;

import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public final class McsreasymodeMinecartVelocityState {
    private static final double PROJECTILE_SPEED_CAP = 320.0D;
    private static final double PASSENGER_SPEED_CAP = 64.0D;
    private static final double INTERNAL_SPEED_CAP = 768.0D;
    private static final double STACK_CONTACT_EXPAND_XZ = 1.25D;
    private static final double STACK_CONTACT_EXPAND_Y = 0.35D;
    private static final double CHARGE_SEARCH_EXPAND_XZ = 2.5D;
    private static final double STACK_MASS_SEARCH_EXPAND_XZ = 3.0D;
    private static final double STACK_MASS_SEARCH_EXPAND_Y = 1.0D;
    private static final int CHARGE_EXPIRY_TICKS = 200;
    private static final Map<AbstractMinecartEntity, Charge> CHARGES = new WeakHashMap<>();
    private static final Map<UUID, Charge> CHARGE_SNAPSHOTS = new java.util.HashMap<>();

    private McsreasymodeMinecartVelocityState() {
    }

    public static void tickStackedMinecart(AbstractMinecartEntity cart) {
        if (cart.world.isClient) {
            return;
        }
        if (!Mcsreasymode.isFunMinecartVelocityGlitchEnabled()) {
            forget(cart);
            return;
        }

        cleanExpired(cart.age);
        Charge ownCharge = CHARGES.get(cart);
        boolean stacked = false;
        int nearbyMinecarts = 0;
        Vec3d inherited = Vec3d.ZERO;

        for (Entity entity : cart.world.getEntities(cart, cart.getBoundingBox().expand(STACK_CONTACT_EXPAND_XZ, STACK_CONTACT_EXPAND_Y, STACK_CONTACT_EXPAND_XZ))) {
            if (!(entity instanceof AbstractMinecartEntity)) {
                continue;
            }

            stacked = true;
            nearbyMinecarts++;
            AbstractMinecartEntity otherCart = (AbstractMinecartEntity) entity;
            Charge otherCharge = chargeFor(otherCart);
            if (otherCharge != null) {
                inherited = inherited.add(otherCharge.velocity.multiply(0.62D));
            }
            inherited = inherited.add(horizontalVelocity(otherCart).multiply(0.75D));
        }

        if (!stacked) {
            if (ownCharge != null && cart.age - ownCharge.lastContactAge > CHARGE_EXPIRY_TICKS) {
                CHARGES.remove(cart);
            }
            return;
        }

        Vec3d actualVelocity = horizontalVelocity(cart);
        Vec3d base = ownCharge == null || horizontalLength(actualVelocity) > horizontalLength(ownCharge.velocity) ? actualVelocity : ownCharge.velocity;
        Vec3d direction = directionFor(cart);
        int stackMass = Math.max(nearbyMinecarts + 1, nearbyMinecartCount(cart, STACK_MASS_SEARCH_EXPAND_XZ, STACK_MASS_SEARCH_EXPAND_Y) + 1);
        double stackFactor = Math.min(stackMass, 5);
        double selfMultiplier = 1.35D + stackFactor * 0.18D;
        Vec3d glitched = base.multiply(selfMultiplier).add(inherited).add(direction.multiply(0.9D * stackFactor));
        glitched = ensureMinimumStackSpeed(glitched, direction, stackMass);
        glitched = capHorizontal(glitched, INTERNAL_SPEED_CAP);
        Charge charge = new Charge(glitched, cart.age);
        CHARGES.put(cart, charge);
        CHARGE_SNAPSHOTS.put(cart.getUuid(), charge);

        if (horizontalLength(glitched) >= 16.0D) {
            Mcsreasymode.debugRateLimited(
                    "fun.minecart.charge." + cart.getEntityId(),
                    "Fun mode minecart velocity charged to " + Math.round(horizontalLength(glitched)) + " b/t.",
                    3000L
            );
        }
    }

    public static Vec3d projectileBoost(Entity shooter) {
        double speed = projectileBoostSpeed(shooter);
        if (speed <= 0.0D) {
            return Vec3d.ZERO;
        }

        Entity vehicle = shooter.getVehicle();
        if (!(vehicle instanceof AbstractMinecartEntity)) {
            return Vec3d.ZERO;
        }

        Charge charge = strongestNearbyCharge((AbstractMinecartEntity) vehicle);
        return charge == null ? Vec3d.ZERO : capHorizontal(charge.velocity, speed);
    }

    public static double projectileBoostSpeed(Entity shooter) {
        if (!Mcsreasymode.isFunMinecartVelocityGlitchEnabled() || shooter == null || !shooter.hasVehicle()) {
            return 0.0D;
        }

        Entity vehicle = shooter.getVehicle();
        if (!(vehicle instanceof AbstractMinecartEntity)) {
            return 0.0D;
        }

        AbstractMinecartEntity cart = (AbstractMinecartEntity) vehicle;
        Charge charge = strongestNearbyCharge(cart);
        if (charge == null || cart.age - charge.lastContactAge > CHARGE_EXPIRY_TICKS) {
            return 0.0D;
        }

        return Math.min(horizontalLength(charge.velocity), PROJECTILE_SPEED_CAP);
    }

    public static double minecartChargeSpeed(Entity viewer) {
        if (!Mcsreasymode.isFunMinecartVelocityGlitchEnabled() || viewer == null || !viewer.hasVehicle()) {
            return 0.0D;
        }

        Entity vehicle = viewer.getVehicle();
        if (!(vehicle instanceof AbstractMinecartEntity)) {
            return 0.0D;
        }

        Charge charge = strongestNearbyCharge((AbstractMinecartEntity) vehicle);
        return charge == null ? 0.0D : horizontalLength(charge.velocity);
    }

    public static void stopShooterRecoil(Entity shooter) {
        if (!Mcsreasymode.isFunMinecartVelocityGlitchEnabled() || shooter == null || shooter.world.isClient || !shooter.hasVehicle()) {
            return;
        }

        Entity vehicle = shooter.getVehicle();
        if (!(vehicle instanceof AbstractMinecartEntity)) {
            return;
        }

        Vec3d shooterVelocity = shooter.getVelocity();
        shooter.setVelocity(0.0D, shooterVelocity.y, 0.0D);
    }

    public static void transferToPassengers(AbstractMinecartEntity cart) {
        if (!Mcsreasymode.isFunMinecartVelocityGlitchEnabled() || cart.world.isClient || !cart.hasPassengers()) {
            return;
        }

        Charge charge = strongestNearbyCharge(cart);
        CHARGES.remove(cart);
        if (charge == null || cart.age - charge.lastContactAge > CHARGE_EXPIRY_TICKS) {
            return;
        }

        Vec3d boost = capHorizontal(charge.velocity, PASSENGER_SPEED_CAP);
        if (horizontalLength(boost) < 1.0D) {
            return;
        }

        for (Entity passenger : cart.getPassengerList()) {
            passenger.stopRiding();
            passenger.setVelocity(passenger.getVelocity().add(boost));
        }

        Mcsreasymode.debugRateLimited(
                "fun.minecart.passenger_transfer." + cart.getEntityId(),
                "Fun mode minecart velocity transferred " + Math.round(horizontalLength(boost)) + " b/t to passenger(s).",
                1000L
        );
    }

    public static void forget(AbstractMinecartEntity cart) {
        CHARGES.remove(cart);
        CHARGE_SNAPSHOTS.remove(cart.getUuid());
    }

    private static Vec3d directionFor(AbstractMinecartEntity cart) {
        Vec3d velocity = cart.getVelocity();
        Vec3d horizontal = new Vec3d(velocity.x, 0.0D, velocity.z);
        if (horizontalLength(horizontal) > 0.01D) {
            return horizontal.normalize();
        }

        double angle = (cart.getEntityId() * 47 % 360) * Math.PI / 180.0D;
        return new Vec3d(Math.cos(angle), 0.0D, Math.sin(angle));
    }

    private static Charge strongestNearbyCharge(AbstractMinecartEntity cart) {
        Charge strongest = strongestOf(chargeFor(cart), currentVelocityCharge(cart));
        for (Entity entity : cart.world.getEntities(cart, cart.getBoundingBox().expand(CHARGE_SEARCH_EXPAND_XZ, 1.0D, CHARGE_SEARCH_EXPAND_XZ))) {
            if (!(entity instanceof AbstractMinecartEntity)) {
                continue;
            }

            AbstractMinecartEntity nearbyCart = (AbstractMinecartEntity) entity;
            Charge charge = strongestOf(chargeFor(nearbyCart), currentVelocityCharge(nearbyCart));
            if (charge == null || cart.age - charge.lastContactAge > CHARGE_EXPIRY_TICKS) {
                continue;
            }

            if (strongest == null || horizontalLength(charge.velocity) > horizontalLength(strongest.velocity)) {
                strongest = charge;
            }
        }
        return strongest;
    }

    private static Charge strongestOf(Charge first, Charge second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        return horizontalLength(second.velocity) > horizontalLength(first.velocity) ? second : first;
    }

    private static Charge currentVelocityCharge(AbstractMinecartEntity cart) {
        Vec3d velocity = horizontalVelocity(cart);
        return horizontalLength(velocity) <= 0.01D ? null : new Charge(velocity, cart.age);
    }

    private static Vec3d ensureMinimumStackSpeed(Vec3d velocity, Vec3d direction, int stackMass) {
        double minimumSpeed = minimumSpeedForStackMass(stackMass);
        if (minimumSpeed <= 0.0D || horizontalLength(velocity) >= minimumSpeed) {
            return velocity;
        }

        return direction.multiply(minimumSpeed);
    }

    private static double minimumSpeedForStackMass(int stackMass) {
        switch (Math.min(stackMass, 5)) {
            case 5:
                return 360.0D;
            case 4:
                return 220.0D;
            case 3:
                return 96.0D;
            default:
                return 0.0D;
        }
    }

    private static int nearbyMinecartCount(AbstractMinecartEntity cart, double horizontalExpand, double verticalExpand) {
        int count = 0;
        for (Entity entity : cart.world.getEntities(cart, cart.getBoundingBox().expand(horizontalExpand, verticalExpand, horizontalExpand))) {
            if (entity instanceof AbstractMinecartEntity) {
                count++;
            }
        }
        return count;
    }

    private static Charge chargeFor(AbstractMinecartEntity cart) {
        Charge charge = CHARGES.get(cart);
        if (charge != null) {
            return charge;
        }
        return CHARGE_SNAPSHOTS.get(cart.getUuid());
    }

    private static Vec3d horizontalVelocity(Entity entity) {
        Vec3d velocity = entity.getVelocity();
        return new Vec3d(velocity.x, 0.0D, velocity.z);
    }

    private static Vec3d capHorizontal(Vec3d velocity, double cap) {
        double horizontal = horizontalLength(velocity);
        if (horizontal <= cap || horizontal <= 0.0D) {
            return new Vec3d(velocity.x, 0.0D, velocity.z);
        }

        double scale = cap / horizontal;
        return new Vec3d(velocity.x * scale, 0.0D, velocity.z * scale);
    }

    private static double horizontalLength(Vec3d velocity) {
        return Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
    }

    private static void cleanExpired(int currentAge) {
        Iterator<Map.Entry<AbstractMinecartEntity, Charge>> iterator = CHARGES.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<AbstractMinecartEntity, Charge> entry = iterator.next();
            AbstractMinecartEntity cart = entry.getKey();
            Charge charge = entry.getValue();
            if (cart == null || cart.removed || currentAge - charge.lastContactAge > CHARGE_EXPIRY_TICKS) {
                if (cart != null) {
                    CHARGE_SNAPSHOTS.remove(cart.getUuid());
                }
                iterator.remove();
            }
        }
    }

    private static final class Charge {
        private final Vec3d velocity;
        private final int lastContactAge;

        private Charge(Vec3d velocity, int lastContactAge) {
            this.velocity = velocity;
            this.lastContactAge = lastContactAge;
        }
    }
}
