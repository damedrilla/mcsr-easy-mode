package me.dota1g.mcsreasymode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class PieChartPathsTest {
    public static void main(String[] args) throws Exception {
        PieChartPath a = new PieChartPath("Entities", " root . gameRenderer . level . entities ");
        PieChartPath b = new PieChartPath("", "root.tick.level.entities.blockEntities");
        require(a.path.equals("root.gameRenderer.level.entities"), "Whitespace normalization");
        require(a.internalPath().equals("root\u001egameRenderer\u001elevel\u001eentities"), "Profiler separator");
        require(new PieChartPath("", a.internalPath()).path.equals(a.path), "Internal path import");
        require(b.label().equals(b.path), "Optional name fallback");
        require(new PieChartPath("Custom", "root.tick.level.entities.tick.minecraft:piglin").path.endsWith("minecraft:piglin"), "Custom profiler names");
        for (String invalid : new String[]{"", "tick.level", "root..tick", "root.", ".root", "root.tick\u0000bad"}) {
            try { PieChartPath.normalize(invalid); throw new AssertionError("Accepted invalid path: " + invalid); }
            catch (IllegalArgumentException expected) { }
        }
        List<PieChartPath> paths = new ArrayList<>(Arrays.asList(a,b));
        PieChartCycle cycle = new PieChartCycle();
        require(cycle.next(paths,"root") == a, "First press");
        require(cycle.next(paths,a.internalPath()) == b, "Next press");
        require(cycle.next(paths,b.internalPath()) == a, "Wraparound");
        require(cycle.next(paths,"root.tick") == a, "Manual navigation reset");
        require(cycle.next(paths,b.internalPath()) == a, "StandardSettings path reset");
        Collections.swap(paths,0,1);
        require(cycle.next(paths,"root") == b, "Reordering");
        PieChartPath bad = new PieChartPath("bad","root"); bad.path="wrong";
        paths.add(0,bad); paths.add(null);
        require(cycle.next(paths,b.internalPath()) == a, "Invalid file entry skipping");
        require(cycle.next(Collections.emptyList(),"root") == null, "Empty list");
        require(cycle.next(null,"root") == null, "Missing list");
        List<PieChartPath> duplicates=Arrays.asList(a,a,b);
        cycle=new PieChartCycle();
        require(cycle.next(duplicates,"root")==a, "Duplicate first");
        require(cycle.next(duplicates,a.internalPath())==a, "Duplicate second");
        require(cycle.next(duplicates,a.internalPath())==b, "Duplicate does not trap cycle");
        java.nio.file.Path directory = java.nio.file.Files.createTempDirectory("pie-key-test-");
        java.nio.file.Path keyFile = directory.resolve("key.txt");
        try {
            PieChartKeyStore keys = new PieChartKeyStore(keyFile);
            require(keys.load("key.keyboard.unknown").equals("key.keyboard.unknown"), "Missing key fallback");
            for (String key : new String[]{"key.keyboard.p", "key.mouse.4", "scancode.42", "key.keyboard.unknown"}) {
                keys.save(key);
                require(new PieChartKeyStore(keyFile).load("key.keyboard.x").equals(key), "Key reload: " + key);
            }
            keys.save("key.keyboard.p");
            try { keys.save("bad key"); throw new AssertionError("Accepted invalid key"); }
            catch (java.io.IOException expected) { }
            require(keys.load("key.keyboard.unknown").equals("key.keyboard.p"), "Failed save lost key");
        } finally {
            java.nio.file.Files.deleteIfExists(keyFile);
            java.nio.file.Files.deleteIfExists(directory);
        }
        System.out.println("Pie chart path checks passed (custom paths, validation, cycling, manual resets, reorder, empty lists).");
    }
    private static void require(boolean condition,String message) {
        if (!condition) throw new AssertionError(message);
    }
}
