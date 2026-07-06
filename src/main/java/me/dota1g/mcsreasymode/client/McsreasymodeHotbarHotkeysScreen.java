package me.dota1g.mcsreasymode.client;

import me.dota1g.mcsreasymode.McsreasymodeConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

import java.io.IOException;

public class McsreasymodeHotbarHotkeysScreen extends Screen {
    private final McsreasymodeConfig config;
    private final Screen parent;

    private ButtonWidget enabledButton;
    private ButtonWidget positionButton;
    private ButtonWidget textSizeButton;
    private ButtonWidget backgroundButton;
    private ButtonWidget handledPositionButton;
    private ButtonWidget handledTextSizeButton;
    private ButtonWidget handledBackgroundButton;

    public McsreasymodeHotbarHotkeysScreen(McsreasymodeConfig config, Screen parent) {
        super(new LiteralText("Hotbar Hotkeys"));
        this.config = config;
        this.parent = parent;
    }

    @Override
    protected void init() {
        int buttonWidth = Math.min(170, (this.width - 48) / 2);
        int leftX = this.width / 2 - buttonWidth - 6;
        int rightX = this.width / 2 + 6;
        int y = Math.max(120, this.height / 2 + 18);
        if (this.height < 260) {
            y = 112;
        }

        // this.enabledButton = this.addButton(new ButtonWidget(this.width / 2 - 100, y - 28, 200, 20, this.enabledText(), button -> {
        //     this.config.showHotbarHotkeys = !this.config.showHotbarHotkeys;
        //     button.setMessage(this.enabledText());
        //     this.updateControlStates();
        // }));

        this.positionButton = this.addButton(new ButtonWidget(leftX, y, buttonWidth, 20, this.positionText(), button -> {
            this.config.hotbarHotkeyPosition = this.config.hotbarHotkeyPosition.next();
            button.setMessage(this.positionText());
        }));

        this.textSizeButton = this.addButton(new ButtonWidget(leftX, y + 24, buttonWidth, 20, this.textSizeText(), button -> {
            this.config.hotbarHotkeyTextSize++;
            if (this.config.hotbarHotkeyTextSize > 4) {
                this.config.hotbarHotkeyTextSize = 1;
            }
            button.setMessage(this.textSizeText());
        }));

        this.backgroundButton = this.addButton(new ButtonWidget(leftX, y + 48, buttonWidth, 20, this.backgroundText(), button -> {
            this.config.hotbarHotkeyBackground = this.config.hotbarHotkeyBackground.next();
            button.setMessage(this.backgroundText());
        }));

        this.handledPositionButton = this.addButton(new ButtonWidget(rightX, y, buttonWidth, 20, this.handledPositionText(), button -> {
            this.config.handledHotbarHotkeyPosition = this.config.handledHotbarHotkeyPosition.next();
            button.setMessage(this.handledPositionText());
        }));

        this.handledTextSizeButton = this.addButton(new ButtonWidget(rightX, y + 24, buttonWidth, 20, this.handledTextSizeText(), button -> {
            this.config.handledHotbarHotkeyTextSize++;
            if (this.config.handledHotbarHotkeyTextSize > 4) {
                this.config.handledHotbarHotkeyTextSize = 1;
            }
            button.setMessage(this.handledTextSizeText());
        }));

        this.handledBackgroundButton = this.addButton(new ButtonWidget(rightX, y + 48, buttonWidth, 20, this.handledBackgroundText(), button -> {
            this.config.handledHotbarHotkeyBackground = this.config.handledHotbarHotkeyBackground.next();
            button.setMessage(this.handledBackgroundText());
        }));

        this.addButton(new ButtonWidget(this.width / 2 - 100, this.height - 27, 200, 20, ScreenTexts.DONE, button -> this.onClose()));
        this.updateControlStates();
    }

    private void updateControlStates() {
        boolean enabled = this.config.showHotbarHotkeys;
        this.positionButton.active = enabled;
        this.textSizeButton.active = enabled;
        this.backgroundButton.active = enabled;
        this.handledPositionButton.active = enabled;
        this.handledTextSizeButton.active = enabled;
        this.handledBackgroundButton.active = enabled;
    }

    // private LiteralText enabledText() {
    //     return new LiteralText("Hotbar Hotkeys: " + (this.config.showHotbarHotkeys ? "On" : "Off"));
    // }

    private LiteralText positionText() {
        return new LiteralText("HUD Corner: " + this.config.hotbarHotkeyPosition.displayName());
    }

    private LiteralText textSizeText() {
        return new LiteralText("HUD Size: " + this.config.hotbarHotkeyTextSize);
    }

    private LiteralText backgroundText() {
        return new LiteralText("HUD BG: " + this.config.hotbarHotkeyBackground.displayName());
    }

    private LiteralText handledPositionText() {
        return new LiteralText("Screen Corner: " + this.config.handledHotbarHotkeyPosition.displayName());
    }

    private LiteralText handledTextSizeText() {
        return new LiteralText("Screen Size: " + this.config.handledHotbarHotkeyTextSize);
    }

    private LiteralText handledBackgroundText() {
        return new LiteralText("Screen BG: " + this.config.handledHotbarHotkeyBackground.displayName());
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        this.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 18, 0xFFFFFF);
        int columnWidth = Math.min(220, Math.max(190, (this.width - 36) / 2));
        int leftPreviewX = this.width / 2 - columnWidth - 6 + (columnWidth - 182) / 2;
        int rightPreviewX = this.width / 2 + 6 + (columnWidth - 182) / 2;
        int labelY = 36;
        this.drawCenteredText(matrices, this.textRenderer, new LiteralText("Hotbars"), leftPreviewX + 91, labelY, 0xFFFFFF);
        this.drawCenteredText(matrices, this.textRenderer, new LiteralText("Inventory/Chest & Other Handled Screens"), rightPreviewX + 91, labelY, 0xFFFFFF);
        if (this.config.showHotbarHotkeys) {
            String[] labels = HotbarHotkeyLabels.get(this.client);
            HotbarHotkeyOverlayRenderer.renderPreviewAt(matrices, this.client, this.config, leftPreviewX, 50, labels);
            HotbarHotkeyOverlayRenderer.renderHandledPreviewAt(matrices, this.client, this.config, rightPreviewX, 50, labels);
        }
        super.render(matrices, mouseX, mouseY, delta);
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
