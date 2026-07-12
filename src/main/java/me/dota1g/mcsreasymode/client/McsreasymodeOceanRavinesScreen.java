package me.dota1g.mcsreasymode.client;

import me.dota1g.mcsreasymode.McsreasymodeConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.MathHelper;

import java.io.IOException;
import java.util.Locale;
import java.util.function.DoubleConsumer;

public class McsreasymodeOceanRavinesScreen extends Screen {
    private static final double AREG_RARITY = 0.04D;
    private static final double AREG_WIDTH = 5.0D;

    private final McsreasymodeConfig config;
    private final Screen parent;

    public McsreasymodeOceanRavinesScreen(McsreasymodeConfig config, Screen parent) {
        super(new LiteralText("Ocean Ravines Alpha"));
        this.config = config;
        this.parent = parent;
    }

    @Override
    protected void init() {
        int contentWidth = Math.min(360, this.width - 36);
        int x = this.width / 2 - contentWidth / 2;
        int y = Math.max(42, this.height / 2 - 72);

        this.addButton(new ButtonWidget(x, y, contentWidth, 20, this.enabledText(), button -> {
            this.config.oceanRavines = !this.config.oceanRavines;
            button.setMessage(this.enabledText());
        }));
        this.addButton(new OceanRavineSlider(x, y + 32, contentWidth, "Rarity", 0.0D, 0.1D, this.config.oceanRavineRarity, 3, value -> this.config.oceanRavineRarity = value));
        this.addButton(new OceanRavineSlider(x, y + 58, contentWidth, "Width", 1.0D, 8.0D, this.config.oceanRavineWidth, 2, value -> this.config.oceanRavineWidth = value));
        this.addButton(new ButtonWidget(x, y + 90, contentWidth, 20, new LiteralText("AreEssGee Values"), button -> {
            this.config.oceanRavineRarity = AREG_RARITY;
            this.config.oceanRavineWidth = AREG_WIDTH;
            this.init(this.client, this.width, this.height);
        }));
        this.addButton(new ButtonWidget(this.width / 2 - 100, this.height - 27, 200, 20, ScreenTexts.DONE, button -> this.onClose()));
    }

    private LiteralText enabledText() {
        return new LiteralText("Ocean Ravines: " + (this.config.oceanRavines ? "On" : "Off"));
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

    private static class OceanRavineSlider extends SliderWidget {
        private final String label;
        private final double min;
        private final double max;
        private final int decimals;
        private final DoubleConsumer setter;

        OceanRavineSlider(int x, int y, int width, String label, double min, double max, double initial, int decimals, DoubleConsumer setter) {
            super(x, y, width, 20, new LiteralText(""), MathHelper.clamp((initial - min) / (max - min), 0.0D, 1.0D));
            this.label = label;
            this.min = min;
            this.max = max;
            this.decimals = decimals;
            this.setter = setter;
            this.updateMessage();
        }

        private double currentValue() {
            return this.min + (this.max - this.min) * this.value;
        }

        @Override
        protected void updateMessage() {
            this.setMessage(new LiteralText(this.label + ": " + String.format(Locale.ROOT, "%." + this.decimals + "f", this.currentValue())));
        }

        @Override
        protected void applyValue() {
            this.setter.accept(this.currentValue());
        }
    }
}
