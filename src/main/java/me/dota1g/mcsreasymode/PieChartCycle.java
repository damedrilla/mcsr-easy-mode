package me.dota1g.mcsreasymode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Cycles in configured order while respecting manual or world-join navigation. */
public final class PieChartCycle {
    private int lastIndex = -1;
    private String lastPath;

    public PieChartPath next(List<PieChartPath> configured, String current) {
        List<PieChartPath> valid = new ArrayList<>();
        if (configured != null) {
            for (PieChartPath entry : configured) {
                if (entry == null) continue;
                try { entry.internalPath(); valid.add(entry); }
                catch (IllegalArgumentException ignored) { }
            }
        }
        if (valid.isEmpty()) return null;
        int index = -1;
        if (Objects.equals(current, this.lastPath) && this.lastIndex >= 0 && this.lastIndex < valid.size()
                && valid.get(this.lastIndex).internalPath().equals(current)) {
            index = this.lastIndex;
        } else {
            for (int i = 0; i < valid.size(); i++) {
                if (valid.get(i).internalPath().equals(current)) { index = i; break; }
            }
        }
        this.lastIndex = (index + 1) % valid.size();
        PieChartPath target = valid.get(this.lastIndex);
        this.lastPath = target.internalPath();
        return target;
    }
}
