package me.dota1g.mcsreasymode.client;

import me.dota1g.mcsreasymode.McsreasymodeConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

import java.io.IOException;

public class McsreasymodeBlazeSpawnerOverlayScreen extends Screen {
    private final McsreasymodeConfig config;
    private final Screen parent;

    public McsreasymodeBlazeSpawnerOverlayScreen(McsreasymodeConfig config, Screen parent) {
        super(new LiteralText("Blaze Spawner Overlay"));
        this.config = config;
        this.parent = parent;
    }

    @Override
    protected void init() {
        int width = Math.min(320, this.width - 36);
        int x = this.width / 2 - width / 2;
        int y = Math.max(54, this.height / 2 - 48);

        this.addButton(new ButtonWidget(x, y, width, 20, this.timerText(), button -> {
            this.config.blazeSpawnerOverlayTimer = !this.config.blazeSpawnerOverlayTimer;
            button.setMessage(this.timerText());
        }));
        this.addButton(new ButtonWidget(x, y + 24, width, 20, this.problemMarkersText(), button -> {
            this.config.blazeSpawnerOverlayProblemMarkers = !this.config.blazeSpawnerOverlayProblemMarkers;
            button.setMessage(this.problemMarkersText());
        }));
        this.addButton(new ButtonWidget(x, y + 48, width, 20, this.detailedText(), button -> {
            this.config.blazeSpawnerOverlayDetailedText = !this.config.blazeSpawnerOverlayDetailedText;
            button.setMessage(this.detailedText());
        }));
        this.addButton(new ButtonWidget(this.width / 2 - 100, this.height - 27, 200, 20, ScreenTexts.DONE, button -> this.onClose()));
    }

    private LiteralText timerText() {
        return new LiteralText("Spawner Timer: " + (this.config.blazeSpawnerOverlayTimer ? "On" : "Off"));
    }

    private LiteralText problemMarkersText() {
        return new LiteralText("Blocked / Too Bright Markers: " + (this.config.blazeSpawnerOverlayProblemMarkers ? "On" : "Off"));
    }

    private LiteralText detailedText() {
        return new LiteralText("Detailed Percentages: " + (this.config.blazeSpawnerOverlayDetailedText ? "On" : "Off"));
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
