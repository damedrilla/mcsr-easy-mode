package me.dota1g.mcsreasymode.client;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

public class McsreasymodeAdjustmentsScreen extends Screen {
    private static final String[] ADJUSTMENTS = {
            "Iron golems always drop exactly 4 iron ingots.",
            "Second thrown eye of ender never breaks; other throws are vanilla.",
            "Blaze rods use vanilla drop rates with no more than 2 dry kills.",
            "Piglin barters pity 4-8 pearls after 24 dry barters.",
            "Piglin barters pity 1 obsidian after 24 dry barters.",
            "Piglin barters pity 8-24 strings after 24 dry barters.",
            "Gravel keeps 10% flint odds with no more than 9 dry breaks.",  
            "First Nether Y48+ blind travel portal creates on the surface.",
            "First eligible bastion chest uses Ranked loot, then later ones are vanilla.",
            "Selected chest loot tables swap to Ranked versions only in Ranked mode.",
            "Ruined portal and buried treasure chests use speedrun-focused tables.",
            "Stables are guaranteed to have adjacent double triple chest ramparts"
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
