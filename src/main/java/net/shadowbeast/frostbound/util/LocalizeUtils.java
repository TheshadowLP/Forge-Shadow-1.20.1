package net.shadowbeast.frostbound.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
public class LocalizeUtils {
    private static final String REMAINING_USES = "tooltip.uses";
    public static Component usesRemaining(int uses) {
        return i18n(REMAINING_USES, uses);
    }
    public static Component i18n(String text, Object... args) {
        MutableComponent result = MutableComponent.create(new TranslatableContents(text, null, args));
        return result.withStyle(ChatFormatting.GRAY);
    }
}