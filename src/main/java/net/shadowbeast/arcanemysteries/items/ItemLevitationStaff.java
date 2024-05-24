package net.shadowbeast.arcanemysteries.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.shadowbeast.arcanemysteries.registries.ParticleRegistry;
import net.shadowbeast.arcanemysteries.registries.SoundRegistry;
import net.shadowbeast.arcanemysteries.util.LocalizeUtils;
import net.shadowbeast.arcanemysteries.networking.MessagesMod;
import net.shadowbeast.arcanemysteries.networking.packet.AddLevitationTagC2SPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
public class ItemLevitationStaff extends Item{
    public ItemLevitationStaff() {
        super(new Properties().durability(18).rarity(Rarity.UNCOMMON));
    }
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, @NotNull Player pPlayer, @NotNull InteractionHand pUsedHand) {

        if (pPlayer instanceof Player) {
            if (!pPlayer.getAbilities().instabuild){
                pPlayer.getCooldowns().addCooldown(this, 0);}
            if(pLevel.isClientSide()){
                for (int i = 5; i < 20; i++) {
                    double x = pPlayer.getX() + (pLevel.random.nextDouble() - 0.5) * 2;
                    double y = pPlayer.getY() + pLevel.random.nextDouble() * 2;
                    double z = pPlayer.getZ() + (pLevel.random.nextDouble() - 0.5) * 2;
                    pLevel.addParticle(ParticleRegistry.FEATHER_PARTICLES.get(), x, y, z, 0, 0, 0);
                }
            }
            if (!pLevel.isClientSide())
            {
                List<LivingEntity> entities = pLevel.getEntitiesOfClass(LivingEntity.class, pPlayer.getBoundingBox().inflate(1));
                for (LivingEntity livingEntity : entities) {
                    float max_health = livingEntity.getMaxHealth();
                    if (max_health < 30F) {
                        livingEntity.addEffect(new MobEffectInstance(MobEffects.LEVITATION, livingEntity.isShiftKeyDown() ? 8 : 15, 20, false, false));
                    }
                }
                MessagesMod.sendToServer(new AddLevitationTagC2SPacket());
                pPlayer.getItemInHand(pUsedHand).hurtAndBreak(1, pPlayer,
                        player1 -> pPlayer.broadcastBreakEvent(pPlayer.getUsedItemHand()));
            }
            pPlayer.playSound(SoundRegistry.LEVITATION_STAFF.get(), 1f, 1f);
        }
        return ItemUtils.startUsingInstantly(pLevel, pPlayer, pUsedHand);
    }
    @Override
    public boolean isEnchantable(@NotNull ItemStack pStack) {return false;}
    @Override
    public boolean isRepairable(@NotNull ItemStack stack) {return false;}
    @Override
    public boolean isValidRepairItem(@NotNull ItemStack pStack, @NotNull ItemStack pRepairCandidate) {return false;}
    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {return false;}
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, @NotNull TooltipFlag flagIn) {
        tooltip.add(LocalizeUtils.i18n("tooltip.levitation"));
        tooltip.add(LocalizeUtils.usesRemaining(stack.getMaxDamage() - stack.getDamageValue()));
    }
}