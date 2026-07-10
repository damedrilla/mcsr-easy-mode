package me.dota1g.mcsreasymode.client;

import me.dota1g.mcsreasymode.McsreasymodeConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.StringRenderable;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class McsreasymodeRngScreen extends Screen {
    private static final int ROW_HEIGHT = 24;
    private static final int BUTTON_WIDTH = 92;

    private final McsreasymodeConfig config;
    private final Screen parent;
    private final List<FeatureRow> rows = new ArrayList<>();
    private ButtonWidget toggleAllButton;
    private int contentLeft;
    private int contentRight;
    private int listTop;
    private int listBottom;
    private int scrollOffset;
    private Text hoveredTooltip;

    public McsreasymodeRngScreen(McsreasymodeConfig config, Screen parent) {
        super(new TranslatableText("mcsreasymode.screen.rng.title"));
        this.config = config;
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.rows.clear();
        int contentWidth = Math.min(420, this.width - 36);
        this.contentLeft = this.width / 2 - contentWidth / 2;
        this.contentRight = this.contentLeft + contentWidth;
        this.listTop = 64;
        this.listBottom = this.height - 34;

        this.toggleAllButton = this.addButton(new ButtonWidget(this.contentRight - 132, 34, 132, 20, this.toggleAllText(), button -> {
            this.config.setAllRngFeatures(!this.config.areAllRngFeaturesRanked());
            this.refreshMessages();
        }));

        this.addRow("mcsreasymode.screen.rng.iron_golem_drops", "mcsreasymode.screen.rng.iron_golem_drops.tooltip", new FeatureAccess() {
            @Override
            public boolean get() {
                return McsreasymodeRngScreen.this.config.rankedIronGolemDrops;
            }

            @Override
            public void set(boolean value) {
                McsreasymodeRngScreen.this.config.rankedIronGolemDrops = value;
            }
        });
        this.addRow("mcsreasymode.screen.rng.eye_breaks", "mcsreasymode.screen.rng.eye_breaks.tooltip", new FeatureAccess() {
            @Override
            public boolean get() {
                return McsreasymodeRngScreen.this.config.rankedEyeBreaks;
            }

            @Override
            public void set(boolean value) {
                McsreasymodeRngScreen.this.config.rankedEyeBreaks = value;
            }
        });
        this.addRow("mcsreasymode.screen.rng.blaze_rods", "mcsreasymode.screen.rng.blaze_rods.tooltip", new FeatureAccess() {
            @Override
            public boolean get() {
                return McsreasymodeRngScreen.this.config.rankedBlazeRods;
            }

            @Override
            public void set(boolean value) {
                McsreasymodeRngScreen.this.config.rankedBlazeRods = value;
            }
        });
        this.addRow("mcsreasymode.screen.rng.piglin_barters", "mcsreasymode.screen.rng.piglin_barters.tooltip", new FeatureAccess() {
            @Override
            public boolean get() {
                return McsreasymodeRngScreen.this.config.rankedPiglinBarters;
            }

            @Override
            public void set(boolean value) {
                McsreasymodeRngScreen.this.config.rankedPiglinBarters = value;
            }
        });
        this.addRow("mcsreasymode.screen.rng.piglin_string", "mcsreasymode.screen.rng.piglin_string.tooltip", new FeatureAccess() {
            @Override
            public boolean get() {
                return McsreasymodeRngScreen.this.config.rankedPiglinString;
            }

            @Override
            public void set(boolean value) {
                McsreasymodeRngScreen.this.config.rankedPiglinString = value;
            }
        });
        this.addRow("mcsreasymode.screen.rng.flint", "mcsreasymode.screen.rng.flint.tooltip", new FeatureAccess() {
            @Override
            public boolean get() {
                return McsreasymodeRngScreen.this.config.rankedFlint;
            }

            @Override
            public void set(boolean value) {
                McsreasymodeRngScreen.this.config.rankedFlint = value;
            }
        });
        this.addRow("mcsreasymode.screen.rng.blind_portal", "mcsreasymode.screen.rng.blind_portal.tooltip", new FeatureAccess() {
            @Override
            public boolean get() {
                return McsreasymodeRngScreen.this.config.rankedBlindPortal;
            }

            @Override
            public void set(boolean value) {
                McsreasymodeRngScreen.this.config.rankedBlindPortal = value;
            }
        });
        this.addRow("mcsreasymode.screen.rng.bastion_chest_loot", "mcsreasymode.screen.rng.bastion_chest_loot.tooltip", new FeatureAccess() {
            @Override
            public boolean get() {
                return McsreasymodeRngScreen.this.config.rankedBastionChestLoot;
            }

            @Override
            public void set(boolean value) {
                McsreasymodeRngScreen.this.config.rankedBastionChestLoot = value;
            }
        });
        this.addRow("mcsreasymode.screen.rng.other_chest_loot_tables", "mcsreasymode.screen.rng.other_chest_loot_tables.tooltip", new FeatureAccess() {
            @Override
            public boolean get() {
                return McsreasymodeRngScreen.this.config.rankedChestLootTables;
            }

            @Override
            public void set(boolean value) {
                McsreasymodeRngScreen.this.config.rankedChestLootTables = value;
            }
        });
        this.addRow("mcsreasymode.screen.rng.hoglin_stable_ramparts", "mcsreasymode.screen.rng.hoglin_stable_ramparts.tooltip", new FeatureAccess() {
            @Override
            public boolean get() {
                return McsreasymodeRngScreen.this.config.rankedHoglinStableRamparts;
            }

            @Override
            public void set(boolean value) {
                McsreasymodeRngScreen.this.config.rankedHoglinStableRamparts = value;
            }
        });

        this.addButton(new ButtonWidget(this.width / 2 - 100, this.height - 27, 200, 20, ScreenTexts.DONE, button -> this.onClose()));
        this.updateRowPositions();
    }

    private void addRow(String labelKey, String tooltipKey, FeatureAccess access) {
        ButtonWidget button = this.addButton(new ButtonWidget(this.contentRight - BUTTON_WIDTH, 0, BUTTON_WIDTH, 20, this.featureText(access.get()), pressed -> {
            access.set(!access.get());
            this.config.updateRngModeFromFeatures();
            this.refreshMessages();
        }));
        this.rows.add(new FeatureRow(new TranslatableText(labelKey), new TranslatableText(tooltipKey), access, button));
    }

    private void refreshMessages() {
        this.config.updateRngModeFromFeatures();
        this.toggleAllButton.setMessage(this.toggleAllText());
        for (FeatureRow row : this.rows) {
            row.button.setMessage(this.featureText(row.access.get()));
        }
        this.updateRowPositions();
    }

    private LiteralText toggleAllText() {
        return new LiteralText(new TranslatableText("mcsreasymode.screen.rng.toggle_all").getString() + ": " + (this.config.areAllRngFeaturesRanked() ? this.rankedText().getString() : this.vanillaText().getString()));
    }

    private Text featureText(boolean ranked) {
        return ranked ? this.rankedText() : this.vanillaText();
    }

    private Text rankedText() {
        return new TranslatableText("mcsreasymode.screen.rng.value.ranked");
    }

    private Text vanillaText() {
        return new TranslatableText("mcsreasymode.screen.rng.value.vanilla");
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        int maxScroll = this.maxScroll();
        if (maxScroll <= 0 || mouseY < this.listTop || mouseY > this.listBottom) {
            return super.mouseScrolled(mouseX, mouseY, amount);
        }

        this.scrollOffset = Math.max(0, Math.min(maxScroll, this.scrollOffset - (int) (amount * ROW_HEIGHT)));
        this.updateRowPositions();
        return true;
    }

    private int maxScroll() {
        return Math.max(0, this.rows.size() * ROW_HEIGHT - (this.listBottom - this.listTop));
    }

    private void updateRowPositions() {
        int y = this.listTop - this.scrollOffset;
        for (FeatureRow row : this.rows) {
            row.y = y;
            row.button.y = y;
            row.button.visible = y + 20 > this.listTop && y < this.listBottom;
            y += ROW_HEIGHT;
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        this.hoveredTooltip = null;
        this.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 18, 0xFFFFFF);
        this.textRenderer.drawWithShadow(matrices, new TranslatableText("mcsreasymode.screen.rng.mode", this.config.rngModeDisplayName()), this.contentLeft, 40, 0xFFFFFF);

        for (FeatureRow row : this.rows) {
            if (row.y + 20 > this.listTop && row.y < this.listBottom) {
                this.textRenderer.drawWithShadow(matrices, row.label, this.contentLeft, row.y + 6, 0xFFFFFF);
                if (McsreasymodeTooltip.isHovered(this.textRenderer, row.label.getString(), this.contentLeft, row.y + 6, mouseX, mouseY)) {
                    this.hoveredTooltip = row.tooltip;
                }
            }
        }

        if (this.maxScroll() > 0) {
            this.textRenderer.drawWithShadow(matrices, new TranslatableText("mcsreasymode.screen.rng.scroll"), this.contentRight - 34, this.listBottom - 10, 0xA0A0A0);
        }

        super.render(matrices, mouseX, mouseY, delta);
        this.renderHoveredTooltip(matrices, mouseX, mouseY);
    }

    private void renderHoveredTooltip(MatrixStack matrices, int mouseX, int mouseY) {
        if (this.hoveredTooltip == null) {
            return;
        }

        List<StringRenderable> lines = McsreasymodeTooltip.wrap(this.textRenderer, this.hoveredTooltip);
        int y = McsreasymodeTooltip.clampedY(lines, mouseY, this.listTop, this.listBottom);
        this.renderTooltip(matrices, lines, mouseX, y);
    }

    @Override
    public void onClose() {
        assert this.client != null;
        this.client.openScreen(this.parent);
    }

    @Override
    public void removed() {
        this.config.updateRngModeFromFeatures();
        if (this.config.container != null) {
            try {
                this.config.container.save();
            } catch (IOException ignored) {
            }
        }
    }

    private interface FeatureAccess {
        boolean get();

        void set(boolean value);
    }

    private static class FeatureRow {
        private final Text label;
        private final Text tooltip;
        private final FeatureAccess access;
        private final ButtonWidget button;
        private int y;

        private FeatureRow(Text label, Text tooltip, FeatureAccess access, ButtonWidget button) {
            this.label = label;
            this.tooltip = tooltip;
            this.access = access;
            this.button = button;
        }
    }
}
