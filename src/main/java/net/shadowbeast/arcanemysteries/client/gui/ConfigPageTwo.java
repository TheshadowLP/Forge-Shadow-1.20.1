package net.shadowbeast.arcanemysteries.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.shadowbeast.arcanemysteries.config.ConfigSettings;
import org.joml.Vector2i;

import javax.annotation.Nullable;

public class ConfigPageTwo extends AbstractConfigPage
{
    public ConfigPageTwo(Screen parentScreen)
    {   super(parentScreen);
    }

    @Override
    public Component sectionOneTitle()
    {   return Component.translatable("arcanemysteries.config.section.preference");
    }

    @Nullable
    @Override
    public Component sectionTwoTitle()
    {   return Component.translatable("arcanemysteries.config.section.hud_settings");
    }

    @Override
    protected void init()
    {
        super.init();

        // Temp Offset
        this.addDecimalInput("temp_offset", Side.LEFT, Component.translatable("arcanemysteries.config.temp_offset.name"),
                value -> ConfigSettings.TEMP_OFFSET.set(value.intValue()),
                input -> input.setValue(String.valueOf(ConfigSettings.TEMP_OFFSET.get())),
                false, false, true, Component.translatable("arcanemysteries.config.temp_offset.desc"));

        // Temp Smoothing
        this.addDecimalInput("temp_smoothing", Side.LEFT, Component.translatable("arcanemysteries.config.temp_smoothing.name"),
                value -> ConfigSettings.TEMP_SMOOTHING.set(Math.max(1, value)),
                input -> input.setValue(String.valueOf(ConfigSettings.TEMP_SMOOTHING.get())),
                false, false, true, Component.translatable("arcanemysteries.config.temp_smoothing.desc"));

        // Distortion Effects
        this.addButton("distortion_effects", Side.LEFT, () -> Component.translatable("arcanemysteries.config.distortion.name").append(": ").append(ConfigSettings.DISTORTION_EFFECTS.get() ? ON : OFF),
                button ->
                {   ConfigSettings.DISTORTION_EFFECTS.set(!ConfigSettings.DISTORTION_EFFECTS.get());
                },
                false, false, true, Component.translatable("arcanemysteries.config.distortion.desc"));

        // Icon Bobbing
        this.addButton("icon_bobbing", Side.LEFT, () -> Component.translatable("arcanemysteries.config.icon_bobbing.name").append(": ").append(ConfigSettings.ICON_BOBBING.get() ? ON : OFF),
                button -> ConfigSettings.ICON_BOBBING.set(!ConfigSettings.ICON_BOBBING.get()),
                false, false, true, Component.translatable("arcanemysteries.config.icon_bobbing.desc"));

        // High Contrast
        this.addButton("high_contrast", Side.LEFT, () -> Component.translatable("arcanemysteries.config.high_contrast.name").append(": ").append(ConfigSettings.HIGH_CONTRAST.get() ? ON : OFF),
                button -> ConfigSettings.HIGH_CONTRAST.set(!ConfigSettings.HIGH_CONTRAST.get()),
                false, false, true, Component.translatable("arcanemysteries.config.high_contrast.desc"));

        // Config Button Repositioning Screen
        this.addButton("button_position", Side.LEFT, () -> Component.translatable("arcanemysteries.config.config_button_pos.name"),
                button ->
                {
                    DrawConfigButton.EDIT_MODE = true;
                    this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options));
                },
                false, false, true, Component.translatable("arcanemysteries.config.config_button_pos.desc"));

        // Direction Buttons: Steve Head
        this.addDirectionPanel("icon_directions", Side.RIGHT, Component.translatable("arcanemysteries.config.temp_icon_location.name"),
                amount -> ConfigSettings.BODY_ICON_POS.set(new Vector2i(ConfigSettings.BODY_ICON_POS.get().x() + amount * ConfigScreen.SHIFT_AMOUNT.get(),
                        ConfigSettings.BODY_ICON_POS.get().y())),
                amount -> ConfigSettings.BODY_ICON_POS.set(new Vector2i(ConfigSettings.BODY_ICON_POS.get().x(),
                        ConfigSettings.BODY_ICON_POS.get().y() + amount * ConfigScreen.SHIFT_AMOUNT.get())),
                () ->
                {   ConfigSettings.BODY_ICON_POS.set(new Vector2i(0, 0));
                },
                () ->
                {   ConfigSettings.BODY_ICON_ENABLED.set(!ConfigSettings.BODY_ICON_ENABLED.get());
                    return ConfigSettings.BODY_ICON_ENABLED.get();
                },
                false, false, true, true, Component.translatable("arcanemysteries.config.temp_icon_location.desc"),
                Component.translatable("arcanemysteries.config.offset_shift.name").withStyle(ChatFormatting.GRAY));

        // Direction Buttons: Temp Readout
        this.addDirectionPanel("readout_directions", Side.RIGHT, Component.translatable("arcanemysteries.config.temp_readout_location.name"),
                amount -> ConfigSettings.BODY_READOUT_POS.set(new Vector2i(ConfigSettings.BODY_READOUT_POS.get().x() + amount * ConfigScreen.SHIFT_AMOUNT.get(),
                        ConfigSettings.BODY_READOUT_POS.get().y())),
                amount -> ConfigSettings.BODY_READOUT_POS.set(new Vector2i(ConfigSettings.BODY_READOUT_POS.get().x(),
                        ConfigSettings.BODY_READOUT_POS.get().y() + amount * ConfigScreen.SHIFT_AMOUNT.get())),
                () ->
                {   ConfigSettings.BODY_READOUT_POS.set(new Vector2i(0, 0));
                },
                () ->
                {   ConfigSettings.BODY_READOUT_ENABLED.set(!ConfigSettings.BODY_READOUT_ENABLED.get());
                    return ConfigSettings.BODY_READOUT_ENABLED.get();
                },
                false, false, true, true, Component.translatable("arcanemysteries.config.temp_readout_location.desc"),
                Component.translatable("arcanemysteries.config.offset_shift.name").withStyle(ChatFormatting.GRAY));

        this.addDirectionPanel("gauge_directions", Side.RIGHT, Component.translatable("arcanemysteries.config.world_temp_location.name"),
                amount -> ConfigSettings.WORLD_GAUGE_POS.set(new Vector2i(ConfigSettings.WORLD_GAUGE_POS.get().x() + amount * ConfigScreen.SHIFT_AMOUNT.get(),
                        ConfigSettings.WORLD_GAUGE_POS.get().y())),
                amount -> ConfigSettings.WORLD_GAUGE_POS.set(new Vector2i(ConfigSettings.WORLD_GAUGE_POS.get().x(),
                        ConfigSettings.WORLD_GAUGE_POS.get().y() + amount * ConfigScreen.SHIFT_AMOUNT.get())),
                () ->
                {   ConfigSettings.WORLD_GAUGE_POS.set(new Vector2i(0, 0));
                },
                () ->
                {   ConfigSettings.WORLD_GAUGE_ENABLED.set(!ConfigSettings.WORLD_GAUGE_ENABLED.get());
                    return ConfigSettings.WORLD_GAUGE_ENABLED.get();
                },
                false, false, true, true, Component.translatable("arcanemysteries.config.world_temp_location.desc"),
                Component.translatable("arcanemysteries.config.offset_shift.name").withStyle(ChatFormatting.GRAY));

        // Custom Hotbar
        this.addButton("custom_hotbar", Side.RIGHT, () -> Component.translatable("arcanemysteries.config.custom_hotbar.name").append(": ").append(ConfigSettings.CUSTOM_HOTBAR_LAYOUT.get() ? ON : OFF),
                button -> ConfigSettings.CUSTOM_HOTBAR_LAYOUT.set(!ConfigSettings.CUSTOM_HOTBAR_LAYOUT.get()),
                false, false, true, Component.translatable("arcanemysteries.config.custom_hotbar.desc"));

        // Move body temp icon
        this.addButton("move_body_icon", Side.RIGHT, () -> Component.translatable("arcanemysteries.config.move_body_icon.name").append(": ").append(ConfigSettings.MOVE_BODY_ICON_WHEN_ADVANCED.get() ? ON : OFF),
                button -> ConfigSettings.MOVE_BODY_ICON_WHEN_ADVANCED.set(!ConfigSettings.MOVE_BODY_ICON_WHEN_ADVANCED.get()),
                false, false, true, Component.translatable("arcanemysteries.config.move_body_icon.desc"));
    }

    @Override
    public void onClose()
    {
        super.onClose();
        ConfigScreen.saveConfig();
    }
}
