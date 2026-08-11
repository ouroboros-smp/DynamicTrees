package com.dtteam.dynamictrees.platform;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.model.FabricBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.model.FabricBlockStateModelPart;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Fabric bridge for level-aware block-state models.
 *
 * <p>Minecraft's vanilla model contract does not provide level context. Fabric's renderer API does, so dynamic tree
 * models collect their parts through the context-aware overload and emit the resulting model parts through Fabric's
 * quad pipeline.</p>
 */
public interface FabricDynamicBlockStateModel extends BlockStateModel, FabricBlockStateModel {

    void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
                      List<BlockStateModelPart> parts);

    @Override
    default void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
        // There is no safe level-independent geometry for a dynamic tree model.
    }

    @Override
    default void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state,
                           RandomSource random, Predicate<@Nullable Direction> cullTest) {
        List<BlockStateModelPart> parts = new ArrayList<>();
        collectParts(level, pos, state, random, parts);
        for (BlockStateModelPart part : parts) {
            ((FabricBlockStateModelPart) (Object) part).emitQuads(emitter, cullTest);
        }
    }
}
