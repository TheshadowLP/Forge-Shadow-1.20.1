package net.shadowbeast.projectshadow.block_entities.screen;

import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowbeast.projectshadow.ProjectShadow;
import net.shadowbeast.projectshadow.block_entities.menu.WinterFurnaceMenu;
import net.shadowbeast.projectshadow.block_entities.recipes.WinterFurnaceRecipeBookComponent;

@OnlyIn(Dist.CLIENT)
public class WinterFurnaceScreen extends AbstractFurnaceScreen<WinterFurnaceMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(ProjectShadow.MOD_ID,"textures/gui/winter_furnace.png");
    public WinterFurnaceScreen(WinterFurnaceMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, new WinterFurnaceRecipeBookComponent(), pPlayerInventory, pTitle, TEXTURE);
    }
}
