package me.dota1g.mcsreasymode.client;

import me.dota1g.mcsreasymode.McsreasymodeConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

import java.io.IOException;

public class McsreasymodePersistentCoordinatesScreen extends Screen {
    private final McsreasymodeConfig config;
    private final Screen parent;

    public McsreasymodePersistentCoordinatesScreen(McsreasymodeConfig config, Screen parent) {
        super(new LiteralText("Persistent Coordinates"));
        this.config = config;
        this.parent = parent;
    }

    @Override
    protected void init() {
        int width = Math.min(320, this.width - 36);
        int x = this.width / 2 - width / 2;
        int y = Math.max(54, this.height / 2 - 48);

        this.addButton(new ButtonWidget(x, y, width, 20, this.preciseText(), button -> {
            this.config.persistentCoordinatesPrecise = !this.config.persistentCoordinatesPrecise;
            button.setMessage(this.preciseText());
        }));
        this.addButton(new ButtonWidget(x, y + 24, width, 20, this.blockText(), button -> {
            this.config.persistentCoordinatesBlock = !this.config.persistentCoordinatesBlock;
            button.setMessage(this.blockText());
        }));
        this.addButton(new ButtonWidget(x, y + 48, width, 20, this.localChunkText(), button -> {
            this.config.persistentCoordinatesLocalChunk = !this.config.persistentCoordinatesLocalChunk;
            button.setMessage(this.localChunkText());
        }));
        this.addButton(new ButtonWidget(this.width / 2 - 100, this.height - 27, 200, 20, ScreenTexts.DONE, button -> this.onClose()));
    }

    private LiteralText preciseText() {
        return this.toggleText("Coordinates", this.config.persistentCoordinatesPrecise);
    }

    private LiteralText blockText() {
        return this.toggleText("Block Coordinates", this.config.persistentCoordinatesBlock);
    }

    private LiteralText localChunkText() {
        return this.toggleText("Local Chunk Coordinates", this.config.persistentCoordinatesLocalChunk);
    }

    private LiteralText toggleText(String label, boolean enabled) {
        return new LiteralText(label + ": " + (enabled ? "On" : "Off"));
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
