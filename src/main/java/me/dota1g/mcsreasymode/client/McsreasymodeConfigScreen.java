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
    private ButtonWidget piglinAggressionButton;
    private ButtonWidget ghastAggressionButton;
    private ButtonWidget hoglinAggressionButton;
    private ButtonWidget moveSaveAndQuitButton;
    private ButtonWidget hotbarHotkeysButton;
    private ButtonWidget hotbarHotkeysCustomizeButton;
    private ButtonWidget handledHotkeysCustomizeButton;
    private int sectionLeftX;
    private int sectionRightX;
    private int valueButtonX;
    private int valueButtonWidth;
    private int rngSectionY;
    private int aggressionSectionY;
    private int uiSectionY;
    private Text hoveredTooltip;

    public McsreasymodeConfigScreen(McsreasymodeConfig config, Screen parent) {
        super(new LiteralText("MCSR Easy Mode"));
        this.config = config;
        this.parent = parent;
    }

    @Override
    protected void init() {
        int contentWidth = Math.min(420, this.width - 36);
        int leftX = this.width / 2 - contentWidth / 2;
        int rightX = leftX + contentWidth;
        int infoButtonWidth = 20;
        int gap = 4;
        this.valueButtonWidth = Math.min(180, Math.max(132, contentWidth / 2));
        this.valueButtonX = rightX - this.valueButtonWidth;
        int rngValueButtonWidth = this.valueButtonWidth - infoButtonWidth - gap;
        int rngInfoButtonX = rightX - infoButtonWidth;
        int hotbarToggleWidth = Math.min(179, Math.max(132, contentWidth / 2) )/ 2;
        int hotbarCustomizeWidth = Math.min(179, Math.max(132, contentWidth / 2) )/ 2;
        int y = Math.max(28, this.height / 2 - 116);
        if (this.height < 300) {
            y = 35;
        }
        this.sectionLeftX = leftX;
        this.sectionRightX = rightX;
        this.rngSectionY = y;
        this.aggressionSectionY = y + 34;
        this.uiSectionY = y + 106;

        this.rngModeButton = this.addButton(new ButtonWidget(this.valueButtonX, this.rngSectionY + 12, rngValueButtonWidth, 20, this.rngModeValueText(), button -> {
            assert this.client != null;
            this.client.openScreen(new McsreasymodeRngScreen(this.config, this));
        }));

        this.addButton(new ButtonWidget(rngInfoButtonX, this.rngSectionY + 12, infoButtonWidth, 20, new LiteralText("i"), button -> {
            assert this.client != null;
            this.client.openScreen(new McsreasymodeAdjustmentsScreen(this));
        }));

        this.piglinAggressionButton = this.addButton(new ButtonWidget(this.valueButtonX, this.aggressionSectionY + 12, this.valueButtonWidth, 20, this.aggressionValueText(this.config.disablePiglinAggression), button -> {
            this.config.disablePiglinAggression = !this.config.disablePiglinAggression;
            button.setMessage(this.aggressionValueText(this.config.disablePiglinAggression));
        }));

        this.ghastAggressionButton = this.addButton(new ButtonWidget(this.valueButtonX, this.aggressionSectionY + 33, this.valueButtonWidth, 20, this.aggressionValueText(this.config.disableGhastAggression), button -> {
            this.config.disableGhastAggression = !this.config.disableGhastAggression;
            button.setMessage(this.aggressionValueText(this.config.disableGhastAggression));
        }));

        this.hoglinAggressionButton = this.addButton(new ButtonWidget(this.valueButtonX, this.aggressionSectionY + 54, this.valueButtonWidth, 20, this.aggressionValueText(this.config.disableHoglinAggression), button -> {
            this.config.disableHoglinAggression = !this.config.disableHoglinAggression;
            button.setMessage(this.aggressionValueText(this.config.disableHoglinAggression));
        }));

        this.moveSaveAndQuitButton = this.addButton(new ButtonWidget(this.valueButtonX, this.uiSectionY + 12, this.valueButtonWidth, 20, this.toggleValueText(this.config.moveSaveAndQuitButton), button -> {
            this.config.moveSaveAndQuitButton = !this.config.moveSaveAndQuitButton;
            button.setMessage(this.toggleValueText(this.config.moveSaveAndQuitButton));
        }));

        this.hotbarHotkeysButton = this.addButton(new ButtonWidget(this.valueButtonX, this.uiSectionY + 33, hotbarToggleWidth, 20, this.toggleValueText(this.config.showHotbarHotkeys), button -> {
            this.config.showHotbarHotkeys = !this.config.showHotbarHotkeys;
            button.setMessage(this.toggleValueText(this.config.showHotbarHotkeys));
            this.hotbarHotkeysCustomizeButton.active = this.config.showHotbarHotkeys;
        }));

        this.hotbarHotkeysCustomizeButton = this.addButton(new ButtonWidget(this.valueButtonX + hotbarToggleWidth + gap, this.uiSectionY + 33, hotbarCustomizeWidth, 20, new LiteralText("Customize"), button -> {
            assert this.client != null;
            this.client.openScreen(new McsreasymodeHotbarHotkeysScreen(this.config, this));
        }));
        this.hotbarHotkeysCustomizeButton.active = this.config.showHotbarHotkeys;


        this.addButton(new ButtonWidget(this.width / 2 - 100, this.height - 27, 200, 20, ScreenTexts.DONE, button -> this.onClose()));
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
        this.drawSectionDivider(matrices, "RNG", this.rngSectionY);
        this.drawSectionDivider(matrices, "Anti-Aggression", this.aggressionSectionY);
        this.drawSectionDivider(matrices, "UI", this.uiSectionY);
        this.drawRowLabel(matrices, "RNG", this.rngSectionY + 12, mouseX, mouseY, "Opens per-feature Vanilla or Ranked standardization toggles.");
        this.drawRowLabel(matrices, "Piglins", this.aggressionSectionY + 12, mouseX, mouseY, "Spoofs gold armor behavior so piglins stay neutral without taking an armor slot.");
        this.drawRowLabel(matrices, "Ghasts", this.aggressionSectionY + 33, mouseX, mouseY, "Prevents ghasts from choosing the player as an attack target.");
        this.drawRowLabel(matrices, "Hoglins", this.aggressionSectionY + 54, mouseX, mouseY, "Prevents hoglins from choosing the player as an attack target.");
        this.drawRowLabel(matrices, "Move Save & Quit", this.uiSectionY + 12, mouseX, mouseY, "Moves the pause menu Save & Quit button away from the usual misclick path.");
        this.drawRowLabel(matrices, "Hotbar Hotkeys", this.uiSectionY + 33, mouseX, mouseY, "Draws your hotbar and offhand keybind labels on the HUD and handled screens.");
        super.render(matrices, mouseX, mouseY, delta);
        this.renderHoveredTooltip(matrices, mouseX, mouseY);
    }

    private void drawRowLabel(MatrixStack matrices, String label, int y, int mouseX, int mouseY, String tooltip) {
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
