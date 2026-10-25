package me.dota1g.mcsreasymode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Independent of options.txt and StandardSettings' keybinding resets. */
public final class PieChartKeyStore {
    private final Path file;

    public PieChartKeyStore(Path file) { this.file = file; }

    public String load(String fallback) throws IOException {
        if (!Files.exists(this.file)) return fallback;
        String key = new String(Files.readAllBytes(this.file), StandardCharsets.UTF_8).trim();
        if (!valid(key)) throw new IOException("Invalid saved pie shortcut key");
        return key;
    }

    public void save(String key) throws IOException {
        if (!valid(key)) throw new IOException("Invalid pie shortcut key");
        Files.createDirectories(this.file.getParent());
        Path temporary = Files.createTempFile(this.file.getParent(), "pie-key-", ".tmp");
        try {
            Files.write(temporary, key.getBytes(StandardCharsets.UTF_8));
            Files.move(temporary, this.file, StandardCopyOption.REPLACE_EXISTING);
        } finally { Files.deleteIfExists(temporary); }
    }

    private static boolean valid(String key) {
        return key != null && key.length() <= 128 && !key.contains("\n") && !key.contains("\r")
                && (key.startsWith("key.keyboard.") || key.startsWith("key.mouse.") || key.startsWith("scancode."));
    }
}
