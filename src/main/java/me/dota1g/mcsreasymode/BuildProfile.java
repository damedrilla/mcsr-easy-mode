package me.dota1g.mcsreasymode;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** Packaged build profile; cannot be changed by a player configuration file. */
public final class BuildProfile {
    public static final boolean RELEASE = isRelease();

    private BuildProfile() {
    }

    private static boolean isRelease() {
        Properties properties = new Properties();
        try (InputStream stream = BuildProfile.class.getResourceAsStream("/mcsreasymode-build.properties")) {
            if (stream != null) properties.load(stream);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read build profile", exception);
        }
        return Boolean.parseBoolean(properties.getProperty("release", "false"));
    }
}
