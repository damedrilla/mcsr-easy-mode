package me.dota1g.mcsreasymode;

/** Immutable, world-coordinate opening shared by all chunks along a route. */
public final class NetherRouteShape {
    private final double[][] points;
    private final double minX, maxX, minZ, maxZ;

    public NetherRouteShape(double[][] points) {
        if (points.length < 2) throw new IllegalArgumentException("A route needs two endpoints");
        this.points = new double[points.length][3];
        double lowX = Double.POSITIVE_INFINITY, highX = Double.NEGATIVE_INFINITY;
        double lowZ = Double.POSITIVE_INFINITY, highZ = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < points.length; i++) {
            this.points[i] = points[i].clone();
            lowX = Math.min(lowX, points[i][0]); highX = Math.max(highX, points[i][0]);
            lowZ = Math.min(lowZ, points[i][2]); highZ = Math.max(highZ, points[i][2]);
        }
        this.minX = lowX - 28; this.maxX = highX + 28;
        this.minZ = lowZ - 28; this.maxZ = highZ + 28;
    }

    public boolean containsColumn(double x, double z) {
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

    public double openDensity(double original, double x, double y, double z) {
        // Keep the lava sea, lower terrain, and roof untouched.
        if (y < 40 || y > 104 || !containsColumn(x, z)) return original;
        double radius = 23 + 3 * Math.sin(x * 0.055 + z * 0.037);
        double distance = Double.POSITIVE_INFINITY;
        for (int i = 1; i < points.length; i++) {
            double[] a = points[i - 1], b = points[i];
            double dx = b[0] - a[0], dz = b[2] - a[2];
            double lengthSquared = dx * dx + dz * dz;
            double t = lengthSquared == 0 ? 0 : Math.max(0, Math.min(1,
                    ((x - a[0]) * dx + (z - a[2]) * dz) / lengthSquared));
            double ox = (x - a[0] - t * dx) / radius;
            double oz = (z - a[2] - t * dz) / radius;
            double oy = (y - a[1] - t * (b[1] - a[1])) / 22;
            distance = Math.min(distance, ox * ox + oz * oz + oy * oy);
        }
        if (distance >= 1) return original;
        // Smoothly blend at the boundary; the inner half is always open.
        double weight = Math.min(1, (1 - distance) * 2);
        weight = weight * weight * (3 - 2 * weight);
        return original + (Math.min(original, -48) - original) * weight;
    }
}
