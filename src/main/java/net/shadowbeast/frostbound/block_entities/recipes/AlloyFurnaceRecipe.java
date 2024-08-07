package net.shadowbeast.frostbound.block_entities.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.shadowbeast.frostbound.Frostbound;
import net.shadowbeast.frostbound.util.ModTags;
import net.shadowbeast.frostbound.util.jei.category.AlloyingCategory;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class AlloyFurnaceRecipe implements Recipe<SimpleContainer> {
    private final ResourceLocation id;
    private final ItemStack output;
    private final NonNullList<Ingredient> recipeItems;
    public final static int DEFAULT_COOK_TIME = 260;
    private final float experience;
    protected final int cookingTime;
    public AlloyFurnaceRecipe(ResourceLocation id, ItemStack output, NonNullList<Ingredient> recipeItems, int cookingTime, float experience) {
        this.id = id;
        this.output = output;
        this.recipeItems = recipeItems;
        this.cookingTime = cookingTime;
        this.experience = experience;
    }

    @Override
    public boolean matches(SimpleContainer pContainer, Level pLevel) {
        if (pLevel.isClientSide()) return false;
        if (recipeItems.get(0).test(pContainer.getItem(1))) {
            return recipeItems.get(1).test(pContainer.getItem(2));
        }
        if (recipeItems.get(0).test(pContainer.getItem(2))) {
            return recipeItems.get(1).test(pContainer.getItem(1));
        }
        return false;
    }

    @Override
    public @NotNull ItemStack assemble(SimpleContainer simpleContainer, RegistryAccess registryAccess) { return output; }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() { return recipeItems; }
    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) { return true; }
    @Override
    public @NotNull ItemStack getResultItem(RegistryAccess registryAccess) { return output.copy(); }
    @Override
    public @NotNull ResourceLocation getId() { return id; }

    public float getExperience() { return experience; }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() { return Serializer.INSTANCE; }
    @Override
    public @NotNull RecipeType<?> getType() { return Type.INSTANCE; }
    public ItemStack getResultItem() { return output.copy(); }
    public Ingredient getFuelItem() { return Ingredient.of(ModTags.Items.ALLOYING_FUEL); }
    public int getCookingTime() { return this.cookingTime; }
    public static class Type implements RecipeType<AlloyFurnaceRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "alloying";
    }
    public static void addAllRecipes(RecipeManager recipeManager, IRecipeRegistration registration) {
        List<AlloyFurnaceRecipe> alloyFurnaceRecipes = recipeManager.getAllRecipesFor(Type.INSTANCE);
        registration.addRecipes(AlloyingCategory.ALLOY_FURNACE_RECIPE_TYPE, alloyFurnaceRecipes);
    }

    public static class Serializer implements RecipeSerializer<AlloyFurnaceRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation(Frostbound.MOD_ID, "alloying");
        @Override
        public @NotNull AlloyFurnaceRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pSerializedRecipe, "output"));
            float experience = GsonHelper.getAsFloat(pSerializedRecipe, "experience");

            JsonArray ingredients = GsonHelper.getAsJsonArray(pSerializedRecipe, "ingredients");
            NonNullList<Ingredient> inputs = NonNullList.withSize(2, Ingredient.EMPTY);

            for (int idx = 0; idx < inputs.size(); idx++) {
                inputs.set(idx, Ingredient.fromJson(ingredients.get(idx)));
            }
            int cookingTime = GsonHelper.getAsInt(pSerializedRecipe, "cookingtime", DEFAULT_COOK_TIME);

            return new AlloyFurnaceRecipe(pRecipeId, output, inputs, cookingTime, experience);
        }
        @Override
        public @Nullable AlloyFurnaceRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            NonNullList<Ingredient> inputs = NonNullList.withSize(buf.readInt(), Ingredient.EMPTY);

            inputs.replaceAll(ignored -> Ingredient.fromNetwork(buf));
            ItemStack output = buf.readItem();
            int i = buf.readVarInt();
            float experience = buf.readFloat();
            return new AlloyFurnaceRecipe(id, output, inputs, i, experience);
        }
        @Override
        public void toNetwork(FriendlyByteBuf buf, AlloyFurnaceRecipe recipe) {
            buf.writeInt(recipe.getIngredients().size());

            for (Ingredient ingredient : recipe.getIngredients()) {
                ingredient.toNetwork(buf);
            }
            buf.writeItemStack(recipe.getResultItem(), false);
            buf.writeVarInt(recipe.cookingTime);
            buf.writeFloat(recipe.experience);
        }
    }
}