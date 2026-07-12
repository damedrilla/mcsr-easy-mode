package me.dota1g.mcsreasymode.client;

import me.dota1g.mcsreasymode.McsreasymodeConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.StringRenderable;
import net.minecraft.text.Text;

import java.io.IOException;
import java.util.List;

public class McsreasymodeConfigScreen extends Screen {
    private final McsreasymodeConfig config;
    private final Screen parent;

    private ButtonWidget rngModeButton;
    private ButtonWidget rngInfoButton;
    private ButtonWidget piglinAggressionButton;
    private ButtonWidget ghastAggressionButton;
    private ButtonWidget hoglinAggressionButton;
    private ButtonWidget moveSaveAndQuitButton;
    private ButtonWidget hotbarHotkeysButton;
    private ButtonWidget hotbarHotkeysCustomizeButton;
    private ButtonWidget debugChatLogsButton;
    private ButtonWidget hideAdvancementToastsButton;
    private ButtonWidget netherTerrainButton;
    private ButtonWidget villageStandardizationButton;
    private ButtonWidget handledHotkeysCustomizeButton;
    private int sectionLeftX;
    private int sectionRightX;
    private int valueButtonX;
    private int valueButtonWidth;
    private int rngSectionY;
    private int aggressionSectionY;
    private int uiSectionY;
    private Text hoveredTooltip;
    private int worldgenSectionY;
    private int listTop;
    private int listBottom;
    private int scrollOffset;
    private int contentHeight;

    public McsreasymodeConfigScreen(McsreasymodeConfig config, Screen parent) {
        super(new LiteralText("MCSR Easy Mode"));
        this.config = config;
        this.parent = parent;
    }

    @Override
    protected void init() {
        int contentWidth = Math.min(640, this.width - 72);
        int leftX = this.width / 2 - contentWidth / 2;
        int rightX = leftX + contentWidth;
        int infoButtonWidth = 20;
        int gap = 6;
        this.valueButtonWidth = Math.min(300, Math.max(150, contentWidth / 2));
        this.valueButtonX = rightX - this.valueButtonWidth;
        int rngValueButtonWidth = this.valueButtonWidth - infoButtonWidth - gap;
        int rngInfoButtonX = rightX - infoButtonWidth;
        int hotbarToggleWidth = (this.valueButtonWidth - gap) / 2;
        int hotbarCustomizeWidth = this.valueButtonWidth - gap - hotbarToggleWidth;
        this.listTop = 42;
        this.listBottom = this.height - 34;
        this.sectionLeftX = leftX;
        this.sectionRightX = rightX;
        this.rngSectionY = 6;
        this.aggressionSectionY = 62;
        this.uiSectionY = 154;
        this.worldgenSectionY = 269;
        this.contentHeight = this.worldgenSectionY + 70;
        this.scrollOffset = Math.min(this.scrollOffset, this.maxScroll());

        this.rngModeButton = this.addButton(new ButtonWidget(this.valueButtonX, 0, rngValueButtonWidth, 20, this.rngModeValueText(), button -> {
            assert this.client != null;
            this.client.openScreen(new McsreasymodeRngScreen(this.config, this));
        }));

        this.rngInfoButton = this.addButton(new ButtonWidget(rngInfoButtonX, 0, infoButtonWidth, 20, new LiteralText("i"), button -> {
            assert this.client != null;
            this.client.openScreen(new McsreasymodeAdjustmentsScreen(this));
        }));

        this.piglinAggressionButton = this.addButton(new ButtonWidget(this.valueButtonX, 0, this.valueButtonWidth, 20, this.aggressionValueText(this.config.disablePiglinAggression), button -> {
            this.config.disablePiglinAggression = !this.config.disablePiglinAggression;
            button.setMessage(this.aggressionValueText(this.config.disablePiglinAggression));
        }));

        this.ghastAggressionButton = this.addButton(new ButtonWidget(this.valueButtonX, 0, this.valueButtonWidth, 20, this.aggressionValueText(this.config.disableGhastAggression), button -> {
            this.config.disableGhastAggression = !this.config.disableGhastAggression;
            button.setMessage(this.aggressionValueText(this.config.disableGhastAggression));
        }));

        this.hoglinAggressionButton = this.addButton(new ButtonWidget(this.valueButtonX, 0, this.valueButtonWidth, 20, this.aggressionValueText(this.config.disableHoglinAggression), button -> {
            this.config.disableHoglinAggression = !this.config.disableHoglinAggression;
            button.setMessage(this.aggressionValueText(this.config.disableHoglinAggression));
        }));

        this.moveSaveAndQuitButton = this.addButton(new ButtonWidget(this.valueButtonX, 0, this.valueButtonWidth, 20, this.toggleValueText(this.config.moveSaveAndQuitButton), button -> {
            this.config.moveSaveAndQuitButton = !this.config.moveSaveAndQuitButton;
            button.setMessage(this.toggleValueText(this.config.moveSaveAndQuitButton));
        }));

        this.hotbarHotkeysButton = this.addButton(new ButtonWidget(this.valueButtonX, 0, hotbarToggleWidth, 20, this.toggleValueText(this.config.showHotbarHotkeys), button -> {
            this.config.showHotbarHotkeys = !this.config.showHotbarHotkeys;
            button.setMessage(this.toggleValueText(this.config.showHotbarHotkeys));
            this.hotbarHotkeysCustomizeButton.active = this.config.showHotbarHotkeys;
        }));

        this.hotbarHotkeysCustomizeButton = this.addButton(new ButtonWidget(this.valueButtonX + hotbarToggleWidth + gap, 0, hotbarCustomizeWidth, 20, new LiteralText("Customize"), button -> {
            assert this.client != null;
            this.client.openScreen(new McsreasymodeHotbarHotkeysScreen(this.config, this));
        }));
        this.hotbarHotkeysCustomizeButton.active = this.config.showHotbarHotkeys;

        this.debugChatLogsButton = this.addButton(new ButtonWidget(this.valueButtonX, 0, this.valueButtonWidth, 20, this.toggleValueText(this.config.showDebugChatLogs), button -> {
            this.config.showDebugChatLogs = !this.config.showDebugChatLogs;
            button.setMessage(this.toggleValueText(this.config.showDebugChatLogs));
        }));

        this.hideAdvancementToastsButton = this.addButton(new ButtonWidget(this.valueButtonX, 0, this.valueButtonWidth, 20, this.toggleValueText(this.config.hideAdvancementToasts), button -> {
            this.config.hideAdvancementToasts = !this.config.hideAdvancementToasts;
            button.setMessage(this.toggleValueText(this.config.hideAdvancementToasts));
        }));

        this.netherTerrainButton = this.addButton(new ButtonWidget(this.valueButtonX, 0, this.valueButtonWidth, 20, new LiteralText("Customize"), button -> {
            assert this.client != null;
            this.client.openScreen(new McsreasymodeNetherTerrainScreen(this.config, this));
        }));

        this.villageStandardizationButton = this.addButton(new ButtonWidget(this.valueButtonX, 0, this.valueButtonWidth, 20, this.toggleValueText(this.config.standardizeVillages), button -> {
            this.config.standardizeVillages = !this.config.standardizeVillages;
            button.setMessage(this.toggleValueText(this.config.standardizeVillages));
        }));

        this.addButton(new ButtonWidget(this.width / 2 - 100, this.height - 27, 200, 20, ScreenTexts.DONE, button -> this.onClose()));
        this.updateButtonPositions();
    }

    private Text rngModeValueText() {
        return new LiteralText(this.config.rngModeDisplayName());
    }

    private Text aggressionValueText(boolean enabled) {
        return new LiteralText(enabled ? "Disabled" : "Vanilla");
    }

    private Text toggleValueText(boolean enabled) {
        return new LiteralText(enabled ? "On" : "Off");
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        this.hoveredTooltip = null;
        this.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 18, 0xFFFFFF);
        fill(matrices, this.sectionLeftX - 14, this.listTop - 6, this.sectionRightX + 18, this.listBottom + 6, 0x66000000);
        this.drawSectionDivider(matrices, "RNG", this.toScreenY(this.rngSectionY));
        this.drawSectionDivider(matrices, "Anti-Aggression", this.toScreenY(this.aggressionSectionY));
        this.drawSectionDivider(matrices, "UI", this.toScreenY(this.uiSectionY));
        this.drawSectionDivider(matrices, "Worldgen", this.toScreenY(this.worldgenSectionY));
        this.drawRowLabel(matrices, "RNG", this.toScreenY(this.rngSectionY + 12), mouseX, mouseY, "Opens per-feature Vanilla or Ranked standardization toggles.");
        this.drawRowLabel(matrices, "Piglins", this.toScreenY(this.aggressionSectionY + 12), mouseX, mouseY, "Spoofs gold armor behavior so piglins stay neutral without taking an armor slot.");
        this.drawRowLabel(matrices, "Ghasts", this.toScreenY(this.aggressionSectionY + 33), mouseX, mouseY, "Prevents ghasts from choosing the player as an attack target.");
        this.drawRowLabel(matrices, "Hoglins", this.toScreenY(this.aggressionSectionY + 54), mouseX, mouseY, "Prevents hoglins from choosing the player as an attack target.");
        this.drawRowLabel(matrices, "Move Save & Quit", this.toScreenY(this.uiSectionY + 12), mouseX, mouseY, "Moves the pause menu Save & Quit button away from the usual misclick path.");
        this.drawRowLabel(matrices, "Hotbar Hotkeys", this.toScreenY(this.uiSectionY + 33), mouseX, mouseY, "Draws your hotbar and offhand keybind labels on the HUD and handled screens.");
        this.drawRowLabel(matrices, "Debug Chat Logs", this.toScreenY(this.uiSectionY + 54), mouseX, mouseY, "Echoes MCSR Easy Mode debug logs into in-game chat while keeping launcher logs enabled.");
        this.drawRowLabel(matrices, "Hide Advancement Toasts", this.toScreenY(this.uiSectionY + 75), mouseX, mouseY, "Prevents advancement popups from covering inventory and crafting screens.");
        this.drawRowLabel(matrices, "Nether Terrain Alpha", this.toScreenY(this.worldgenSectionY + 12), mouseX, mouseY, "Experimental Nether terrain controls for opening terrain while preserving vanilla behavior unless enabled.");
        this.drawRowLabel(matrices, "Village Standardization", this.toScreenY(this.worldgenSectionY + 33), mouseX, mouseY, "If a vanilla village has no smith, adds an artificial smith-style building and a nearby lava pool.");
        this.drawScrollBar(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.renderHoveredTooltip(matrices, mouseX, mouseY);
    }

    private void drawRowLabel(MatrixStack matrices, String label, int y, int mouseX, int mouseY, String tooltip) {
        if (y + 20 <= this.listTop || y >= this.listBottom) {
            return;
        }
        int labelY = y + 6;
        this.textRenderer.drawWithShadow(matrices, label, this.sectionLeftX, labelY, 0xFFFFFF);
        if (McsreasymodeTooltip.isHovered(this.textRenderer, label, this.sectionLeftX, labelY, mouseX, mouseY)) {
            this.hoveredTooltip = new LiteralText(tooltip);
        }
    }

    private void renderHoveredTooltip(MatrixStack matrices, int mouseX, int mouseY) {
        if (this.hoveredTooltip == null) {
            return;
        }

        List<StringRenderable> lines = McsreasymodeTooltip.wrap(this.textRenderer, this.hoveredTooltip);
        int y = McsreasymodeTooltip.clampedY(lines, mouseY, 24, this.height - 32);
        this.renderTooltip(matrices, lines, mouseX, y);
    }

    private void drawSectionDivider(MatrixStack matrices, String label, int y) {
        if (y + 12 <= this.listTop || y >= this.listBottom) {
            return;
        }
        int centerX = this.width / 2;
        int labelWidth = this.textRenderer.getWidth(label);
        int labelX = centerX - labelWidth / 2;
        int lineY = y + 4;
        int leftEnd = labelX - 8;
        int rightStart = labelX + labelWidth + 8;
        if (leftEnd > this.sectionLeftX) {
            fill(matrices, this.sectionLeftX, lineY, leftEnd, lineY + 1, 0xFF666666);
        }
        if (rightStart < this.sectionRightX) {
            fill(matrices, rightStart, lineY, this.sectionRightX, lineY + 1, 0xFF666666);
        }
        this.drawCenteredText(matrices, this.textRenderer, new LiteralText(label), centerX, y, 0xFFFFFF);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        int maxScroll = this.maxScroll();
        if (maxScroll <= 0 || mouseY < this.listTop || mouseY > this.listBottom) {
            return super.mouseScrolled(mouseX, mouseY, amount);
        }

        this.scrollOffset = Math.max(0, Math.min(maxScroll, this.scrollOffset - (int) (amount * 24)));
        this.updateButtonPositions();
        return true;
    }

    private void updateButtonPositions() {
        this.setButtonY(this.rngModeButton, this.rngSectionY + 12);
        this.setButtonY(this.rngInfoButton, this.rngSectionY + 12);
        this.setButtonY(this.piglinAggressionButton, this.aggressionSectionY + 12);
        this.setButtonY(this.ghastAggressionButton, this.aggressionSectionY + 33);
        this.setButtonY(this.hoglinAggressionButton, this.aggressionSectionY + 54);
        this.setButtonY(this.moveSaveAndQuitButton, this.uiSectionY + 12);
        this.setButtonY(this.hotbarHotkeysButton, this.uiSectionY + 33);
        this.setButtonY(this.hotbarHotkeysCustomizeButton, this.uiSectionY + 33);
        this.setButtonY(this.debugChatLogsButton, this.uiSectionY + 54);
        this.setButtonY(this.hideAdvancementToastsButton, this.uiSectionY + 75);
        this.setButtonY(this.netherTerrainButton, this.worldgenSectionY + 12);
        this.setButtonY(this.villageStandardizationButton, this.worldgenSectionY + 33);
    }

    private void setButtonY(ButtonWidget button, int contentY) {
        int y = this.toScreenY(contentY);
        button.y = y;
        button.visible = y + 20 > this.listTop && y < this.listBottom;
    }

    private int toScreenY(int contentY) {
        return this.listTop + contentY - this.scrollOffset;
    }

    private int maxScroll() {
        return Math.max(0, this.contentHeight - (this.listBottom - this.listTop));
    }

    private void drawScrollBar(MatrixStack matrices) {
        int maxScroll = this.maxScroll();
        if (maxScroll <= 0) {
            return;
        }

        int trackX = this.sectionRightX + 8;
        int trackTop = this.listTop;
        int trackBottom = this.listBottom;
        int trackHeight = trackBottom - trackTop;
        int thumbHeight = Math.max(24, trackHeight * trackHeight / this.contentHeight);
        int thumbTravel = trackHeight - thumbHeight;
        int thumbY = trackTop + (thumbTravel <= 0 ? 0 : this.scrollOffset * thumbTravel / maxScroll);
        fill(matrices, trackX, trackTop, trackX + 4, trackBottom, 0x66000000);
        fill(matrices, trackX, thumbY, trackX + 4, thumbY + thumbHeight, 0xFFAAAAAA);
    }

    @Override
    public void onClose() {
        assert this.client != null;
        this.client.openScreen(this.parent);
    }

    @Override
    public void removed() {
        if (this.config.container != null) {
            try {
                this.config.container.save();
            } catch (IOException ignored) {
            }
        }
    }
}
