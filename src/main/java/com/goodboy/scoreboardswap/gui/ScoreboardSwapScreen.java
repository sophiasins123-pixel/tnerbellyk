package com.goodboy.scoreboardswap.gui;

import com.goodboy.scoreboardswap.SwapConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScoreboardSwapScreen extends Screen {

    private TextFieldWidget originalField;
    private TextFieldWidget replacementField;
    private final List<String[]> entryList = new ArrayList<>();

    // Scroll offset for the list
    private int scrollOffset = 0;
    private static final int ENTRY_HEIGHT = 22;
    private static final int LIST_TOP = 120;
    private static final int MAX_VISIBLE = 6;

    public ScoreboardSwapScreen() {
        super(Text.literal("ScoreboardSwap"));
    }

    @Override
    protected void init() {
        refreshEntryList();

        int cx = this.width / 2;

        // Title drawn in render()

        // "Original text" label + field
        originalField = new TextFieldWidget(this.textRenderer,
                cx - 160, 50, 150, 20,
                Text.literal("Original"));
        originalField.setPlaceholderText(Text.literal("Original text (e.g. MelvinPLAYZ_)"));
        originalField.setMaxLength(64);
        addDrawableChild(originalField);

        // "Replace with" label + field
        replacementField = new TextFieldWidget(this.textRenderer,
                cx + 10, 50, 150, 20,
                Text.literal("Replace with"));
        replacementField.setPlaceholderText(Text.literal("Replacement text"));
        replacementField.setMaxLength(64);
        addDrawableChild(replacementField);

        // Add button
        addDrawableChild(ButtonWidget.builder(Text.literal("Add / Update"), btn -> {
            String orig = originalField.getText().trim();
            String repl = replacementField.getText().trim();
            if (!orig.isEmpty() && !repl.isEmpty()) {
                SwapConfig.replacements.put(orig, repl);
                SwapConfig.save();
                originalField.setText("");
                replacementField.setText("");
                refreshEntryList();
            }
        }).dimensions(cx - 50, 80, 100, 20).build());

        // Scroll buttons
        addDrawableChild(ButtonWidget.builder(Text.literal("▲"), btn -> {
            if (scrollOffset > 0) scrollOffset--;
        }).dimensions(cx + 165, LIST_TOP, 20, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("▼"), btn -> {
            if (scrollOffset < Math.max(0, entryList.size() - MAX_VISIBLE)) scrollOffset++;
        }).dimensions(cx + 165, LIST_TOP + (MAX_VISIBLE - 1) * ENTRY_HEIGHT, 20, 20).build());

        // Done button
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), btn -> close())
                .dimensions(cx - 40, LIST_TOP + MAX_VISIBLE * ENTRY_HEIGHT + 10, 80, 20).build());
    }

    private void refreshEntryList() {
        entryList.clear();
        for (Map.Entry<String, String> e : SwapConfig.replacements.entrySet()) {
            entryList.add(new String[]{e.getKey(), e.getValue()});
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int cx = this.width / 2;

        // Title
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("§6ScoreboardSwap"), cx, 20, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("§7Press §fF8 §7to open  |  Changes apply instantly"),
                cx, 32, 0xFFFFFF);

        // Field labels
        context.drawTextWithShadow(this.textRenderer,
                Text.literal("§7Original"), cx - 160, 42, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer,
                Text.literal("§7Replace with"), cx + 10, 42, 0xFFFFFF);

        // List header
        context.drawTextWithShadow(this.textRenderer,
                Text.literal("§eActive replacements:"), cx - 160, LIST_TOP - 14, 0xFFFFFF);

        // Entry rows
        int visible = Math.min(MAX_VISIBLE, entryList.size() - scrollOffset);
        for (int i = 0; i < visible; i++) {
            int idx = i + scrollOffset;
            if (idx >= entryList.size()) break;
            String[] entry = entryList.get(idx);
            int y = LIST_TOP + i * ENTRY_HEIGHT;

            // Row background
            context.fill(cx - 162, y - 2, cx + 162, y + 14, 0x44000000);

            context.drawTextWithShadow(this.textRenderer,
                    Text.literal("§f" + entry[0] + " §7→ §a" + entry[1]),
                    cx - 158, y, 0xFFFFFF);

            // Delete [X] button — drawn manually as text, detected by click
            context.drawTextWithShadow(this.textRenderer,
                    Text.literal("§c[X]"), cx + 140, y, 0xFFFFFF);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Check if user clicked a [X] delete button
        int cx = this.width / 2;
        int visible = Math.min(MAX_VISIBLE, entryList.size() - scrollOffset);
        for (int i = 0; i < visible; i++) {
            int idx = i + scrollOffset;
            if (idx >= entryList.size()) break;
            int y = LIST_TOP + i * ENTRY_HEIGHT;
            // [X] is at cx+140, width ~16, height 10
            if (mouseX >= cx + 140 && mouseX <= cx + 162
                    && mouseY >= y - 2 && mouseY <= y + 12) {
                String key = entryList.get(idx)[0];
                SwapConfig.replacements.remove(key);
                SwapConfig.save();
                refreshEntryList();
                if (scrollOffset >= entryList.size() && scrollOffset > 0) scrollOffset--;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount < 0 && scrollOffset < Math.max(0, entryList.size() - MAX_VISIBLE)) {
            scrollOffset++;
        } else if (verticalAmount > 0 && scrollOffset > 0) {
            scrollOffset--;
        }
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
