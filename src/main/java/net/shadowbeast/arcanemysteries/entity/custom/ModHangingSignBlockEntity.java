package net.shadowbeast.arcanemysteries.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HangingSignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowbeast.arcanemysteries.entity.ModBlockEntities;
import org.jetbrains.annotations.NotNull;

public class ModHangingSignBlockEntity extends HangingSignBlockEntity {
    public ModHangingSignBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(blockPos, blockState);
    }
    @Override
    public @NotNull BlockEntityType<?> getType() { return ModBlockEntities.MOD_HANGING_SIGN.get(); }
}