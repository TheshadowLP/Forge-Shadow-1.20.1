package net.shadowbeast.frostbound.util.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.*;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.shadowbeast.frostbound.Frostbound;
import net.shadowbeast.frostbound.block_entities.menu.AlloyFurnaceMenu;
import net.shadowbeast.frostbound.block_entities.menu.CrusherMenu;
import net.shadowbeast.frostbound.block_entities.menu.ModMenuTypes;
import net.shadowbeast.frostbound.block_entities.recipes.AlloyFurnaceRecipe;
import net.shadowbeast.frostbound.block_entities.recipes.CrusherRecipe;
import net.shadowbeast.frostbound.block_entities.screen.AlloyFurnaceScreen;
import net.shadowbeast.frostbound.block_entities.screen.CrusherScreen;
import net.shadowbeast.frostbound.registries.ModBlocks;
import net.shadowbeast.frostbound.util.jei.category.AlloyingCategory;
import net.shadowbeast.frostbound.util.jei.category.CrushingCategory;

import javax.annotation.ParametersAreNonnullByDefault;
@JeiPlugin
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FrostboundJEI implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(Frostbound.MOD_ID, "jei_plugin");
    }
    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new CrushingCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new AlloyingCategory(registration.getJeiHelpers().getGuiHelper()));
        //TODO
    }
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        assert Minecraft.getInstance().level != null;
        CrusherRecipe.addAllRecipes(Minecraft.getInstance().level.getRecipeManager(), registration);
        AlloyFurnaceRecipe.addAllRecipes(Minecraft.getInstance().level.getRecipeManager(), registration);
        //TODO
    }
    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(CrusherScreen.class, 82, 35, 11, 18,
                CrushingCategory.CRUSHER_RECIPE_TYPE);
        registration.addRecipeClickArea(AlloyFurnaceScreen.class, 80, 10, 14, 14,
                AlloyingCategory.ALLOY_FURNACE_RECIPE_TYPE);
    }
    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CRUSHER.get()), CrushingCategory.CRUSHER_RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ALLOY_FURNACE.get()), AlloyingCategory.ALLOY_FURNACE_RECIPE_TYPE);
    }
    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(CrusherMenu.class, ModMenuTypes.CRUSHER_MENU.get(),
                CrushingCategory.CRUSHER_RECIPE_TYPE, 0, 3, 3, 36);
        registration.addRecipeTransferHandler(AlloyFurnaceMenu.class, ModMenuTypes.ALLOY_FURNACE_MENU.get(),
                AlloyingCategory.ALLOY_FURNACE_RECIPE_TYPE, 0, 3, 3, 36);
    }
}
