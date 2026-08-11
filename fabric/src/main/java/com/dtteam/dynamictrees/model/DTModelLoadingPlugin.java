package com.dtteam.dynamictrees.model;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.model.blockstate.AerialRootsSoilBlockStateModel;
import com.dtteam.dynamictrees.model.blockstate.PottedSaplingBlockStateModel;
import com.dtteam.dynamictrees.model.blockstate.SurfaceRootBlockStateModel;
import com.dtteam.dynamictrees.model.blockstate.UnbakedBranchModel;
import com.dtteam.dynamictrees.model.blockstate.UnbakedCreakingHeartModel;
import com.dtteam.dynamictrees.model.blockstate.UnbakedRootsModel;
import com.dtteam.dynamictrees.model.blockstate.UnbakedRootsMossModel;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.minecraft.resources.Identifier;

/** Registers Dynamic Trees' 26.2 custom block-state model codecs with Fabric. */
public final class DTModelLoadingPlugin {

    public static final Identifier BRANCH = DynamicTrees.location("branch");
    public static final Identifier SURFACE_ROOT = DynamicTrees.location("surface_root");
    public static final Identifier ROOTS = DynamicTrees.location("roots");
    public static final Identifier CREAKING_HEART = DynamicTrees.location("creaking_heart");
    public static final Identifier POTTED_DYNAMIC_SAPLING = DynamicTrees.location("potted_dynamic_sapling");
    public static final Identifier AERIAL_ROOTS_SOIL = DynamicTrees.location("aerial_roots_soil");
    public static final Identifier ROOTS_MOSS = DynamicTrees.location("roots_moss");

    private DTModelLoadingPlugin() {}

    public static void register() {
        CustomUnbakedBlockStateModel.register(BRANCH, UnbakedBranchModel.CODEC);
        CustomUnbakedBlockStateModel.register(ROOTS, UnbakedRootsModel.CODEC);
        CustomUnbakedBlockStateModel.register(CREAKING_HEART, UnbakedCreakingHeartModel.CODEC);
        CustomUnbakedBlockStateModel.register(SURFACE_ROOT, SurfaceRootBlockStateModel.Unbaked.CODEC);
        CustomUnbakedBlockStateModel.register(POTTED_DYNAMIC_SAPLING, PottedSaplingBlockStateModel.Unbaked.CODEC);
        CustomUnbakedBlockStateModel.register(AERIAL_ROOTS_SOIL, AerialRootsSoilBlockStateModel.Unbaked.CODEC);
        CustomUnbakedBlockStateModel.register(ROOTS_MOSS, UnbakedRootsMossModel.CODEC);
    }
}
