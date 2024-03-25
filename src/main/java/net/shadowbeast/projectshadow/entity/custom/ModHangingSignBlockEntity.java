package net.shadowbeast.projectshadow.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowbeast.projectshadow.entity.ModBlockEntities;
import org.jetbrains.annotations.NotNull;

public class ModHangingSignBlockEntity extends SignBlockEntity {
    public ModHangingSignBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.MOD_HANGING_SIGN.get(), blockPos, blockState);
    }
    @Override
    public @NotNull BlockEntityType<?> getType() {
        return ModBlockEntities.MOD_HANGING_SIGN.get();
    }
    public int getTextLineHeight() {return 9;}
    public int getMaxTextLineWidth() {return 60;}
}