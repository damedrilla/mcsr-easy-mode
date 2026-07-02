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

    public McsreasymodeHotbarHotkeysScreen(McsreasymodeConfig config, Screen parent) {
        super(new LiteralText("Hotbar Hotkeys"));
        this.config = config;
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = this.width / 2 - 100;
        int y = Math.max(76, this.height / 2 - 34);

        this.enabledButton = this.addButton(new ButtonWidget(x, y, 200, 20, this.enabledText(), button -> {
            this.config.showHotbarHotkeys = !this.config.showHotbarHotkeys;
            button.setMessage(this.enabledText());
            this.updateControlStates();
        }));

        this.positionButton = this.addButton(new ButtonWidget(x, y + 24, 200, 20, this.positionText(), button -> {
            this.config.hotbarHotkeyPosition = this.config.hotbarHotkeyPosition.next();
            button.setMessage(this.positionText());
        }));

        this.textSizeButton = this.addButton(new ButtonWidget(x, y + 48, 200, 20, this.textSizeText(), button -> {
            this.config.hotbarHotkeyTextSize++;
            if (this.config.hotbarHotkeyTextSize > 4) {
                this.config.hotbarHotkeyTextSize = 1;
            }
            button.setMessage(this.textSizeText());
        }));

        this.backgroundButton = this.addButton(new ButtonWidget(x, y + 72, 200, 20, this.backgroundText(), button -> {
            this.config.hotbarHotkeyBackground = this.config.hotbarHotkeyBackground.next();
            button.setMessage(this.backgroundText());
        }));

        this.addButton(new ButtonWidget(x, this.height - 27, 200, 20, ScreenTexts.DONE, button -> this.onClose()));
        this.updateControlStates();
    }

    private void updateControlStates() {
        boolean enabled = this.config.showHotbarHotkeys;
        this.positionButton.active = enabled;
        this.textSizeButton.active = enabled;
        this.backgroundButton.active = enabled;
    }

    private LiteralText enabledText() {
        return new LiteralText("Hotbar Hotkeys: " + (this.config.showHotbarHotkeys ? "On" : "Off"));
    }

    private LiteralText positionText() {
        return new LiteralText("Label Corner: " + this.config.hotbarHotkeyPosition.displayName());
    }

    private LiteralText textSizeText() {
        return new LiteralText("Text Size: " + this.config.hotbarHotkeyTextSize);
    }

    private LiteralText backgroundText() {
        return new LiteralText("Background: " + this.config.hotbarHotkeyBackground.displayName());
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        this.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 18, 0xFFFFFF);
        if (this.config.showHotbarHotkeys) {
            HotbarHotkeyOverlayRenderer.renderPreview(matrices, this.client, this.config, this.width, 45, HotbarHotkeyLabels.get(this.client));
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
