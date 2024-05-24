package net.shadowbeast.arcanemysteries.block_entities.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.ItemStack;
import net.shadowbeast.arcanemysteries.block_entities.recipes.WinterFurnaceRecipe;
import org.jetbrains.annotations.NotNull;

public class WinterFurnaceMenu extends AbstractFurnaceMenu {
    protected WinterFurnaceMenu(int pContainerId, Inventory pPlayerInventory, FriendlyByteBuf friendlyByteBuf) {
        this(pContainerId, pPlayerInventory);
    }
    public WinterFurnaceMenu(int pContainerId, Inventory pPlayerInventory, Container container, ContainerData data) {
        super(MenuTypesMod.WINTER_FURNACE_MENU.get(), WinterFurnaceRecipe.Type.INSTANCE, RecipeBookType.FURNACE, pContainerId, pPlayerInventory, container, data);
    }
    public WinterFurnaceMenu(int pContainerId, Inventory pPlayerInventory) {
        super(MenuTypesMod.WINTER_FURNACE_MENU.get(), WinterFurnaceRecipe.Type.INSTANCE, RecipeBookType.FURNACE, pContainerId, pPlayerInventory);
    }
    @Override
    protected boolean isFuel(@NotNull ItemStack pStack) {
        return true;
    }
}
