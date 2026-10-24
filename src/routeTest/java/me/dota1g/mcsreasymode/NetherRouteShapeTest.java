package me.dota1g.mcsreasymode;

/** No external test library: run with the routeShapeTest Gradle task. */
public final class NetherRouteShapeTest {
    public static void main(String[] args) {
        double[][] points = {{-80, 64, -60}, {-32, 80, 12}, {16, 64, -20}, {80, 64, 60}};
        NetherRouteShape route = new NetherRouteShape(points);
        // Even after interpolation of Minecraft's 4x8x4 noise grid, bends and rises stay open.
        for (int i = 1; i < points.length; i++) {
            for (int step = 0; step <= 100; step++) {
                double t = step / 100.0;
                double x = points[i - 1][0] + t * (points[i][0] - points[i - 1][0]);
                double y = points[i - 1][1] + t * (points[i][1] - points[i - 1][1]);
                double z = points[i - 1][2] + t * (points[i][2] - points[i - 1][2]);
                require(interpolated(route, x, y, z) < 0, "Blocked route after noise interpolation");
            }
        }
        for (int x = -128; x <= 128; x += 4) {
            for (int z = -128; z <= 128; z += 4) {
                for (int y = 0; y <= 128; y += 8) {
                    for (double original : new double[]{-400, -1, 0, 100, 400}) {
                        double result = route.openDensity(original, x, y, z);
                        require(result <= original, "Opening added solid terrain");
                        if (y < 40 || y > 104) require(result == original, "Changed protected vertical bands");
                    }
                }
            }
        }
        require(route.openDensity(100, 200, 64, 200) == 100, "Changed distant terrain");
        double before = route.openDensity(100, -80, 64, -60);
        points[0][0] = 10000;
        require(route.openDensity(100, -80, 64, -60) == before, "Route was mutable");
        NetherRouteShape shortRoute = new NetherRouteShape(new double[][]{{0, 64, 0}, {0, 64, 0}});
        require(shortRoute.openDensity(400, 0, 64, 0) < 0, "Coincident endpoints failed");
        // Boundary blending should not introduce a sudden density step.
        NetherRouteShape straight = new NetherRouteShape(new double[][]{{-100, 64, 0}, {100, 64, 0}});
        for (double z = 10; z < 40; z += 0.01) {
            require(Math.abs(straight.openDensity(100, 0, 64, z + 0.01)
                    - straight.openDensity(100, 0, 64, z)) < 1, "Sharp density boundary");
        }
        System.out.println("Nether route shape checks passed (interpolated clearance, locality, protected bands, blending).");
    }

    private static double interpolated(NetherRouteShape route, double x, double y, double z) {
        int nx = (int) Math.floor(x / 4) * 4, ny = (int) Math.floor(y / 8) * 8, nz = (int) Math.floor(z / 4) * 4;
        double value = 0;
        for (int a = 0; a < 2; a++) for (int b = 0; b < 2; b++) for (int c = 0; c < 2; c++) {
            double weight = (a == 0 ? 1 - (x - nx) / 4 : (x - nx) / 4)
                    * (b == 0 ? 1 - (y - ny) / 8 : (y - ny) / 8)
                    * (c == 0 ? 1 - (z - nz) / 4 : (z - nz) / 4);
            value += weight * route.openDensity(400, nx + a * 4, ny + b * 8, nz + c * 4);
        }
        return value;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
