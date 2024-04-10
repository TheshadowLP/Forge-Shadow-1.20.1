package net.shadowbeast.projectshadow.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.shadowbeast.projectshadow.config.Config;
import net.shadowbeast.projectshadow.ProjectShadow;
import net.shadowbeast.projectshadow.items.ModItems;
import net.shadowbeast.projectshadow.items.custom.HammerItem;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = ProjectShadow.MOD_ID)
public class ModEvents {
    private static final Set<BlockPos> HARVESTED_BLOCKS = new HashSet<>();
    @SubscribeEvent
    public static void milkCow(@NotNull PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof LivingEntity targetEntity) {
            if (targetEntity instanceof Cow) {
                if (event.getEntity().getItemInHand(InteractionHand.MAIN_HAND).getItem() == Items.GLASS_BOTTLE) {
                    event.getEntity().playSound(SoundEvents.COW_MILK);
                    event.getEntity().getItemInHand(InteractionHand.MAIN_HAND).shrink(1);
                    event.getEntity().addItem(new ItemStack(ModItems.MILK_BOTTLE.get(), 1));
                    event.setCanceled(true); // Cancel the event to prevent default interaction
                }
            }
        }
    }
    @SubscribeEvent
    public static void bakedPotatoesDamageEntity(TickEvent.PlayerTickEvent event) {
        if (Config.bakedPotatoesDoDamage) {
            Player player = event.player;
            ItemStack mainHandItem = player.getMainHandItem();

            if (mainHandItem.getItem().equals(Items.BAKED_POTATO)) {
                player.hurt(player.damageSources().onFire(), 0.5F);
            }
        }
    }
    @SubscribeEvent
    public static void stackedBakedPotatoesDamageEntity(TickEvent.PlayerTickEvent event) {
        if (Config.bakedPotatoesDoDamage) {
            Player player = event.player;
            ItemStack mainHandItem = player.getMainHandItem();

            if (mainHandItem.getItem().equals(ModItems.STACKED_BAKED_POTATO.get())) {
                player.hurt(player.damageSources().onFire(), 0.7F);
            }
        }
    }
    @SubscribeEvent
    public static void onHammerUsage(@NotNull BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHandItem = player.getMainHandItem();

        if(mainHandItem.getItem() instanceof HammerItem hammer && player instanceof ServerPlayer serverPlayer) {
            BlockPos initalBlockPos = event.getPos();
            if (HARVESTED_BLOCKS.contains(initalBlockPos)) {
                return;
            }
            for (BlockPos pos : HammerItem.getBlocksToBeDestroyed(1, initalBlockPos, serverPlayer)) {
                if(pos == initalBlockPos || !hammer.isCorrectToolForDrops(mainHandItem, event.getLevel().getBlockState(pos))) {
                    continue;
                }
                // Have to add them to a Set otherwise, the same code right here will get called for each block!
                HARVESTED_BLOCKS.add(pos);
                serverPlayer.gameMode.destroyBlock(pos);
                HARVESTED_BLOCKS.remove(pos);
            }
        }
    }
}