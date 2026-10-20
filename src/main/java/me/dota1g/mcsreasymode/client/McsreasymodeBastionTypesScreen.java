package me.dota1g.mcsreasymode.client;

import me.dota1g.mcsreasymode.McsreasymodeConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;

import java.io.IOException;

public class McsreasymodeBastionTypesScreen extends Screen {
    private final McsreasymodeConfig config;
    private final Screen parent;
    private ButtonWidget housingButton;
    private ButtonWidget stablesButton;
    private ButtonWidget treasureButton;
    private ButtonWidget bridgeButton;
    private ButtonWidget enableAllButton;

    public McsreasymodeBastionTypesScreen(McsreasymodeConfig config, Screen parent) {
        super(new TranslatableText("mcsreasymode.screen.bastion_types.title"));
        this.config = config;
        this.parent = parent;
    }

    @Override
    protected void init() {
        int width = Math.min(360, this.width - 36);
        int x = this.width / 2 - width / 2;
        int y = Math.max(42, this.height / 2 - 72);

        this.housingButton = this.addButton(new ButtonWidget(x, y, width, 20, this.typeText("housing", this.config.bastionHousingEnabled), button -> {
            this.config.bastionHousingEnabled = !this.config.bastionHousingEnabled;
            this.refreshButtons();
        }));
        this.stablesButton = this.addButton(new ButtonWidget(x, y + 24, width, 20, this.typeText("stables", this.config.bastionStablesEnabled), button -> {
            this.config.bastionStablesEnabled = !this.config.bastionStablesEnabled;
            this.refreshButtons();
        }));
        this.treasureButton = this.addButton(new ButtonWidget(x, y + 48, width, 20, this.typeText("treasure", this.config.bastionTreasureEnabled), button -> {
            this.config.bastionTreasureEnabled = !this.config.bastionTreasureEnabled;
            this.refreshButtons();
        }));
        this.bridgeButton = this.addButton(new ButtonWidget(x, y + 72, width, 20, this.typeText("bridge", this.config.bastionBridgeEnabled), button -> {
            this.config.bastionBridgeEnabled = !this.config.bastionBridgeEnabled;
            this.refreshButtons();
        }));
        this.enableAllButton = this.addButton(new ButtonWidget(x, y + 104, width, 20, new TranslatableText("mcsreasymode.screen.bastion_types.enable_all"), button -> {
            this.config.setAllBastionTypesEnabled(true);
            this.refreshButtons();
        }));
        this.addButton(new ButtonWidget(this.width / 2 - 100, this.height - 27, 200, 20, ScreenTexts.DONE, button -> this.onClose()));
        this.refreshButtons();
    }

    private void refreshButtons() {
        int enabledCount = this.config.enabledBastionTypeCount();
        this.housingButton.setMessage(this.typeText("housing", this.config.bastionHousingEnabled));
        this.stablesButton.setMessage(this.typeText("stables", this.config.bastionStablesEnabled));
        this.treasureButton.setMessage(this.typeText("treasure", this.config.bastionTreasureEnabled));
        this.bridgeButton.setMessage(this.typeText("bridge", this.config.bastionBridgeEnabled));
        this.housingButton.active = !this.config.bastionHousingEnabled || enabledCount > 1;
        this.stablesButton.active = !this.config.bastionStablesEnabled || enabledCount > 1;
        this.treasureButton.active = !this.config.bastionTreasureEnabled || enabledCount > 1;
        this.bridgeButton.active = !this.config.bastionBridgeEnabled || enabledCount > 1;
        this.enableAllButton.active = !this.config.areAllBastionTypesEnabled();
    }

    private TranslatableText typeText(String type, boolean enabled) {
        return new TranslatableText(
                "mcsreasymode.screen.bastion_types.option",
                new TranslatableText("mcsreasymode.screen.bastion_types." + type),
                new TranslatableText(enabled ? "mcsreasymode.screen.bastion_types.on" : "mcsreasymode.screen.bastion_types.off")
        );
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
