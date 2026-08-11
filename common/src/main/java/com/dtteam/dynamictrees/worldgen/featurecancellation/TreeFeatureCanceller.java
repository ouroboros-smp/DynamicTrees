package com.dtteam.dynamictrees.worldgen.featurecancellation;

import com.dtteam.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.dtteam.dynamictrees.api.worldgen.FeatureCanceller;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class TreeFeatureCanceller<T extends FeatureConfiguration> extends FeatureCanceller {

    private final Class<T> treeFeatureConfigClass;

    public TreeFeatureCanceller(final Identifier registryName, Class<T> treeFeatureConfigClass) {
        super(registryName);
        this.treeFeatureConfigClass = treeFeatureConfigClass;
    }

    @Override
    public boolean shouldCancel(ConfiguredFeature<?, ?> configuredFeature, BiomePropertySelectors.NormalFeatureCancellation featureCancellations) {
        final FeatureConfiguration featureConfig = configuredFeature.config();

        if (this.treeFeatureConfigClass.isInstance(featureConfig)) {
            String namespace = "";
            final Identifier featureRegistryName = BuiltInRegistries.FEATURE.getKey(configuredFeature.feature());
            if (featureRegistryName != null) {
                namespace = featureRegistryName.getNamespace();
            }
            return !namespace.isEmpty() && featureCancellations.shouldCancelNamespace(namespace);
        }

        // Selector features expose their nested configured features through this API. Recursing here covers both
        // random_selector and simple_random_selector without depending on their concrete configuration classes.
        return featureConfig.getSubFeatures()
                .map(holder -> holder.value())
                .anyMatch(subFeature -> this.shouldCancel(subFeature, featureCancellations));
    }

}
