package com.dtteam.dynamictrees;

import com.dtteam.dynamictrees.api.worldgen.LevelContext;
import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.block.soil.SoilProperties;
import com.dtteam.dynamictrees.client.TextureHelper;
import com.dtteam.dynamictrees.client.ThickBranchRingsSource;
import com.dtteam.dynamictrees.client.TintSources.CloneTintSource;
import com.dtteam.dynamictrees.client.TintSources.DendroPotionItemTintSource;
import com.dtteam.dynamictrees.client.TintSources.PottedSaplingTintSource;
import com.dtteam.dynamictrees.client.TintSources.SaplingTintSource;
import com.dtteam.dynamictrees.client.TintSources.SoilRootsTintSource;
import com.dtteam.dynamictrees.client.TintSources.StaffCrystalItemTintSource;
import com.dtteam.dynamictrees.client.TintSources.StaffHandleItemTintSource;
import com.dtteam.dynamictrees.client.TintSources.SuppliedConstantTintSource;
import com.dtteam.dynamictrees.config.DTConfigs;
import com.dtteam.dynamictrees.item.Seed;
import com.dtteam.dynamictrees.model.DTModelLoadingPlugin;
import com.dtteam.dynamictrees.model.entity.render.FallingTreeRenderer;
import com.dtteam.dynamictrees.model.entity.render.LingeringEffectorRenderer;
import com.dtteam.dynamictrees.registry.DTRegistries;
import com.dtteam.dynamictrees.systems.season.SeasonHelper;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.species.Species;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.SpriteSourceRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.config.ModConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class DynamicTreesFabricClient implements ClientModInitializer {

    private static boolean colorsInitialized;

    @Override
    public void onInitializeClient() {
        ConfigRegistry.INSTANCE.register(DynamicTrees.MOD_ID, ModConfig.Type.CLIENT, DTConfigs.CLIENT_CONFIG);
        SpriteSourceRegistry.register(ThickBranchRingsSource.ID, ThickBranchRingsSource.CODEC);
        DTModelLoadingPlugin.register();
        registerItemTintSources();
        registerEntityRenderers();
        registerTooltipCallback();
        registerClientTicks();
    }

    private static void registerItemTintSources() {
        ItemTintSources.ID_MAPPER.put(DynamicTrees.location("dendro_potion"), DendroPotionItemTintSource.MAP_CODEC);
        ItemTintSources.ID_MAPPER.put(DynamicTrees.location("staff_handle"), StaffHandleItemTintSource.MAP_CODEC);
        ItemTintSources.ID_MAPPER.put(DynamicTrees.location("staff_crystal"), StaffCrystalItemTintSource.MAP_CODEC);
    }

    private static void registerEntityRenderers() {
        EntityRendererRegistry.register(DTRegistries.FALLING_TREE.get(), FallingTreeRenderer::new);
        EntityRendererRegistry.register(DTRegistries.LINGERING_EFFECTOR.get(), LingeringEffectorRenderer::new);
    }

    private static void registerClientTicks() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (!colorsInitialized && client.level != null) {
                discoverWoodColors();
                registerBlockTintSources(client.getBlockColors());
                colorsInitialized = true;
            }
        });
        ClientTickEvents.START_LEVEL_TICK.register(level -> SeasonHelper.updateTick(level, level.getOverworldClockTime()));
    }

    private static void registerBlockTintSources(BlockColors blockColors) {
        SoilProperties.REGISTRY.getAll().stream().map(SoilProperties::getBlock).flatMap(Optional::stream)
                .forEach(soilBlock -> {
                    SoilProperties properties = soilBlock.getSoilProperties();
                    List<BlockTintSource> sources = CloneTintSource.cloneAllSources(
                            blockColors,
                            () -> properties.getPrimitiveSoilState(soilBlock.defaultBlockState()),
                            properties.getFoliageTintLayerCount());
                    sources.add(new SoilRootsTintSource(soilBlock));
                    blockColors.register(sources, soilBlock);
                });

        LeavesProperties.REGISTRY.getAll().stream().map(LeavesProperties::getDynamicLeavesBlock).flatMap(Optional::stream)
                .forEach(leaves -> {
                    LeavesProperties properties = leaves.getLeavesProperties();
                    if (properties.hasCustomColor()) {
                        Integer customColor = properties.getCustomColor();
                        blockColors.register(List.of(customColor == null
                                ? BlockTintSources.foliage()
                                : BlockTintSources.constant(customColor)), leaves);
                    } else {
                        blockColors.register(CloneTintSource.cloneAllSources(
                                blockColors, properties::getPrimitiveLeaves, properties.getFoliageTintLayerCount()), leaves);
                    }
                });

        blockColors.register(List.of(new PottedSaplingTintSource(blockColors)), DTRegistries.POTTED_SAPLING.get());
        Species.REGISTRY.getAll().stream().map(Species::getSapling).flatMap(Optional::stream)
                .forEach(sapling -> {
                    Species species = sapling.getSpecies();
                    blockColors.register(List.of(
                            new SaplingTintSource(blockColors, species),
                            new SuppliedConstantTintSource(() -> species.getFamily().woodBarkColor)), sapling);
                });
    }

    private static void registerTooltipCallback() {
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            Item item = stack.getItem();
            if (!(item instanceof Seed seed)) return;
            Player player = Minecraft.getInstance().player;
            if (player != null) seed.appendHoverText(stack, LevelContext.create(player.level()), lines, player);
        });
    }

    private static void discoverWoodColors() {
        for (Family family : Family.REGISTRY.getAll()) {
            family.woodRingColor = 0xFFF1AE;
            family.woodBarkColor = 0xB3A979;
            if (family != Family.NULL_FAMILY) {
                family.getPrimitiveLog().ifPresent(log -> {
                    BlockState state = log.defaultBlockState();
                    family.woodRingColor = getFaceColor(state, Direction.DOWN);
                    family.woodBarkColor = getFaceColor(state, Direction.NORTH);
                });
            }
        }
    }

    private static int getFaceColor(BlockState state, Direction face) {
        BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(RandomSource.create(), parts);
        if (parts.isEmpty()) return 0;
        List<BakedQuad> quads = parts.getFirst().getQuads(face);
        if (quads.isEmpty()) quads = parts.getFirst().getQuads(null);
        if (quads.isEmpty()) return 0;

        TextureAtlasSprite sprite = quads.getFirst().materialInfo().sprite();
        TextureHelper.PixelBuffer pixels = new TextureHelper.PixelBuffer(sprite);
        int unit = pixels.w / 16;
        TextureHelper.PixelBuffer center = new TextureHelper.PixelBuffer(unit * 8, unit * 8);
        pixels.blit(center, unit * -8, unit * -8);
        return center.averageColor();
    }
}
