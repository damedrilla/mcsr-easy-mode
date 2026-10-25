package me.dota1g.mcsreasymode.client;

import me.dota1g.mcsreasymode.McsreasymodeConfig;
import me.dota1g.mcsreasymode.PieChartPath;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import org.lwjgl.glfw.GLFW;
import java.io.IOException;
import java.util.Collections;

public final class McsreasymodePiePathsScreen extends Screen {
    private final McsreasymodeConfig config;
    private final Screen parent;
    private TextFieldWidget nameField;
    private TextFieldWidget pathField;
    private ButtonWidget keyButton;
    private int selected;
    private boolean adding;
    private boolean capturingKey;
    private String error = "";

    public McsreasymodePiePathsScreen(McsreasymodeConfig config, Screen parent) {
        super(new LiteralText("Pie Chart Paths"));
        this.config = config;
        this.parent = parent;
        if (config.pieChartPaths == null) config.pieChartPaths = McsreasymodeConfig.defaultPieChartPaths();
        this.adding = config.pieChartPaths.isEmpty();
    }

    @Override
    protected void init() {
        String oldName = this.nameField == null ? null : this.nameField.getText();
        String oldPath = this.pathField == null ? null : this.pathField.getText();
        int width = Math.min(520, this.width - 36);
        int x = (this.width - width) / 2;
        int third = (width - 8) / 3;
        this.addButton(new ButtonWidget(x, 32, 40, 20, new LiteralText("<"), b -> this.select(-1)));
        this.addButton(new ButtonWidget(x + width - 40, 32, 40, 20, new LiteralText(">"), b -> this.select(1)));
        this.nameField = this.addChild(new TextFieldWidget(this.textRenderer, x, 68, width, 20, new LiteralText("Name (optional)")));
        this.nameField.setMaxLength(64);
        this.pathField = this.addChild(new TextFieldWidget(this.textRenderer, x, 104, width, 20, new LiteralText("Profiler path")));
        this.pathField.setMaxLength(512);
        if (oldName == null) this.loadFields();
        else { this.nameField.setText(oldName); this.pathField.setText(oldPath); }
        this.nameField.setChangedListener(value -> this.error = "");
        this.pathField.setChangedListener(value -> this.error = "");
        this.addButton(new ButtonWidget(x, 144, third, 20, new LiteralText("Apply"), b -> this.applyDraft()));
        this.addButton(new ButtonWidget(x + third + 4, 144, third, 20, new LiteralText("Add Path"), b -> {
            if (!this.applyDraft()) return;
            this.adding = true;
            this.loadFields();
            this.setFocused(this.pathField);
            this.pathField.setSelected(true);
        }));
        this.addButton(new ButtonWidget(x + 2 * (third + 4), 144, third, 20, new LiteralText("Remove"), b -> {
            if (!this.adding && !this.config.pieChartPaths.isEmpty()) this.config.pieChartPaths.remove(this.selected);
            this.selected = Math.max(0, Math.min(this.selected, this.config.pieChartPaths.size() - 1));
            this.adding = this.config.pieChartPaths.isEmpty();
            this.loadFields();
            this.persist();
        }));
        this.addButton(new ButtonWidget(x, 168, third, 20, new LiteralText("Move Earlier"), b -> this.move(-1)));
        this.addButton(new ButtonWidget(x + third + 4, 168, third, 20, new LiteralText("Move Later"), b -> this.move(1)));
        this.keyButton = this.addButton(new ButtonWidget(x + 2 * (third + 4), 168, third, 20, this.keyText(), b -> {
            this.capturingKey = true;
            b.setMessage(this.keyText());
            this.setFocused(null);
            this.nameField.setSelected(false);
            this.pathField.setSelected(false);
        }));
        this.addButton(new ButtonWidget(this.width / 2 - 102, this.height - 27, 100, 20, new LiteralText("Cancel"), b -> this.client.openScreen(this.parent)));
        this.addButton(new ButtonWidget(this.width / 2 + 2, this.height - 27, 100, 20, ScreenTexts.DONE, b -> this.onClose()));
    }

    private void loadFields() {
        PieChartPath entry = this.adding ? null : this.config.pieChartPaths.get(this.selected);
        this.nameField.setText(entry == null || entry.name == null ? "" : entry.name);
        this.pathField.setText(entry == null || entry.path == null ? "root" : entry.path);
        this.error = "";
    }

    private boolean applyDraft() {
        try {
            PieChartPath entry = new PieChartPath(this.nameField.getText().trim(), this.pathField.getText());
            if (this.adding) {
                this.config.pieChartPaths.add(entry);
                this.selected = this.config.pieChartPaths.size() - 1;
                this.adding = false;
            } else {
                this.config.pieChartPaths.set(this.selected, entry);
            }
            return this.persist();
        } catch (IllegalArgumentException exception) {
            this.error = exception.getMessage();
            return false;
        }
    }

    private boolean persist() {
        try {
            this.config.savePieChartPaths();
            this.error = "";
            return true;
        } catch (IOException exception) {
            this.error = "Could not save paths. Check the config folder and try again.";
            return false;
        }
    }

    private void select(int direction) {
        if (!this.applyDraft()) return;
        this.selected = Math.floorMod(this.selected + direction, this.config.pieChartPaths.size());
        this.loadFields();
    }

    private void move(int direction) {
        if (!this.applyDraft()) return;
        int destination = this.selected + direction;
        if (destination < 0 || destination >= this.config.pieChartPaths.size()) return;
        Collections.swap(this.config.pieChartPaths, this.selected, destination);
        this.selected = destination;
        this.persist();
    }

    private LiteralText keyText() {
        return new LiteralText(this.capturingKey ? "Press a key..." : "Key: " +
                (PieChartShortcuts.cycleKey == null || PieChartShortcuts.cycleKey.isUnbound()
                        ? "Not Bound" : PieChartShortcuts.cycleKey.getBoundKeyLocalizedText().getString()));
    }

    private void bind(InputUtil.Key key) {
        try {
            PieChartShortcuts.bind(this.client, key);
        } catch (IOException exception) {
            this.error = "Could not save shortcut key. Check the config folder.";
        }
        this.capturingKey = false;
        this.keyButton.setMessage(this.keyText());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.capturingKey) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                this.capturingKey = false;
                this.keyButton.setMessage(this.keyText());
            } else {
                this.bind(keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE
                        ? InputUtil.UNKNOWN_KEY : InputUtil.fromKeyCode(keyCode, scanCode));
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (this.capturingKey) { this.bind(InputUtil.Type.MOUSE.createFromCode(button)); return true; }
        return super.mouseClicked(x, y, button);
    }

    @Override
    public void tick() {
        this.nameField.tick();
        this.pathField.tick();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        this.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 12, 0xFFFFFF);
        this.drawCenteredText(matrices, this.textRenderer, new LiteralText(this.adding ? "New path" :
                "Path " + (this.selected + 1) + " / " + this.config.pieChartPaths.size()), this.width / 2, 38, 0xFFFFFF);
        int x = this.nameField.x;
        this.textRenderer.drawWithShadow(matrices, "Name (optional)", x, 57, 0xFFFFFF);
        this.textRenderer.drawWithShadow(matrices, "Path (e.g. root.tick.level.entities.blockEntities)", x, 93, 0xFFFFFF);
        this.nameField.render(matrices, mouseX, mouseY, delta);
        this.pathField.render(matrices, mouseX, mouseY, delta);
        this.textRenderer.drawWithShadow(matrices, this.error.isEmpty() ? "Sections can be empty until profiler data is available." : this.error,
                x, 130, this.error.isEmpty() ? 0xAAAAAA : 0xFF7777);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        // An empty list is intentional; do not recreate a default entry when leaving it.
        if (this.adding && this.nameField.getText().isEmpty() && "root".equals(this.pathField.getText())) {
            if (!this.persist()) return;
        } else if (!this.applyDraft()) return;
        this.client.openScreen(this.parent);
    }
}
