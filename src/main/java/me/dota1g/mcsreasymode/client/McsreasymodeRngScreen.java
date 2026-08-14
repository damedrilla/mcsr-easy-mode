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
        private static final int GROUP_HEADER_GAP = 16;
        private static final int GROUP_SPACING = 14;

        private final List<FeatureRow> rows = new ArrayList<>();
        private final List<RowGroup> groups = new ArrayList<>();
        private RowGroup currentGroup;
        private ButtonWidget toggleAllButton;
        private ButtonWidget stableRampartModeButton;

        private final McsreasymodeConfig config;
        private final Screen parent;
        private int contentLeft;
        private int contentRight;
        private int valueButtonX;
        private int valueButtonWidth;
        private int listTop;
        private int listBottom;
        private int scrollOffset;
        private int contentHeight;
        private int toggleAllY;
        private int featuresSectionY;
        private Text hoveredTooltip;

        public McsreasymodeRngScreen(McsreasymodeConfig config, Screen parent) {
            super(new TranslatableText("mcsreasymode.screen.rng.title"));
            this.config = config;
            this.parent = parent;
        }


        @Override
        protected void init() {
            this.rows.clear();
            this.groups.clear();
            int contentWidth = Math.min(640, this.width - 72);
            this.contentLeft = this.width / 2 - contentWidth / 2;
            this.contentRight = this.contentLeft + contentWidth;
            this.valueButtonWidth = Math.min(300, Math.max(150, contentWidth / 2));
            this.valueButtonX = this.contentRight - this.valueButtonWidth;
            this.listTop = 42;
            this.listBottom = this.height - 34;
            this.toggleAllY = 30;
            this.featuresSectionY = 60;
            this.scrollOffset = Math.min(this.scrollOffset, this.maxScroll());

            this.toggleAllButton = this.addButton(new ButtonWidget(this.valueButtonX, 0, this.valueButtonWidth, 20, this.toggleAllText(), button -> {
                this.config.setAllRngFeatures(!this.config.areAllRngFeaturesRanked());
                this.refreshMessages();
            }));

            this.addGroup("mcsreasymode.screen.rng.group.loot");
            this.addRow("mcsreasymode.screen.rng.iron_golem_drops", "mcsreasymode.screen.rng.iron_golem_drops.tooltip", new FeatureAccess() {
                public boolean get() { return McsreasymodeRngScreen.this.config.rankedIronGolemDrops; }
                public void set(boolean value) { McsreasymodeRngScreen.this.config.rankedIronGolemDrops = value; }
            });
            this.addRow("mcsreasymode.screen.rng.blaze_rods", "mcsreasymode.screen.rng.blaze_rods.tooltip", new FeatureAccess() {
                public boolean get() { return McsreasymodeRngScreen.this.config.rankedBlazeRods; }
                public void set(boolean value) { McsreasymodeRngScreen.this.config.rankedBlazeRods = value; }
            });
            this.addRow("mcsreasymode.screen.rng.piglin_barters", "mcsreasymode.screen.rng.piglin_barters.tooltip", new FeatureAccess() {
                public boolean get() { return McsreasymodeRngScreen.this.config.rankedPiglinBarters; }
                public void set(boolean value) { McsreasymodeRngScreen.this.config.rankedPiglinBarters = value; }
            });
            this.addRow("mcsreasymode.screen.rng.piglin_string", "mcsreasymode.screen.rng.piglin_string.tooltip", new FeatureAccess() {
                public boolean get() { return McsreasymodeRngScreen.this.config.rankedPiglinString; }
                public void set(boolean value) { McsreasymodeRngScreen.this.config.rankedPiglinString = value; }
            });
            this.addRow("mcsreasymode.screen.rng.flint", "mcsreasymode.screen.rng.flint.tooltip", new FeatureAccess() {
                public boolean get() { return McsreasymodeRngScreen.this.config.rankedFlint; }
                public void set(boolean value) { McsreasymodeRngScreen.this.config.rankedFlint = value; }
            });
            this.addRow("mcsreasymode.screen.rng.buried_treasure_chance", "mcsreasymode.screen.rng.buried_treasure_chance.tooltip", new FeatureAccess() {
                public boolean get() { return McsreasymodeRngScreen.this.config.rankedBuriedTreasureChance; }
                public void set(boolean value) { McsreasymodeRngScreen.this.config.rankedBuriedTreasureChance = value; }
            });
            this.addRow("mcsreasymode.screen.rng.bastion_chest_loot", "mcsreasymode.screen.rng.bastion_chest_loot.tooltip", new FeatureAccess() {
                public boolean get() { return McsreasymodeRngScreen.this.config.rankedBastionChestLoot; }
                public void set(boolean value) { McsreasymodeRngScreen.this.config.rankedBastionChestLoot = value; }
            });
            this.addRow("mcsreasymode.screen.rng.other_chest_loot_tables", "mcsreasymode.screen.rng.other_chest_loot_tables.tooltip", new FeatureAccess() {
                public boolean get() { return McsreasymodeRngScreen.this.config.rankedChestLootTables; }
                public void set(boolean value) { McsreasymodeRngScreen.this.config.rankedChestLootTables = value; }
            });

            this.addGroup("mcsreasymode.screen.rng.group.world_interaction");
            this.addRow("mcsreasymode.screen.rng.eye_breaks", "mcsreasymode.screen.rng.eye_breaks.tooltip", new FeatureAccess() {
                public boolean get() { return McsreasymodeRngScreen.this.config.rankedEyeBreaks; }
                public void set(boolean value) { McsreasymodeRngScreen.this.config.rankedEyeBreaks = value; }
            });
            this.addRow("mcsreasymode.screen.rng.blind_portal", "mcsreasymode.screen.rng.blind_portal.tooltip", new FeatureAccess() {
                public boolean get() { return McsreasymodeRngScreen.this.config.rankedBlindPortal; }
                public void set(boolean value) { McsreasymodeRngScreen.this.config.rankedBlindPortal = value; }
            });

            this.addGroup("mcsreasymode.screen.rng.group.bastion_standardization");
            this.addRow("mcsreasymode.screen.rng.bastion_spawn_protection", "mcsreasymode.screen.rng.bastion_spawn_protection.tooltip", new FeatureAccess() {
                public boolean get() { return McsreasymodeRngScreen.this.config.rankedBastionSpawnProtection; }
                public void set(boolean value) { McsreasymodeRngScreen.this.config.rankedBastionSpawnProtection = value; }
            });
            this.addRow("mcsreasymode.screen.rng.hoglin_stable_ramparts", "mcsreasymode.screen.rng.hoglin_stable_ramparts.tooltip", new FeatureAccess() {
                public boolean get() { return McsreasymodeRngScreen.this.config.rankedHoglinStableRamparts; }
                public void set(boolean value) { McsreasymodeRngScreen.this.config.rankedHoglinStableRamparts = value; }
            });
            this.stableRampartModeButton = this.addButton(new ButtonWidget(this.valueButtonX, 0, this.valueButtonWidth, 20, this.stableRampartModeText(), button -> {
                this.config.hoglinStableRampartMode = this.config.hoglinStableRampartMode.next();
                this.refreshMessages();
            }));
            FeatureRow modeRow = new FeatureRow(
                    new TranslatableText("mcsreasymode.screen.rng.hoglin_stable_ramparts_mode"),
                    new TranslatableText("mcsreasymode.screen.rng.hoglin_stable_ramparts_mode.tooltip"),
                    null,
                    this.stableRampartModeButton,
                    true
            );
            this.rows.add(modeRow);
            this.currentGroup.rows.add(modeRow);
            this.addRow("mcsreasymode.screen.rng.bridge_ramparts", "mcsreasymode.screen.rng.bridge_ramparts.tooltip", new FeatureAccess() {
                public boolean get() { return McsreasymodeRngScreen.this.config.rankedHoglinBridgeRamparts; }
                public void set(boolean value) { McsreasymodeRngScreen.this.config.rankedHoglinBridgeRamparts = value; }
            });
            this.rows.add(modeRow);
            this.currentGroup.rows.add(modeRow);
            this.addRow("mcsreasymode.screen.rng.rp_herd", "mcsreasymode.screen.rng.rp_herd.tooltip", new FeatureAccess() {
                public boolean get() { return McsreasymodeRngScreen.this.config.rankedRuinedPortalAnimals; }
                public void set(boolean value) { McsreasymodeRngScreen.this.config.rankedRuinedPortalAnimals = value; }
            });
            this.addButton(new ButtonWidget(this.width / 2 - 100, this.height - 27, 200, 20, ScreenTexts.DONE, button -> this.onClose()));
            this.updateContentHeight();
            this.updateRowPositions();
        }
        private void addRow(String labelKey, String tooltipKey, FeatureAccess access) {
            this.addRow(labelKey, tooltipKey, access, false);
        }

        private void addRow(String labelKey, String tooltipKey, FeatureAccess access, boolean indent) {
            ButtonWidget button = this.addButton(new ButtonWidget(this.valueButtonX, 0, this.valueButtonWidth, 20, this.featureText(access.get()), pressed -> {
                access.set(!access.get());
                this.config.updateRngModeFromFeatures();
                this.refreshMessages();
            }));
            FeatureRow row = new FeatureRow(new TranslatableText(labelKey), new TranslatableText(tooltipKey), access, button, indent);
            this.rows.add(row);
            this.currentGroup.rows.add(row);
        }
        private void addGroup(String labelKey) {
            this.currentGroup = new RowGroup(new TranslatableText(labelKey));
            this.groups.add(this.currentGroup);
        }
        private void refreshMessages() {
            this.config.updateRngModeFromFeatures();
            this.toggleAllButton.setMessage(this.toggleAllText());
            for (FeatureRow row : this.rows) {
                if (row.access != null) {
                    row.button.setMessage(this.featureText(row.access.get()));
                }
            }
            this.stableRampartModeButton.setMessage(this.stableRampartModeText());
            this.stableRampartModeButton.active = this.config.rankedHoglinStableRamparts;
            this.updateRowPositions();
        }

        private Text toggleAllText() {
            return this.config.areAllRngFeaturesRanked() ? this.rankedText() : this.vanillaText();
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
            return Math.max(0, this.contentHeight - (this.listBottom - this.listTop));
        }

        private void updateRowPositions() {
            this.setButtonY(this.toggleAllButton, this.toggleAllY);
            int contentY = this.featuresSectionY;
            for (RowGroup group : this.groups) {
                group.headerContentY = contentY;
                group.headerY = this.toScreenY(contentY);
                contentY += GROUP_HEADER_GAP;
                for (FeatureRow row : group.rows) {
                    row.contentY = contentY;
                    row.y = this.toScreenY(contentY);
                    row.button.y = row.y;
                    row.button.visible = row.y + 20 > this.listTop && row.y < this.listBottom;
                    contentY += ROW_HEIGHT;
                }
                contentY += GROUP_SPACING;
            }
        }

        @Override
        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            this.renderBackground(matrices);
            this.hoveredTooltip = null;
            this.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 18, 0xFFFFFF);
            fill(matrices, this.contentLeft - 14, this.listTop - 6, this.contentRight + 18, this.listBottom + 6, 0x66000000);
            this.drawRowLabel(matrices, new TranslatableText("mcsreasymode.screen.rng.mode", this.config.rngModeDisplayName()), this.toScreenY(6), mouseX, mouseY, null, false);
            this.drawRowLabel(matrices, new TranslatableText("mcsreasymode.screen.rng.toggle_all"), this.toScreenY(this.toggleAllY), mouseX, mouseY, null, false);

            for (RowGroup group : this.groups) {
                if (group.headerY + 12 > this.listTop && group.headerY < this.listBottom) {
                    this.drawSectionDivider(matrices, group.label, group.headerY);
                }
                for (FeatureRow row : group.rows) {
                    if (row.y + 20 > this.listTop && row.y < this.listBottom) {
                        this.drawRowLabel(matrices, row.label, row.y, mouseX, mouseY, row.tooltip, row.indent);
                        int labelIndent = row.indent ? 12 : 0;
                        if (McsreasymodeTooltip.isHovered(this.textRenderer, row.label.getString(), this.contentLeft + labelIndent, row.y + 6, mouseX, mouseY)) {
                            this.hoveredTooltip = row.tooltip;
                        }
                    }
                }
            }

            this.drawScrollBar(matrices);
            super.render(matrices, mouseX, mouseY, delta);
            this.renderHoveredTooltip(matrices, mouseX, mouseY);
        }
        private void drawRowLabel(MatrixStack matrices, Text label, int y, int mouseX, int mouseY, Text tooltip, boolean indent) {
            if (y + 20 <= this.listTop || y >= this.listBottom) {
                return;
            }

            int labelY = y + 6;
            int labelX = this.contentLeft + (indent ? 12 : 0);
            this.textRenderer.drawWithShadow(matrices, label, labelX, labelY, 0xFFFFFF);
            if (tooltip != null && McsreasymodeTooltip.isHovered(this.textRenderer, label.getString(), labelX, labelY, mouseX, mouseY)) {
                this.hoveredTooltip = tooltip;
            }
        }
        private void drawSectionDivider(MatrixStack matrices, Text label, int y) {
            if (y + 12 <= this.listTop || y >= this.listBottom) {
                return;
            }

            int centerX = this.width / 2;
            int labelWidth = this.textRenderer.getWidth(label);
            int labelX = centerX - labelWidth / 2;
            int lineY = y + 4;
            int leftEnd = labelX - 8;
            int rightStart = labelX + labelWidth + 8;
            if (leftEnd > this.contentLeft) {
                fill(matrices, this.contentLeft, lineY, leftEnd, lineY + 1, 0xFF666666);
            }
            if (rightStart < this.contentRight) {
                fill(matrices, rightStart, lineY, this.contentRight, lineY + 1, 0xFF666666);
            }
            this.drawCenteredText(matrices, this.textRenderer, label, centerX, y, 0xFFFFFF);
        }

        private void setButtonY(ButtonWidget button, int contentY) {
            int y = this.toScreenY(contentY);
            button.y = y;
            button.visible = y + 20 > this.listTop && y < this.listBottom;
        }

        private int toScreenY(int contentY) {
            return this.listTop + contentY - this.scrollOffset;
        }
        private void updateContentHeight() {
            int contentY = this.featuresSectionY;
            for (RowGroup group : this.groups) {
                contentY += GROUP_HEADER_GAP;
                contentY += group.rows.size() * ROW_HEIGHT;
                contentY += GROUP_SPACING;
            }
            this.contentHeight = contentY + 8;
            this.scrollOffset = Math.min(this.scrollOffset, this.maxScroll());
        }

        private void drawScrollBar(MatrixStack matrices) {
            int maxScroll = this.maxScroll();
            if (maxScroll <= 0) {
                return;
            }

            int trackX = this.contentRight + 8;
            int trackTop = this.listTop;
            int trackBottom = this.listBottom;
            int trackHeight = trackBottom - trackTop;
            int thumbHeight = Math.max(24, trackHeight * trackHeight / this.contentHeight);
            int thumbTravel = trackHeight - thumbHeight;
            int thumbY = trackTop + (thumbTravel <= 0 ? 0 : this.scrollOffset * thumbTravel / maxScroll);
            fill(matrices, trackX, trackTop, trackX + 4, trackBottom, 0x66000000);
            fill(matrices, trackX, thumbY, trackX + 4, thumbY + thumbHeight, 0xFFAAAAAA);
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
            private final boolean indent;
            private int contentY;
            private int y;

            private FeatureRow(Text label, Text tooltip, FeatureAccess access, ButtonWidget button, boolean indent) {
                this.label = label;
                this.tooltip = tooltip;
                this.access = access;
                this.button = button;
                this.indent = indent;
            }
        }


        private Text stableRampartModeText() {
            switch (this.config.hoglinStableRampartMode) {
                case DOUBLE_TRIPLE: return new TranslatableText("mcsreasymode.screen.rng.value.double_triple");
                case TRIPLE_TO_GAP: return new TranslatableText("mcsreasymode.screen.rng.value.triple_to_gap");
                default: return new TranslatableText("mcsreasymode.screen.rng.value.either");
            }
        }
        private static class RowGroup {
            private final Text label;
            private final List<FeatureRow> rows = new ArrayList<>();
            private int headerContentY;
            private int headerY;

            private RowGroup(Text label) {
                this.label = label;
            }
        }


    }
