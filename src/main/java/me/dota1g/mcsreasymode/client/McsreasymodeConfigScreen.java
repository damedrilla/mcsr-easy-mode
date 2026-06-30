package me.dota1g.mcsreasymode.client;

import me.dota1g.mcsreasymode.McsreasymodeConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.io.IOException;

public class McsreasymodeConfigScreen extends Screen {
    private final McsreasymodeConfig config;
    private final Screen parent;

    private ButtonWidget rngModeButton;
    private ButtonWidget piglinAggressionButton;
    private ButtonWidget ghastAggressionButton;
    private ButtonWidget hoglinAggressionButton;

    public McsreasymodeConfigScreen(McsreasymodeConfig config, Screen parent) {
        super(new LiteralText("MCSR Easy Mode"));
        this.config = config;
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = this.width / 2 - 100;
        int y = Math.max(42, this.height / 2 - 78);

        this.rngModeButton = this.addButton(new ButtonWidget(x, y, 200, 20, this.rngModeText(), button -> {
            this.config.rngMode = this.config.rngMode == McsreasymodeConfig.RngMode.VANILLA ? McsreasymodeConfig.RngMode.RANKED : McsreasymodeConfig.RngMode.VANILLA;
            button.setMessage(this.rngModeText());
        }));

        this.addButton(new ButtonWidget(x, y + 24, 200, 20, new LiteralText("Adjustments"), button -> {
            assert this.client != null;
            this.client.openScreen(new McsreasymodeAdjustmentsScreen(this));
        }));

        this.piglinAggressionButton = this.addButton(new ButtonWidget(x, y + 58, 200, 20, this.booleanText("Piglins", this.config.disablePiglinAggression), button -> {
            this.config.disablePiglinAggression = !this.config.disablePiglinAggression;
            button.setMessage(this.booleanText("Piglins", this.config.disablePiglinAggression));
        }));

        this.ghastAggressionButton = this.addButton(new ButtonWidget(x, y + 82, 200, 20, this.booleanText("Ghasts", this.config.disableGhastAggression), button -> {
            this.config.disableGhastAggression = !this.config.disableGhastAggression;
            button.setMessage(this.booleanText("Ghasts", this.config.disableGhastAggression));
        }));

        this.hoglinAggressionButton = this.addButton(new ButtonWidget(x, y + 106, 200, 20, this.booleanText("Hoglins", this.config.disableHoglinAggression), button -> {
            this.config.disableHoglinAggression = !this.config.disableHoglinAggression;
            button.setMessage(this.booleanText("Hoglins", this.config.disableHoglinAggression));
        }));

        this.addButton(new ButtonWidget(x, this.height - 27, 200, 20, ScreenTexts.DONE, button -> this.onClose()));
    }

    private Text rngModeText() {
        return new LiteralText("RNG: " + this.config.rngMode.displayName());
    }

    private Text booleanText(String label, boolean enabled) {
        return new LiteralText(label + " aggression: " + (enabled ? "Disabled" : "Vanilla"));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        this.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 18, 0xFFFFFF);
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
