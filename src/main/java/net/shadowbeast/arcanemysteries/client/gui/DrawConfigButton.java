package net.shadowbeast.arcanemysteries.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.shadowbeast.arcanemysteries.api.TaskScheduler;
import net.shadowbeast.arcanemysteries.config.ConfigSettings;
import net.shadowbeast.arcanemysteries.networking.ModMessages;
import net.shadowbeast.arcanemysteries.networking.packet.ClientConfigAskMessage;
import net.shadowbeast.arcanemysteries.util.MathHelper;
import net.shadowbeast.arcanemysteries.util.helper.ClientOnlyHelper;
import org.joml.Vector2i;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class DrawConfigButton
{
    public static boolean EDIT_MODE = false;

    @SubscribeEvent
    public static void eventHandler(ScreenEvent.Init.Post event)
    {
        if (event.getScreen() instanceof OptionsScreen && ConfigSettings.SHOW_CONFIG_BUTTON.get())
        {
            // The offset from the config
            Supplier<Vector2i> buttonPos = () -> ConfigSettings.CONFIG_BUTTON_POS.get();
            AtomicInteger xOffset = new AtomicInteger(buttonPos.get().x());
            AtomicInteger yOffset = new AtomicInteger(buttonPos.get().y());
            int buttonX = event.getScreen().width / 2 - 183;
            int buttonY = event.getScreen().height / 6 + 110;
            int screenWidth = event.getScreen().width;
            int screenHeight = event.getScreen().height;

            if (xOffset.get() + buttonX < -1 || yOffset.get() + buttonY < -1)
            {   xOffset.set(0);
                yOffset.set(0);
                ConfigSettings.CONFIG_BUTTON_POS.set(new Vector2i(0, 0));
            }

            // Main config button
            ImageButton mainButton = new ImageButton(buttonX + xOffset.get(), buttonY + yOffset.get(),
                    24, 24, 40, 40, 24,
                    new ResourceLocation("arcanemysteries:textures/gui/screen/config_gui.png"),
                    button ->
                    {
                        ConfigScreen.CURRENT_PAGE = 0;
                        if (!Minecraft.getInstance().isLocalServer() && Minecraft.getInstance().player != null)
                        {   ModMessages.INSTANCE.sendToServer(new ClientConfigAskMessage(Minecraft.getInstance().player.getUUID()));
                        }
                        else ClientOnlyHelper.openConfigScreen();
                    });
            // Add main button
            event.addListener(mainButton);

            // Reconfigure the options screen with controls for the position of the config button
            if (EDIT_MODE)
            {
                int buttonStartX = event.getScreen().width / 2 - 183;
                int buttonStartY = event.getScreen().height / 6 + 110;
                Runnable saveAndClamp = () ->
                {   xOffset.set(MathHelper.clamp(xOffset.get(), -buttonStartX, screenWidth - mainButton.getWidth() - buttonStartX));
                    yOffset.set(MathHelper.clamp(yOffset.get(), -buttonStartY, screenHeight - mainButton.getHeight() - buttonStartY));
                    mainButton.setPosition(buttonStartX + xOffset.get(), buttonStartY + yOffset.get());
                    ConfigSettings.CONFIG_BUTTON_POS.set(new Vector2i(xOffset.get(), yOffset.get()));
                };

                AtomicReference<AbstractButton> doneButtonAtomic = new AtomicReference<>(null);
                // Disable all other buttons
                event.getScreen().children().forEach(child ->
                {
                    // Don't disable "Done" button
                    if (child instanceof AbstractButton button)
                    {
                        boolean isDoneButton = button.getMessage().getString().equals(CommonComponents.GUI_DONE.getString());
                        if (!isDoneButton)
                        {   button.active = false;
                        }
                        else
                        {   doneButtonAtomic.set(button);
                            button.setWidth(button.getWidth() - 72);
                        }
                    }
                    if (child instanceof AbstractSliderButton slider)
                    {   slider.active = false;
                    }
                });

                if (doneButtonAtomic.get() == null) return;
                AbstractButton doneButton = doneButtonAtomic.get();

                // Create "left" button
                ImageButton leftButton = new ImageButton(doneButton.getX() + doneButton.getWidth() + 2, doneButton.getY(), 14, 20, 0, 0, 20,
                        new ResourceLocation("arcanemysteries:textures/gui/screen/config_gui.png"),
                        button ->
                        {   xOffset.set(xOffset.get() - ConfigScreen.SHIFT_AMOUNT.get());
                            saveAndClamp.run();
                        });
                // Add left button
                event.addListener(leftButton);

                // Create "up" button
                ImageButton upButton = new ImageButton(leftButton.getX() + leftButton.getWidth(), leftButton.getY(), 20, 10, 14, 0, 20,
                        new ResourceLocation("arcanemysteries:textures/gui/screen/config_gui.png"),
                        button ->
                        {   yOffset.set(yOffset.get() - ConfigScreen.SHIFT_AMOUNT.get());
                            saveAndClamp.run();
                        });
                // Add up button
                event.addListener(upButton);

                // Create "down" button
                ImageButton downButton = new ImageButton(upButton.getX(), upButton.getY() + upButton.getHeight(), 20, 10, 14, 10, 20,
                        new ResourceLocation("arcanemysteries:textures/gui/screen/config_gui.png"),
                        button ->
                        {   yOffset.set(yOffset.get() + ConfigScreen.SHIFT_AMOUNT.get());
                            saveAndClamp.run();
                        });
                // Add down button
                event.addListener(downButton);

                // Create "right" button
                ImageButton rightButton = new ImageButton(upButton.getX() + upButton.getWidth(), upButton.getY(), 14, 20, 34, 0, 20,
                        new ResourceLocation("arcanemysteries:textures/gui/screen/config_gui.png"),
                        button ->
                        {   xOffset.set(xOffset.get() + ConfigScreen.SHIFT_AMOUNT.get());
                            saveAndClamp.run();
                        });
                // Add right button
                event.addListener(rightButton);

                // Create "reset" button
                ImageButton resetButton = new ImageButton(rightButton.getX() + rightButton.getWidth() + 2, rightButton.getY(), 20, 20, 48, 0, 20,
                        new ResourceLocation("arcanemysteries:textures/gui/screen/config_gui.png"),
                        button ->
                        {   xOffset.set(0);
                            yOffset.set(0);
                            saveAndClamp.run();
                        });
                // Add reset button
                event.addListener(resetButton);

                TaskScheduler.scheduleClient(() -> EDIT_MODE = false, 1);
            }
        }
    }
}
