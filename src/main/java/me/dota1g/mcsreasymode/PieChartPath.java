package me.dota1g.mcsreasymode;

/** User-facing dotted path and optional name, independent of current profiler contents. */
public final class PieChartPath {
    public String name;
    public String path;

    public PieChartPath(String name, String path) {
        this.name = name;
        this.path = normalize(path);
    }

    public static String normalize(String input) {
        if (input == null || input.trim().isEmpty()) throw new IllegalArgumentException("Enter a path starting with root.");
        if (input.length() > 512) throw new IllegalArgumentException("Path must be at most 512 characters.");
        String[] parts = input.replace('\u001e', '.').split("\\.", -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
            if (parts[i].isEmpty()) throw new IllegalArgumentException("Path sections cannot be empty.");
            for (int j = 0; j < parts[i].length(); j++) {
                if (Character.isISOControl(parts[i].charAt(j))) throw new IllegalArgumentException("Path contains a control character.");
            }
        }
        if (!"root".equals(parts[0])) throw new IllegalArgumentException("Path must start with root.");
        return String.join(".", parts);
    }

    public String internalPath() {
        return normalize(this.path).replace('.', '\u001e');
    }

    public String label() {
        return this.name == null || this.name.trim().isEmpty() ? normalize(this.path) : this.name.trim();
    }
}
