package net.shadowbeast.projectshadow.enchantments;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.*;
import net.shadowbeast.projectshadow.effect.ModEffects;

import java.beans.EventHandler;


public class IceAspectEnchantment extends Enchantment {
    protected IceAspectEnchantment(Rarity pRarity, EnchantmentCategory weapon, EquipmentSlot... pApplicableSlots) {
        super(pRarity, EnchantmentCategory.WEAPON, pApplicableSlots);
    }

    @Override
    public int getMinCost(int pEnchantmentLevel) {
        return 10 + 20 * (pEnchantmentLevel - 1);
    }

    @Override
    public int getMaxCost(int pEnchantmentLevel) {
        return super.getMinCost(pEnchantmentLevel) + 50;
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }

    @Override
    public boolean checkCompatibility(Enchantment pEnch) {
        return super.checkCompatibility(pEnch) && pEnch != Enchantments.FIRE_ASPECT;
    }

    @Override
    public void doPostAttack(LivingEntity user, Entity target, int level) {
        if (target instanceof LivingEntity) {
            int duration = 100 * level; //Rn level 1 is 6-7 seconds (dk fully) and level 2 is 10 seconds, we can always change tho
            ((LivingEntity) target).addEffect(new MobEffectInstance(ModEffects.FREEZE.get(), duration, level - 1));
        }
    }

}












