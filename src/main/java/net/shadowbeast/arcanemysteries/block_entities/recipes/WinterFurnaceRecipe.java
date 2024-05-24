package net.shadowbeast.arcanemysteries.block_entities.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.shadowbeast.arcanemysteries.registries.ModBlocks;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
public class WinterFurnaceRecipe extends AbstractCookingRecipe {
    public WinterFurnaceRecipe(ResourceLocation pId, String pGroup,
                               CookingBookCategory pCategory, Ingredient pIngredient, ItemStack pResult, float pExperience, int pCookingTime) {
        super(Type.INSTANCE, pId, pGroup, pCategory, pIngredient, pResult, pExperience, pCookingTime);
    }
    @Override
    public @NotNull ItemStack getToastSymbol(){
        return new ItemStack(ModBlocks.WINTER_FURNACE.get());
    }
    @Override
    public @NotNull RecipeType<?> getType(){
        return Type.INSTANCE;
    }
    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }
    public static class Type implements RecipeType<WinterFurnaceRecipe>{
        public static final Type INSTANCE = new Type();
    }
    public static class Serializer implements RecipeSerializer<WinterFurnaceRecipe>{
        private final int defaultCookingTime;
        public static final Serializer INSTANCE = new Serializer(50);
        public Serializer(int defaultCookingTime) {

            this.defaultCookingTime = defaultCookingTime;
        }
        public @NotNull WinterFurnaceRecipe fromJson(@NotNull ResourceLocation pRecipeId, @NotNull JsonObject pJson) {
            String s = GsonHelper.getAsString(pJson, "group", "");
            CookingBookCategory cookingbookcategory = CookingBookCategory.CODEC.byName(GsonHelper.getAsString(pJson, "category", null), CookingBookCategory.MISC);
            JsonElement jsonelement = GsonHelper.isArrayNode(pJson, "ingredient") ? GsonHelper.getAsJsonArray(pJson, "ingredient") : GsonHelper.getAsJsonObject(pJson, "ingredient");
            Ingredient ingredient = Ingredient.fromJson(jsonelement, false);
            //Forge: Check if primitive string to keep vanilla or an object which can contain a count field.
            if (!pJson.has("result")) throw new com.google.gson.JsonSyntaxException("Missing result, expected to find a string or object");
            ItemStack itemstack;
            if (pJson.get("result").isJsonObject()) itemstack = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pJson, "result"));
            else {
                String s1 = GsonHelper.getAsString(pJson, "result");
                ResourceLocation resourcelocation = new ResourceLocation(s1);
                itemstack = new ItemStack(BuiltInRegistries.ITEM.getOptional(resourcelocation).orElseThrow(() -> new IllegalStateException("Item: " + s1 + " does not exist")));
            }
            float f = GsonHelper.getAsFloat(pJson, "experience", 0.0F);
            int i = GsonHelper.getAsInt(pJson, "cookingtime", this.defaultCookingTime);
            return new WinterFurnaceRecipe(pRecipeId, s, cookingbookcategory, ingredient, itemstack, f, i);
        }
        public WinterFurnaceRecipe fromNetwork(@NotNull ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            String s = pBuffer.readUtf();
            CookingBookCategory cookingbookcategory = pBuffer.readEnum(CookingBookCategory.class);
            Ingredient ingredient = Ingredient.fromNetwork(pBuffer);
            ItemStack itemstack = pBuffer.readItem();
            float f = pBuffer.readFloat();
            int i = pBuffer.readVarInt();
            return new WinterFurnaceRecipe(pRecipeId, s, cookingbookcategory, ingredient, itemstack, f, i);
        }
        public void toNetwork(FriendlyByteBuf pBuffer, WinterFurnaceRecipe pRecipe) {
            pBuffer.writeUtf(pRecipe.group);
            pBuffer.writeEnum(pRecipe.category());
            pRecipe.ingredient.toNetwork(pBuffer);
            pBuffer.writeItem(pRecipe.result);
            pBuffer.writeFloat(pRecipe.experience);
            pBuffer.writeVarInt(pRecipe.cookingTime);
        }
    }
}
