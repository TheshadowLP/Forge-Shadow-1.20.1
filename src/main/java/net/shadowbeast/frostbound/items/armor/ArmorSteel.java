package net.shadowbeast.frostbound.items.armor;

import com.google.common.collect.ImmutableMap;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.shadowbeast.frostbound.enums.ArmorStats;

import java.util.Map;
public class ArmorSteel extends ArmorItem {
    public static final Map<ArmorMaterial, MobEffectInstance> MATERIAL_TO_EFFECT_MAP =
            (new ImmutableMap.Builder<ArmorMaterial, MobEffectInstance>())
                    .put(ArmorStats.STEEL, new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200,
                            0, false, false, false)).build();
    private ArmorSteel(ArmorMaterial pMaterial, Type pType, Properties pProperties) {
        super(pMaterial, pType, pProperties);
    }
    @SuppressWarnings("removal")
    @Override
    public void onArmorTick(ItemStack stack, Level world, Player player) {
        if(!world.isClientSide()) {
            if(hasFullSuitOfArmorOn(player)) {
                evaluateArmorEffects(player);
            }
        }
    }
    private void evaluateArmorEffects(Player player) {
        for(Map.Entry<ArmorMaterial, MobEffectInstance> entry : MATERIAL_TO_EFFECT_MAP.entrySet()) {
            MobEffectInstance mapStatusEffect = entry.getValue();

            if(hasCorrectArmorOn(player)) {
                addStatusEffectForMaterial(player, mapStatusEffect);
            }
        }
    }
    private void addStatusEffectForMaterial(Player player, MobEffectInstance pEffect) {
        boolean hasPlayerEffect = player.hasEffect(pEffect.getEffect());

        if(hasCorrectArmorOn(player) && !hasPlayerEffect && !(player.hasEffect(MobEffects.DAMAGE_BOOST))) {
            player.addEffect(pEffect);
        }
    }
    private boolean hasCorrectArmorOn(Player player) {
        for(ItemStack armorStack : player.getInventory().armor) {
            if(!(armorStack.getItem() instanceof ArmorItem)) {
                return false;
            }
        }
        ArmorItem helmet = ((ArmorItem) player.getInventory().getArmor(0).getItem());
        ArmorItem chestplate = ((ArmorItem) player.getInventory().getArmor(1).getItem());
        ArmorItem leggings = ((ArmorItem) player.getInventory().getArmor(2).getItem());
        ArmorItem boots = ((ArmorItem) player.getInventory().getArmor(3).getItem());

        return helmet.getMaterial() == ArmorStats.STEEL && chestplate.getMaterial() == ArmorStats.STEEL
                && leggings.getMaterial() == ArmorStats.STEEL && boots.getMaterial() == ArmorStats.STEEL;
    }
    private boolean hasFullSuitOfArmorOn(Player player) {
        ItemStack helmet = player.getInventory().getArmor(0);
        ItemStack chestplate = player.getInventory().getArmor(1);
        ItemStack leggings = player.getInventory().getArmor(2);
        ItemStack boots = player.getInventory().getArmor(3);

        return !helmet.isEmpty() && !chestplate.isEmpty()
                && !leggings.isEmpty() && !boots.isEmpty();
    }
    public static ArmorSteel getInstance(Type ptype) {
        return new ArmorSteel(ArmorStats.STEEL, ptype, new Properties().stacksTo(1));
    }
}
