package me.dota1g.mcsreasymode.client;

import me.dota1g.mcsreasymode.McsreasymodeConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.io.IOException;
import java.util.Locale;
import java.util.function.DoubleConsumer;

public class McsreasymodeNetherTerrainScreen extends Screen {
    private static final double VANILLA_XZ_SCALE = 1.0D;
    private static final double VANILLA_DENSITY_FACTOR = 0.0D;
    private static final double VANILLA_DENSITY_OFFSET = 0.019921875D;
    private static final double AREG_XZ_SCALE = 0.52D;
    private static final double AREG_DENSITY_FACTOR = 0.08215D;
    private static final double AREG_DENSITY_OFFSET = 0.01174D;

    private final McsreasymodeConfig config;
    private final Screen parent;
    private ButtonWidget enabledButton;

    public McsreasymodeNetherTerrainScreen(McsreasymodeConfig config, Screen parent) {
        super(new LiteralText("Nether Terrain Alpha"));
        this.config = config;
        this.parent = parent;
    }

    @Override
    protected void init() {
        int contentWidth = Math.min(360, this.width - 36);
        int x = this.width / 2 - contentWidth / 2;
        int y = Math.max(34, this.height / 2 - 96);
        int halfWidth = (contentWidth - 4) / 2;

        this.enabledButton = this.addButton(new ButtonWidget(x, y, contentWidth, 20, this.enabledText(), button -> {
            this.config.openNetherTerrain = !this.config.openNetherTerrain;
            button.setMessage(this.enabledText());
        }));

        this.addButton(new TerrainSliderWidget(x, y + 32, contentWidth, 20, "XZ Scale", AREG_XZ_SCALE, VANILLA_XZ_SCALE, this.config.netherTerrainXzScale, value -> this.config.netherTerrainXzScale = value));
        this.addButton(new TerrainSliderWidget(x, y + 58, contentWidth, 20, "Density Factor", VANILLA_DENSITY_FACTOR, AREG_DENSITY_FACTOR, this.config.netherTerrainDensityFactor, value -> this.config.netherTerrainDensityFactor = value));
        this.addButton(new TerrainSliderWidget(x, y + 84, contentWidth, 20, "Density Offset", AREG_DENSITY_OFFSET, VANILLA_DENSITY_OFFSET, this.config.netherTerrainDensityOffset, value -> this.config.netherTerrainDensityOffset = value));

        this.addButton(new ButtonWidget(x, y + 116, halfWidth, 20, new LiteralText("Vanilla Values"), button -> {
            this.setTerrainValues(VANILLA_XZ_SCALE, VANILLA_DENSITY_FACTOR, VANILLA_DENSITY_OFFSET);
            this.init(this.client, this.width, this.height);
        }));
        this.addButton(new ButtonWidget(x + halfWidth + 4, y + 116, halfWidth, 20, new LiteralText("AreEssGee Values"), button -> {
            this.setTerrainValues(AREG_XZ_SCALE, AREG_DENSITY_FACTOR, AREG_DENSITY_OFFSET);
            this.init(this.client, this.width, this.height);
        }));

        this.addButton(new ButtonWidget(this.width / 2 - 100, this.height - 27, 200, 20, ScreenTexts.DONE, button -> this.onClose()));
    }

    private Text enabledText() {
        return new LiteralText("Open Nether Terrain: " + (this.config.openNetherTerrain ? "On" : "Off"));
    }

    private void setTerrainValues(double xzScale, double densityFactor, double densityOffset) {
        this.config.netherTerrainXzScale = xzScale;
        this.config.netherTerrainDensityFactor = densityFactor;
        this.config.netherTerrainDensityOffset = densityOffset;
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

    private static class TerrainSliderWidget extends SliderWidget {
        private final String label;
        private final double min;
        private final double max;
        private final DoubleConsumer setter;

        TerrainSliderWidget(int x, int y, int width, int height, String label, double min, double max, double initialValue, DoubleConsumer setter) {
            super(x, y, width, height, new LiteralText(""), normalize(min, max, initialValue));
            this.label = label;
            this.min = min;
            this.max = max;
            this.setter = setter;
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            this.setMessage(new LiteralText(this.label + ": " + String.format(Locale.ROOT, "%.5f", this.currentValue())));
        }

        @Override
        protected void applyValue() {
            this.setter.accept(this.currentValue());
        }

        private double currentValue() {
            return this.min + (this.max - this.min) * this.value;
        }

        private static double normalize(double min, double max, double value) {
            if (max <= min) {
                return 0.0D;
            }
            return MathHelper.clamp((value - min) / (max - min), 0.0D, 1.0D);
        }
    }
}
