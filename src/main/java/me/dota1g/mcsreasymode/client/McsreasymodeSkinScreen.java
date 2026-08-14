package me.dota1g.mcsreasymode.client;

import me.dota1g.mcsreasymode.McsreasymodeConfig;
import me.dota1g.mcsreasymode.client.skin.WorldSkinManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class McsreasymodeSkinScreen extends Screen {
    private final McsreasymodeConfig config;
    private final Screen parent;

    private final List<SkinRow> rows = new ArrayList<>();
    private boolean rowsInitialized = false;

    private ButtonWidget enabledButton;

    private int contentLeft;
    private int contentRight;
    private int listTop;
    private int listBottom;
    private int scrollOffset;
    private int contentHeight;

    public McsreasymodeSkinScreen(McsreasymodeConfig config, Screen parent) {
        super(new LiteralText("World Skin Settings"));
        this.config = config;
        this.parent = parent;
    }

    @Override
    protected void init() {
        int contentWidth = Math.min(640, this.width - 72);
        this.contentLeft = this.width / 2 - contentWidth / 2;
        this.contentRight = this.contentLeft + contentWidth;
        this.listTop = 68;
        this.listBottom = this.height - 60;
        this.scrollOffset = Math.min(this.scrollOffset, this.maxScroll());

        if (!this.rowsInitialized) {
            for (String path : this.config.worldSkinPaths) {
                SkinRow row = new SkinRow();
                row.pendingText = path;
                this.rows.add(row);
            }
            this.rowsInitialized = true;
        }

        this.enabledButton = this.addButton(new ButtonWidget(this.contentLeft, 42, contentWidth, 20,
                this.toggleText("Random skin on world join", this.config.worldSkinEnabled), button -> {
            this.config.worldSkinEnabled = !this.config.worldSkinEnabled;
            button.setMessage(this.toggleText("Random skin on world join", this.config.worldSkinEnabled));
        }));

        this.layoutRows();

        this.addButton(new ButtonWidget(this.width / 2 - 75, this.height - 52, 150, 20,
                new LiteralText("+ Add Skin Path"), button -> this.addRow()));
        this.addButton(new ButtonWidget(this.width / 2 - 100, this.height - 27, 200, 20,
                ScreenTexts.DONE, button -> this.onClose()));
    }

    private void layoutRows() {
        for (SkinRow row : this.rows) {
            if (row.field != null) { this.buttons.remove(row.field); this.children.remove(row.field); }
            if (row.removeButton != null) { this.buttons.remove(row.removeButton); this.children.remove(row.removeButton); }
        }

        int contentY = 0;
        for (int i = 0; i < this.rows.size(); i++) {
            SkinRow row = this.rows.get(i);
            int rowIndex = i;
            int screenY = this.toScreenY(contentY);
            boolean visible = screenY + 20 > this.listTop && screenY < this.listBottom;

            TextFieldWidget field = new TextFieldWidget(this.textRenderer, this.contentLeft, screenY,
                    this.contentRight - this.contentLeft - 26, 20, new LiteralText("skin path"));
            field.setMaxLength(256);
            field.setVisible(visible);
            field.setText(row.field != null ? row.field.getText() : (row.pendingText != null ? row.pendingText : ""));
            this.addButton(field);
            row.field = field;

            ButtonWidget removeButton = new ButtonWidget(this.contentRight - 20, screenY, 20, 20,
                    new LiteralText("x"), button -> this.removeRow(rowIndex));
            removeButton.visible = visible;
            this.addButton(removeButton);
            row.removeButton = removeButton;

            contentY += 24;
        }

        this.contentHeight = contentY + 8;
    }

    private void addRow() {
        SkinRow row = new SkinRow();
        row.pendingText = "";
        this.rows.add(row);
        this.layoutRows();
    }

    private void removeRow(int index) {
        if (index < 0 || index >= this.rows.size()) {
            return;
        }
        SkinRow row = this.rows.remove(index);
        if (row.field != null) { this.buttons.remove(row.field); this.children.remove(row.field); }
        if (row.removeButton != null) { this.buttons.remove(row.removeButton); this.children.remove(row.removeButton); }
        this.layoutRows();
    }

    private Text toggleText(String label, boolean enabled) {
        return new LiteralText(label + ": " + (enabled ? "On" : "Off"));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        int maxScroll = this.maxScroll();
        if (maxScroll <= 0 || mouseY < this.listTop || mouseY > this.listBottom) {
            return super.mouseScrolled(mouseX, mouseY, amount);
        }
        this.scrollOffset = Math.max(0, Math.min(maxScroll, this.scrollOffset - (int) (amount * 24)));
        this.layoutRows();
        return true;
    }

    private int toScreenY(int contentY) {
        return this.listTop + contentY - this.scrollOffset;
    }

    private int maxScroll() {
        return Math.max(0, this.contentHeight - (this.listBottom - this.listTop));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        this.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 18, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }

    private void saveRows() {
        List<String> paths = new ArrayList<>();
        WorldSkinManager.getSkinPool().clear();
        for (SkinRow row : this.rows) {
            String text = row.field != null ? row.field.getText() : (row.pendingText != null ? row.pendingText : "");
            text = text.trim();
            if (!text.isEmpty()) {
                paths.add(text);
                WorldSkinManager.addSkin(text);
            }
        }
        this.config.worldSkinPaths = paths;
    }

    @Override
    public void onClose() {
        this.saveRows();
        assert this.client != null;
        this.client.openScreen(this.parent);
    }

    @Override
    public void removed() {
        this.saveRows();
        if (this.config.container != null) {
            try {
                this.config.container.save();
            } catch (IOException ignored) {
            }
        }
    }

    private static class SkinRow {
        String pendingText;
        TextFieldWidget field;
        ButtonWidget removeButton;
    }
}