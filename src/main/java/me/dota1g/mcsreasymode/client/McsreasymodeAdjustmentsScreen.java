package me.dota1g.mcsreasymode.client;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

public class McsreasymodeAdjustmentsScreen extends Screen {
    private static final String[] ADJUSTMENTS = {
            "[Implemented] Iron golems always drop exactly 4 iron ingots.",
            "[Implemented] Thrown eyes of ender always drop back as items.",
            "[Implemented] Blaze rods use 50% chance with no more than 2 dry kills.",
            "[Implemented] Piglin barters pity pearls after 12 dry barters.",
            "[Implemented] Piglin barters pity obsidian after 16 dry barters.",
            "[Planned] Flint streak protection.",
            "[Planned] Nether portal Y behavior inspired by Ranked.",
            "[Planned] Blind travel portal surfacing behavior inspired by Ranked.",
            "[Planned] Spawn coordinate behavior inspired by Ranked."
    };

    private final Screen parent;

    public McsreasymodeAdjustmentsScreen(Screen parent) {
        super(new LiteralText("RNG Adjustments"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.addButton(new ButtonWidget(this.width / 2 - 100, this.height - 27, 200, 20, ScreenTexts.DONE, button -> this.onClose()));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        this.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 18, 0xFFFFFF);

        int x = Math.max(20, this.width / 2 - 180);
        int y = 44;
        for (String adjustment : ADJUSTMENTS) {
            this.textRenderer.drawWithShadow(matrices, adjustment, x, y, 0xE0E0E0);
            y += 14;
        }

        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        assert this.client != null;
        this.client.openScreen(this.parent);
    }
}
