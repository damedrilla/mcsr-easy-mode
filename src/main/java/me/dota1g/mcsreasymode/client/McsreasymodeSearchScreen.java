package me.dota1g.mcsreasymode.client;

import me.dota1g.mcsreasymode.McsreasymodeConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class McsreasymodeSearchScreen extends Screen {
    private final McsreasymodeConfig config;
    private final Screen parent;

    private final List<PresetRow> rows = new ArrayList<>();
    private boolean rowsInitialized = false;
    private int capturingIndex = -1;

    private ButtonWidget autoFocusButton;
    private ButtonWidget chatFocusButton;
    private ButtonWidget presetsEnabledButton;
    private ButtonWidget backspaceModeButton;

    private int contentLeft;
    private int contentRight;
    private int listTop;
    private int listBottom;
    private int scrollOffset;
    private int contentHeight;

    public McsreasymodeSearchScreen(McsreasymodeConfig config, Screen parent) {
        super(new LiteralText("Search Crafting Settings"));
        this.config = config;
        this.parent = parent;
    }

    @Override
    protected void init() {
        int contentWidth = Math.min(640, this.width - 72);
        this.contentLeft = this.width / 2 - contentWidth / 2;
        this.contentRight = this.contentLeft + contentWidth;
        this.listTop = 140;
        this.listBottom = this.height - 60;
        this.scrollOffset = Math.min(this.scrollOffset, this.maxScroll());

        if (!this.rowsInitialized) {
            for (Map.Entry<Integer, String> entry : this.config.searchPresets.entrySet()) {
                PresetRow row = new PresetRow();
                row.keyCode = entry.getKey();
                row.pendingText = entry.getValue();
                this.rows.add(row);
            }
            this.rowsInitialized = true;
        }

        this.autoFocusButton = this.addButton(new ButtonWidget(this.contentLeft, 42, contentWidth, 20,
                this.toggleText("Focus search on open", this.config.autoFocusSearchField), button -> {
            this.config.autoFocusSearchField = !this.config.autoFocusSearchField;
            button.setMessage(this.toggleText("Focus search on open", this.config.autoFocusSearchField));
        }));

        this.chatFocusButton = this.addButton(new ButtonWidget(this.contentLeft, 66, contentWidth, 20,
                this.toggleText("Force search focus with chat key", this.config.focusSearchOnChatKey), button -> {
            this.config.focusSearchOnChatKey = !this.config.focusSearchOnChatKey;
            button.setMessage(this.toggleText("Force search focus with chat key", this.config.focusSearchOnChatKey));
        }));

        this.presetsEnabledButton = this.addButton(new ButtonWidget(this.contentLeft, 90, contentWidth, 20,
                this.toggleText("Preset hotkeys (Alt+key)", this.config.searchPresetsEnabled), button -> {
            this.config.searchPresetsEnabled = !this.config.searchPresetsEnabled;
            button.setMessage(this.toggleText("Preset hotkeys (Alt+key)", this.config.searchPresetsEnabled));
        }));

        this.backspaceModeButton = this.addButton(new ButtonWidget(this.contentLeft, 114, contentWidth, 20,
                this.backspaceModeText(), button -> {
            this.config.searchBackspaceMode = this.config.searchBackspaceMode.next();
            button.setMessage(this.backspaceModeText());
        }));

        this.layoutRows();

        this.addButton(new ButtonWidget(this.width / 2 - 75, this.height - 52, 150, 20,
                new LiteralText("+ Add Preset"), button -> this.addRow()));
        this.addButton(new ButtonWidget(this.width / 2 - 100, this.height - 27, 200, 20,
                ScreenTexts.DONE, button -> this.onClose()));
    }

    private void layoutRows() {
        for (PresetRow row : this.rows) {
            if (row.field != null) { this.buttons.remove(row.field); this.children.remove(row.field); }
            if (row.keyButton != null) { this.buttons.remove(row.keyButton); this.children.remove(row.keyButton); }
            if (row.removeButton != null) { this.buttons.remove(row.removeButton); this.children.remove(row.removeButton); }
        }

        int contentY = 0;
        for (int i = 0; i < this.rows.size(); i++) {
            PresetRow row = this.rows.get(i);
            int rowIndex = i;
            int screenY = this.toScreenY(contentY);
            boolean visible = screenY + 20 > this.listTop && screenY < this.listBottom;

            ButtonWidget keyButton = new ButtonWidget(this.contentLeft, screenY, 90, 20,
                    this.keyLabelText(row, rowIndex), button -> this.beginCapture(rowIndex));
            keyButton.visible = visible;
            this.addButton(keyButton);
            row.keyButton = keyButton;

            TextFieldWidget field = new TextFieldWidget(this.textRenderer, this.contentLeft + 96, screenY,
                    this.contentRight - this.contentLeft - 96 - 26, 20, new LiteralText("search"));
            field.setMaxLength(64);
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
        PresetRow row = new PresetRow();
        row.keyCode = null;
        row.pendingText = "";
        this.rows.add(row);
        this.layoutRows();
    }

    private void removeRow(int index) {
        if (index < 0 || index >= this.rows.size()) {
            return;
        }
        PresetRow row = this.rows.remove(index);
        if (row.field != null) this.children.remove(row.field);
        if (row.keyButton != null) { this.buttons.remove(row.keyButton); this.children.remove(row.keyButton); }
        if (row.removeButton != null) { this.buttons.remove(row.removeButton); this.children.remove(row.removeButton); }
        this.layoutRows();
    }

    private void beginCapture(int index) {
        this.capturingIndex = index;
        this.layoutRows();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.capturingIndex != -1) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                this.capturingIndex = -1;
                this.layoutRows();
                return true;
            }
            if (mcsreasymode$isModifierKey(keyCode)) {
                return true;
            }
            this.rows.get(this.capturingIndex).keyCode = keyCode;
            this.capturingIndex = -1;
            this.layoutRows();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private static boolean mcsreasymode$isModifierKey(int keyCode) {
        return keyCode == GLFW.GLFW_KEY_LEFT_SHIFT || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT
                || keyCode == GLFW.GLFW_KEY_LEFT_CONTROL || keyCode == GLFW.GLFW_KEY_RIGHT_CONTROL
                || keyCode == GLFW.GLFW_KEY_LEFT_ALT || keyCode == GLFW.GLFW_KEY_RIGHT_ALT
                || keyCode == GLFW.GLFW_KEY_LEFT_SUPER || keyCode == GLFW.GLFW_KEY_RIGHT_SUPER;
    }

    private Text keyLabelText(PresetRow row, int index) {
        if (index == this.capturingIndex) {
            return new LiteralText("Press a key...");
        }
        if (row.keyCode == null) {
            return new LiteralText("Unbound");
        }
        String keyName = GLFW.glfwGetKeyName(row.keyCode, 0);
        return new LiteralText("Alt+" + (keyName != null ? keyName.toUpperCase() : "?"));
    }

    private Text toggleText(String label, boolean enabled) {
        return new LiteralText(label + ": " + (enabled ? "On" : "Off"));
    }

    private Text backspaceModeText() {
        return new LiteralText("Backspace: " + this.config.searchBackspaceMode.displayName());
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
        java.util.LinkedHashMap<Integer, String> map = new java.util.LinkedHashMap<>();
        for (PresetRow row : this.rows) {
            if (row.keyCode == null) {
                continue;
            }
            String text = row.field != null ? row.field.getText() : (row.pendingText != null ? row.pendingText : "");
            map.put(row.keyCode, text);
        }
        this.config.searchPresets = map;
        this.config.saveSearchPresets();
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

    private static class PresetRow {
        Integer keyCode;
        String pendingText;
        TextFieldWidget field;
        ButtonWidget keyButton;
        ButtonWidget removeButton;
    }
}
