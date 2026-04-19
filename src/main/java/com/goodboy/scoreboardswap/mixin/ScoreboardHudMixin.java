package com.goodboy.scoreboardswap.mixin;

import com.goodboy.scoreboardswap.SwapConfig;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(InGameHud.class)
public class ScoreboardHudMixin {

    /**
     * Intercepts each Text component rendered on the scoreboard sidebar
     * and applies the user's replacements while preserving the original style.
     */
    @ModifyVariable(
            method = "renderScoreboardSidebar",
            at = @At("STORE"),
            ordinal = 0
    )
    private Text scoreboardswap$replaceText(Text original) {
        if (original == null) return null;

        String raw = original.getString();
        String replaced = SwapConfig.applyReplacement(raw);

        if (!replaced.equals(raw)) {
            // Build a new literal with the replaced text but keep the original style
            Style style = original.getStyle();
            MutableText newText = Text.literal(replaced).setStyle(style);
            return newText;
        }

        return original;
    }
}
